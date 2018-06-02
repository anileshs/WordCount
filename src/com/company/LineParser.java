package com.company;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

/**
 * The runnable class to parse a line at a time from a blocking queue passed in the constructor.
 * The result of parsing is stored in a local hash map which is accessible from the getter.
 */
class LineParser implements Callable<HashMap<String, Long>> {

    private final ArrayBlockingQueue<String> _lines;
    private final HashMap<String, Long> _result = new HashMap<>();

    /**
     * The constructor of LineParser which takes in one parameter.
     *
     * @param lines an ArrayBlockingQueue of String.
     *              This is the buffer from which the LineParser reads the lines to parse.
     */
    public LineParser(ArrayBlockingQueue<String> lines) {
        if (lines == null) throw new IllegalArgumentException("The input line buffer cannot be null.");
        _lines = lines;
    }

    /**
     * The method to break a line into individual words and collect locally.
     *
     * @param line a String
     *             the string which is parsed and word counted.
     */
    private void parseLine(String line) {
        //String[] words = line.split("\\P{L}+");
        String[] words = line.split("\\W+");
        for (String word : words) {
            word = word.toLowerCase();
            if (_result.containsKey(word)) {
                _result.put(word, _result.get(word) + 1);
            } else {
                _result.put(word, 1l);
            }
        }
    }

    @Override
    public HashMap<String, Long> call() throws Exception {
        //The parser simply needs to read from the concurrent collection and process each line.
        String line;
        while (!_lines.isEmpty()){
            try {
                line = _lines.take();
                if (line.equals(null)) {
                    //If null is encountered, the collection is done.
                    //Put the null back for other threads to read and break out of their while loop.
                    _lines.put(null);
                    break;
                }
            } catch (InterruptedException e) {
                throw e;
            }
            parseLine(line);
        }
        return _result;
    }
}