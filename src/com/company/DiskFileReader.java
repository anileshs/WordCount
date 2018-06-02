package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

class DiskFileReader implements Runnable {

    private final String[] _filePath;
    private final ArrayBlockingQueue<String> _lines;
    private static final String DETAILED_MESSAGE_TAG = "Detailed Message";
    private static final String DETAILED_CAUSE_TAG = "Detailed Cause";
    private static final String FATAL_ERROR_TAG = "FATAL ERROR";

    DiskFileReader(String[] filePath, ArrayBlockingQueue<String> lines) {
        _filePath = filePath;
        _lines = lines;
    }

    @Override
    public void run() {
        populateLines();
        markBlockingQueueAsDone();
    }

    private void markBlockingQueueAsDone(){
        //To tell that the reading is done, we add a null value at the end.
        try {
            _lines.put(null);
        } catch (InterruptedException e) {
            String message = "Failed to mark the buffer as done. Kill the program manually.";
            printMessage(FATAL_ERROR_TAG, message);
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
            } catch (IOException e) {
                reportIOException(e, path);
            } catch (InterruptedException e) {
                reportInterruptedException(e);
            }
        }
    }

    private void reportInterruptedException(InterruptedException exception) {
        printLineBreak();
        System.out.println("Reader interrupted while reading.");
        printMessage(DETAILED_CAUSE_TAG, exception.getCause().toString());
        System.out.println();
        printMessage(DETAILED_MESSAGE_TAG, exception.getMessage());
        printLineBreak();
        System.out.println();
    }

    private void printMessage(String tag, String message) {
        System.out.println(tag + ":");
        System.out.println(message);
    }

    private void reportIOException(IOException exception, String path) {
        printLineBreak();
        System.out.println("Exception in reading file: " + path);
        printMessage(DETAILED_MESSAGE_TAG, exception.getMessage());
        printLineBreak();
        System.out.println();
    }

    private void printLineBreak() {
        System.out.println("----------------------------------");
    }
}