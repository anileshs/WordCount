package com.company;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * This class is the central point of directing the output to console. Its methods have been synchronized
 * to keep the output from multiple threads from mingling. When trying to write output to console,
 * developers should consider adding a synchronized method here instead and then calling that.
 */
class ConsoleOutput {

    //region Static Final Class Variables

    static final String NEW_LINE = System.lineSeparator();
    private static final String DETAILED_MESSAGE_TAG = "Detailed Message";
    private static final String DETAILED_CAUSE_TAG = "Detailed Cause";
    private static final String CAUSE_TAG = "Exception Cause";
    private static final String FATAL_ERROR_TAG = "FATAL ERROR";
    static final String END_MARKER = "$$$ ### --- %%% !!! @@@ ^^^";

    //endregion

    //region General Message Printers

    static synchronized void printMessageWithoutGaps(String message) {
        System.out.println(message);
    }

    static synchronized void printMessageWithGaps(String message) {
        System.out.println();
        System.out.println(message);
        System.out.println();
    }

    static synchronized void printMessageWithGapsAndLineBreaks(String message) {
        System.out.println();
        printLineBreak();
        System.out.println(message);
        printLineBreak();
        System.out.println();
    }

    //endregion

    //region Tagged Message Printers

    private static synchronized void printTaggedMessage(String tag, String message) {
        System.out.println(tag + ":");
        System.out.println(message);
    }

    private static synchronized void printDetailedInterruptionCause(String message) {
        printTaggedMessage(DETAILED_CAUSE_TAG, message);
    }

    private static synchronized void printDetailedExceptionMessage(String message) {
        printTaggedMessage(DETAILED_MESSAGE_TAG, message);
    }

    private static synchronized void printExceptionWithCause(String exceptionCause) {
        printTaggedMessage(CAUSE_TAG, exceptionCause);
    }

    //endregion

    //region Exception Printers

    static synchronized void printIOException(String errorHeader, IOException e) {
        printLineBreak();
        System.out.println(errorHeader);
        printDetailedExceptionMessage(e.getMessage());
        printLineBreak();
        System.out.println();
    }

    static synchronized void printInterruptedException(String errorHeader, InterruptedException e) {
        printLineBreak();
        System.out.println(errorHeader);
        printDetailedInterruptionCause(e.getCause().toString());
        System.out.println();
        printDetailedExceptionMessage(e.getMessage());
        System.out.println();
        printLineBreak();
    }

    static synchronized void printExecutionException(String errorHeader, Throwable cause, ExecutionException e) {
        printLineBreak();
        System.out.println(errorHeader);
        printExceptionWithCause(cause.getMessage());
        System.out.println();
        printDetailedExceptionMessage(e.getMessage());
        System.out.println();
        printLineBreak();
    }

    //endregion

    //region Other Case Specific Printers

    private static synchronized void printLineBreak() {
        System.out.println("----------------------------------");
    }

    static synchronized void blockPrintMap(HashMap<String, Long> map) {
        printLineBreak();
        printMessageWithGaps("Merged Word Count:");
        for (Map.Entry<String, Long> mapEntry : map.entrySet()) {
            printMessageWithoutGaps("{'" + mapEntry.getKey() + "': " + mapEntry.getValue() + "}");
        }
        printLineBreak();
    }

    //endregion
}
