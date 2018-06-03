package com.company;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * The callable class to parse one line at a time from a blocking queue passed in the constructor.
 * The result of parsing is stored in a local hash map.
 */
class LineParser implements Callable<HashMap<String, Long>> {

    //region Static Final Class Variables and Collections

    private static final String NON_WORD_GREEDY_DELIMITER_REGEX = "\\W+";
    private final BlockingQueue<String> _lines;
    private final HashMap<String, Long> _result = new HashMap<>();

    //endregion

    //region Constructor

    /**
     * The constructor of LineParser which takes in one parameter.
     *
     * @param lines an ArrayBlockingQueue of String.
     *              This is the buffer from which the LineParser reads the lines to parse.
     */
    LineParser(BlockingQueue<String> lines) {
        if (lines == null) throw new IllegalArgumentException("The input line buffer cannot be null.");
        _lines = lines;
    }

    //endregion

    //region Call method implementation

    @Override
    public HashMap<String, Long> call() {
        //The parser simply needs to read from the concurrent collection and process each line.
        String line = "";//Line is set empty to quiet the compiler error about possible uninitialized line.
        while (!_lines.isEmpty()){
            try {
                line = _lines.take();
                if (line.equals(ConsoleOutput.END_MARKER)) {
                    //If END_MARKER is encountered, the collection is done.
                    //Put the END_MARKER back for other threads to read and break out of their while loop.
                    _lines.put(ConsoleOutput.END_MARKER);
                    break;
                }
            } catch (InterruptedException e) {
                final String errorHeader = "Parser thread interrupted while waiting for lines to parse.";
                ConsoleOutput.printInterruptedException(errorHeader, e);
            }
            parseLine(line);
        }
        return _result;
    }

    /**
     * The method to break a line into individual words and collect locally.
     *
     * @param line a String
     *             the string which is parsed and word counted.
     */
    private void parseLine(String line) {
        String[] words = line.split(NON_WORD_GREEDY_DELIMITER_REGEX);
        for (String word : words) {
            //Skip empty words
            if (word.equals("")) continue;

            //Convert word to lowercase
            word = word.toLowerCase();

            //Update the local word count
            if (!_result.containsKey(word)) _result.put(word, 0L);
            _result.put(word, _result.get(word) + 1);
        }
    }

    //endregion
}