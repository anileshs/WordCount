package com.company;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test class for LineParser class.
 */
class LineParserTest {

    @Test
    void lineParserShouldThrowForNullLines() {
        BlockingQueue<String> lines = null;
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> new LineParser(lines));
        assertEquals("The input line buffer cannot be null.", illegalArgumentException.getMessage());
    }

    @Test
    void resultShouldBeEmptyForEmptyLines() {

        //region Arrange

        BlockingQueue<String> lines = new LinkedBlockingQueue<>();
        lines.add(ConsoleOutput.END_MARKER);

        //endregion

        //region Act

        HashMap<String, Long> result = new LineParser(lines).call();

        //endregion

        assertEquals(0, result.size());
    }

    @Test
    void verifyResultForProblemDescriptionSample() {

        //region Arrange

        BlockingQueue<String> lines = new LinkedBlockingQueue<>();
        lines.add("I like dogs. Dogs are cute.");
        lines.add("Are these things like the others?");
        lines.add(ConsoleOutput.END_MARKER);

        //endregion

        //region Act

        HashMap<String, Long> result = new LineParser(lines).call();

        //endregion

        //region Assert

        assertEquals(9, result.size());
        assertEquals(1L, result.get("i").longValue());
        assertEquals(2L, result.get("like").longValue());
        assertEquals(2L, result.get("dogs").longValue());
        assertEquals(2L, result.get("are").longValue());
        assertEquals(1L, result.get("cute").longValue());
        assertEquals(1L, result.get("these").longValue());
        assertEquals(1L, result.get("things").longValue());
        assertEquals(1L, result.get("the").longValue());
        assertEquals(1L, result.get("others").longValue());

        //endregion
    }
}