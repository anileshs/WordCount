package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * The class which can be put on a thread and used for reading input from disk.
 */
class DiskFileReader implements Runnable {

    //region Final Variables and Collections

    private final String[] _filePath;
    private final BlockingQueue<String> _lines;

    //endregion

    //region Constructor

    DiskFileReader(String[] filePath, BlockingQueue<String> lines) {
        _filePath = filePath;
        _lines = lines;
    }

    //endregion

    //region Run method implementation

    @Override
    public void run() {
        ConsoleOutput.printMessageWithGaps("Reading the lines from input files...");
        populateLines();
        markBlockingQueueAsDone();
        ConsoleOutput.printMessageWithGaps("Done reading lines from input files.");
    }

    private void markBlockingQueueAsDone(){
        //To tell that the reading is done, we add the END_MARKER at the end.
        try {
            _lines.put(ConsoleOutput.END_MARKER);
        } catch (InterruptedException e) {
            final String errorHeader = "Failed to mark the buffer as done. Kill the program manually.";
            ConsoleOutput.printInterruptedException(errorHeader, e);
        }
    }

    private void populateLines() {
        //Iterate over all the files provided in the array
        for (String path : _filePath) {
            //1. Open the file
            try (FileReader fileReader = new FileReader(path);
                 BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                //2. Read and put the lines one by one in the concurrent collection
                String line;
                while ((line = bufferedReader.readLine()) != null) _lines.put(line);
                ConsoleOutput.printMessageWithoutGaps("Done reading lines from file: " + path);
            } catch (IOException e) {
                final String errorHeader = "Exception in reading file: " + path;
                ConsoleOutput.printIOException(errorHeader, e);
            } catch (InterruptedException e) {
                final String errorHeader = "Reader interrupted while reading.";
                ConsoleOutput.printInterruptedException(errorHeader, e);
            }
        }
    }

    //endregion
}