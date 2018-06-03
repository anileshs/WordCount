package com.company;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * The callable class to parse one line at a time from a blocking queue passed in the constructor.
 * The result of parsing is stored in a local hash map.
 */
public class LineParser implements Callable<HashMap<String, Long>> {

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

        /*

         HACK: I'm aware that in the method below, there is the possibility that the reader thread
         terminates without adding the END_MARKER in the blockingQueue, thereby leaving the
         lineParsers in an infinite loop.

         Ideally,

          EITHER:

           There should be some scope for lineParsers to know if the reader thread is alive and
           properly working.

          OR:

           We could check in the Main whether the lineParsers have been idle-waiting. If they are,
           we know there's something wrong.

          OR:

           When the reader thread quits unexpectedly, then in the catch block where the exception
           is caught, we should also terminate the lineParsers.

         ---------------------------

         For now, my patch for this issue is in the 'waitForParsers(ExecutorService executor)' method
         where I await termination of lineParsers in a while loop. If we are waiting too long, we know
         that a manual intervention is needed due to lineParsers not terminating for some reason.
         */

        String line = "";//Line is set empty to quiet the compiler error about possible uninitialized line.
        while (true) {
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
            addWordsToMap(parse(line));
        }
        return _result;
    }

    //region Line Parsing

    /**
     * I think the two methods below are a good candidate for extraction to a separate
     * 'Parser' class. I'm leaving them here for now because of time constraints.
     * <p>
     * Reasons:
     * 1. Parsing is a clear and separate responsibility. The two classes would be
     * following the 'S' of SOLID principles.
     * 2. In the separate class, these two methods would become public and be testable.
     * 3. We could change the delimiterRegex to something else for 'accentuated words' OR
     * 'another encoding', and quickly test if the new regex is working for lines that
     * were being properly parsed earlier.
     */

    private void addWordsToMap(String[] words) {
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

    private String[] parse(String line) {
        return line.split(NON_WORD_GREEDY_DELIMITER_REGEX);
    }

    //endregion

    //endregion
}