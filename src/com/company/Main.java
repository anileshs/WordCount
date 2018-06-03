package com.company;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class Main {

    //region Static Final Class Variables and Collections

    private static final int PARSER_TIMEOUT = 5;
    private static final TimeUnit PARSER_TIMEOUT_UNIT = TimeUnit.SECONDS;
    private static final ArrayList<Future<HashMap<String, Long>>> _parsers = new ArrayList<>();
    private static final ArrayList<Callable<HashMap<String, Long>>> _lineParsers = new ArrayList<>();

    //endregion

    public static void main(String[] filePath) {

        //1. Validate the list of file paths.
        validateInput(filePath);

        //2. Orchestrate word count process if all the input file paths were valid.
        orchestrateWordCount(filePath);

        //3. Collect the results when the execution of threads is done.
        mergeAndPrintResult();

        //4. Wait for user to see the results.
        waitForReturnPress();
    }

    //region Printing Results

    private static void mergeAndPrintResult() {
        ConsoleOutput.printMessageWithGaps("Merging individual parser results...");
        HashMap<String, Long> result = mergeResults();
        ConsoleOutput.printMessageWithGaps("Results merged.");
        ConsoleOutput.blockPrintMap(result);
    }

    private static HashMap<String, Long> mergeResults() {
        //Instantiate the map to be returned.
        HashMap<String, Long> result = new HashMap<>();

        //Merge the individual result of each line parser iteratively
        for (Future<HashMap<String, Long>> parser : _parsers) {

            //Merge the individual key in current line parser result
            HashMap<String, Long> parserResult = null;
            try {
                parserResult = parser.get();
            } catch (InterruptedException e) {
                //Handle it the standard way for this project.
                final String errorHeader = "Main interrupted while awaiting LineParser result.";
                ConsoleOutput.printInterruptedException(errorHeader, e);
            } catch (ExecutionException e) {
                final Throwable cause = new Throwable(e).getCause();
                final String errorHeader = "ExcecutionException thrown while Main was waiting to get individual results from Line Parser.";
                ConsoleOutput.printExecutionException(errorHeader, cause, e);
            }

            if (parserResult == null) {
                final String message = "Line Parser returned a null result.";
                ConsoleOutput.printMessageWithGapsAndLineBreaks(message);
                continue;
            }

            for (String key : parserResult.keySet()) {
                if (!result.containsKey(key)) result.put(key, 0L);
                result.put(key, result.get(key) + parserResult.get(key));
            }
        }

        //The results have been collected. Return them.
        return result;
    }

    //endregion

    //region Orchestration

    private static void orchestrateWordCount(String[] filePath) {

        //Initialize the buffer where producer and consumer will write/read data.
        //Setting up a LinkedBlockingQueue, so that there is no space issue in the buffer.
        BlockingQueue<String> lines = new LinkedBlockingQueue<>();

        //Initiate reader.
        Thread reader = startReader(filePath, lines);

        //Initiate parsers.
        ExecutorService executor = startProcessors(lines);

        //First wait for reader thread to finish.
        waitForReader(reader);

        //Soon after reader, parsers will be done. Wait for them.
        waitForParsers(executor);
    }

    //region Termination

    private static void waitForParsers(ExecutorService executor) {
        //While ideally there is a scope for this method to be stuck in an infinite
        //loop, but if everything else went well, the method should return with a
        //graceful termination of the executor service.

        try {
            //First, do a normal wait.
            executor.awaitTermination(PARSER_TIMEOUT, PARSER_TIMEOUT_UNIT);

            //If the executor service did not terminate in initial timeout,
            //attempt to terminate it in a loop, notifying the user on console.
            while (!executor.isTerminated()){
                System.out.println("ExecutorService still awaiting termination...");
                executor.awaitTermination(PARSER_TIMEOUT, PARSER_TIMEOUT_UNIT);
            }

            //Print graceful parsers termination message.
            ConsoleOutput.printMessageWithGaps("ExecutorService gracefully terminated.");
        } catch (InterruptedException e) {
            final String errorHeader = "Main thread interrupted while awaiting termination of Line Parsers.";
            ConsoleOutput.printInterruptedException(errorHeader, e);
        }
    }

    private static void waitForReader(Thread reader) {
        try {
            //Just do a normal wait.
            reader.join();

            //Print graceful reader thread termination message.
            ConsoleOutput.printMessageWithGaps("Reader thread gracefully terminated.");
        } catch (InterruptedException e) {
            final String errorHeader = "Interrupted when waiting for Reader thread to finish.";
            ConsoleOutput.printInterruptedException(errorHeader, e);
        }
    }

    //endregion

    //region Invocation

    private static ExecutorService startProcessors(BlockingQueue<String> lines) {

        //Calculate the number of consumer (aka LineParser) tasks
        //Not a performance tuned decision, but initializing as many line parsers as the
        //number of processors on the machine should be fine. For large enough input,
        //this will use available cores efficiently.

        //Since the availableProcessors() call returns the logical processors, the case
        //of hyper-threading is also taken care of.

        int processorCount = Runtime.getRuntime().availableProcessors();

        //Start a executor service that will run a fixed number of threads.
        ExecutorService executor = Executors.newFixedThreadPool(processorCount);

        instantiateParsers(lines, processorCount);
        invokeParsers(executor);

        executor.shutdown(); //to disable any new tasks from being submitted.
        return executor;
    }

    private static void invokeParsers(ExecutorService executor) {
        try {
            final List<Future<HashMap<String, Long>>> futureTaskList = executor.invokeAll(_lineParsers);
            ConsoleOutput.printMessageWithGaps("Line Parsers invoked by the ExecutorService.");
            _parsers.addAll(futureTaskList);
        } catch (InterruptedException e) {
            final String errorHeader = "Main interrupted while awaiting invoke of Line Parsers.";
            ConsoleOutput.printInterruptedException(errorHeader, e);
        }
    }

    private static void instantiateParsers(BlockingQueue<String> lines, int processorCount) {
        for (int index = 0; index < processorCount; index++) {
            LineParser callableParser = new LineParser(lines);
            _lineParsers.add(callableParser);
        }
    }

    private static Thread startReader(String[] filePath, BlockingQueue<String> lines) {
        DiskFileReader diskFileReader = new DiskFileReader(filePath, lines);
        Thread reader = new Thread(diskFileReader);
        reader.start();
        return reader;
    }

    //endregion

    //endregion

    //region Validation

    private static void validateInput(String[] filePath) {

        //The standard input from command line is never null.
        //But we're checking for the input in this case because it has been factored out
        //in a method which can be called from other places within the class too.

        ConsoleOutput.printMessageWithGaps("Validating input file paths...");

        StringBuilder err = new StringBuilder();
        if (filePath == null || filePath.length == 0) {
            String nullOrEmptyListMessage = "No file path provided.";
            System.out.println(nullOrEmptyListMessage);
            System.out.println();
            errorExit();
        }

        //Verify if each file path
        assert filePath != null;
        for (String path : filePath) {
            File inputFile = new File(path);
            try {
                //Is the given path a file?
                if (!inputFile.isFile()) {
                    err.append("\tInvalid File Path: ").append(path);
                    err.append(ConsoleOutput.NEW_LINE);
                    continue;
                }
                //If the path is a file, is it readable?
                if (!inputFile.canRead()) {
                    err.append("\tCannot Read File: ").append(path);
                    err.append(ConsoleOutput.NEW_LINE);
                }
            } catch (SecurityException securityException) {
                //The only exception this code can throw is SecurityException
                //when a security manager exists and denies the read access.
                err.append("\tRead access denied for file path: ").append(path);
                err.append("\tDetailed Message: ").append(securityException.getMessage());
                err.append(ConsoleOutput.NEW_LINE);
            }
        }

        //If no error, say that and return.
        if (err.length() == 0) {
            ConsoleOutput.printMessageWithGaps("Done with input file path validation.");
            return;
        }

        //Now, if the String Builder is not empty, then there were errors.
        //Show errors on the terminal and quit the program.
        String errorMessage = "There were one or more errors with the input:"
                + ConsoleOutput.NEW_LINE
                + err.toString();
        ConsoleOutput.printFatalErrorMessage(errorMessage);
        errorExit();
    }

    private static void errorExit() {
        waitForReturnPress();
        System.exit(-1);
    }

    //endregion

    private static void waitForReturnPress() {
        System.out.println();
        System.out.println("Press Return key to exit...");
        try {
            final int read = System.in.read();
            assert read >= 0;
        } catch (IOException e) {
            //Any exception at this point of time is useless. We are exiting
            //anyway. However, let's log it for the sake of good coding.
            final String errorHeader = "IOException raised when waiting for user to press Return.";
            ConsoleOutput.printIOException(errorHeader, e);
            //Alternatively, we could call e.printStackTrace();
        }
    }
}