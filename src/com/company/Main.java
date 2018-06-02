package com.company;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    public static final String NEW_LINE = System.lineSeparator();
    private static final ArrayList<HashMap<String, Long>> results = new ArrayList<>();
    private static final ArrayList<Future<HashMap<String, Long>>> _parsers = new ArrayList<>();

    public static void main(String[] filePath) {

        //This problem is a classic Producer-Consumer problem where producer
        //puts into a buffer and consumer takes from the buffer.

        //Validate the list of file paths.
        validateInput(filePath);

        //If we reached here, then all the input file paths were valid.

        //Since reading is off disk - it is a slow process. Assuming a single machine
        //with reasonable number of cores (<= 32), single reader thread should be fine.
        //The performance can be tuned for specific cases. Multiple reader threads,
        //reading from multiple files at the same time will cause the disk's head to jump all
        //over the place. That is undesirable for HDDs since the movement of head is a
        //mechanical process and therefore slow.

        //Even for SSDs, having multiple reader threads may not be a great idea as the
        //task is consumer heavy - the consumer having to process multiple words in a line.

        //Calculate the number of consumer (aka LineParser) tasks
        //Not a performance tuned decision, but initializing as many line parsers as the
        //number of processors on the machine should be fine. That will result in 100% CPU usage.
        //Since the availableProcessors() call returns the logical processors, the case
        //of hyper-threading has been already taken care of.
        int processorCount = Runtime.getRuntime().availableProcessors();

        //Initialize the buffer where producer and consumer will write/read data.
        //Setting the capacity to be four times the number of consumer threads so that
        //there is ample of space for multiple threads to work, without exhausting the
        //system memory.
        ArrayBlockingQueue<String> lines = new ArrayBlockingQueue<>(4 * processorCount);

        //Initiate reader.
        Thread reader = getReaderThread(filePath, lines);
        reader.start();

        //Start a executor service that will run a precomputed count of threads.
        ExecutorService executor = Executors.newFixedThreadPool(processorCount);
        createAndSubmitTasksTo(executor, processorCount, lines);

        //First wait for reader thread to finish.
        try {
            reader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Interrupted when waiting for Reader thread to finish.");
            errorExit();
        }

        //Soon after reader, parsers will be done. Wait for them.
        //Collect the result from all parser.
        HashMap<String, Long> result = mergeResults();

        //Finally, sort the results on key in descending order.
        Map<Long, String> sortedResult = new TreeMap<>(Comparator.reverseOrder());
        for (String key : result.keySet()) {
            sortedResult.put(result.get(key), key);
        }

        printLineBreak();
        Iterator<Map.Entry<Long, String>> sortedResultIterator = sortedResult.entrySet().iterator();
        while (sortedResultIterator.hasNext()) {
            printEntry(sortedResultIterator.next());
        }
        printLineBreak();
        waitForReturnPress();
    }

    private static void printEntry(Map.Entry<Long, String> resultEntry) {
        System.out.println(resultEntry.getValue() + "\t\t" + resultEntry.getKey());
    }

    private static void createAndSubmitTasksTo(ExecutorService executor, int lineParserCount, ArrayBlockingQueue<String> lines) {
        for (int index = 0; index < lineParserCount; index++) {
            LineParser callableParser = new LineParser(lines);
            Future<HashMap<String, Long>> futureParser = executor.submit(callableParser);
            _parsers.add(futureParser);
        }
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
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if (parserResult == null) {
                //Raise an exception
                continue;
            }
            for (String key : parserResult.keySet()) {
                if (result.containsKey(key)) {
                    result.put(key, result.get(key) + parserResult.get(key));
                } else {
                    result.put(key, parserResult.get(key));
                }
            }
        }

        //The results have been collected. Return them.
        return result;
    }

//    private static void startLineParsers(Thread[] lineParser) {
//        for (Thread parser : lineParser){
//            parser.start();
//        }
//    }

//    private static Thread[] getParserThreads(int parserCount, ArrayBlockingQueue<String> lines) {
//        //Not checking for validity of value in 'parserCount'. Barricading expected.
//        Thread[] parserThreads = new Thread[parserCount];
//        for (int parserIndex = 0; parserIndex < parserCount; parserIndex++) {
//            LineParser lineParser = new LineParser(lines);
//            parserThreads[parserIndex] = new Thread(lineParser);
//        }
//        return parserThreads;
//    }

    private static Thread getReaderThread(String[] filePath, ArrayBlockingQueue<String> lines) {
        DiskFileReader diskFileReader = new DiskFileReader(filePath, lines);
        Thread readerThread = new Thread(diskFileReader);
        return readerThread;
    }

    private static void validateInput(String[] filePath) {

        //The standard input from command line is never null.
        //But we're checking for the input in this case because it has been factored out
        //in a method which can be called from other places within the class too.

        StringBuilder err = new StringBuilder();
        if (filePath == null || filePath.length == 0) {
            String nullOrEmptyListMessage = "No file path provided.";
            System.out.println(nullOrEmptyListMessage);
            System.out.println();
            errorExit();
        }

        //Verify if each file path
        for (String path : filePath) {
            File inputFile = new File(path);
            try {
                //Is the given path a file?
                if (!inputFile.isFile()) {
                    err.append("\tInvalid File Path: " + path);
                    err.append(NEW_LINE);
                    continue;
                }
                //If the path is a file, is it readable?
                if (!inputFile.canRead()) {
                    err.append("\tCannot Read File: " + path);
                    err.append(NEW_LINE);
                }
            } catch (SecurityException securityException) {
                //The only exception this code can throw is SecurityException
                //when a security manager exists and denies the read access.
                err.append("\tRead access denied for file path: " + path);
                err.append("\tDetailed Message: " + securityException.getMessage());
                err.append(NEW_LINE);
            }
        }

        if (err.length() == 0) return;

        //Now, if the String Builder is not empty, then there were errors.
        //Show errors on the terminal and exit.
        System.out.println("There were one or more errors with the input:");
        System.out.println(err.toString());
        System.out.println();
        errorExit();
    }

    private static void errorExit() {
        waitForReturnPress();
        System.exit(-1);
    }

    private static void waitForReturnPress() {
        System.out.println();
        System.out.println("Press Return key to exit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printLineBreak() {
        System.out.println("----------------------------------");
    }
}