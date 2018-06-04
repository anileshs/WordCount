package com.company;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test class for DiskFileReader class.
 */
class DiskFileReaderTest {

    //Other tests that could be added to the suite:
    //1. Test with more than just one file
    //2. Test with empty file

    @Test
    void instantiationShouldThrowWithNullLines() {
        final BlockingQueue<String> lines = null;
        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> new DiskFileReader(new String[0], lines));
        assertEquals("'lines' cannot be null.", illegalArgumentException.getMessage());
    }

    @Test
    void instantiationShouldThrowWithNullPaths() {
        final String[] filePath = null;
        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> new DiskFileReader(filePath, new LinkedBlockingQueue<>()));
        assertEquals("'filePath' cannot be null.", illegalArgumentException.getMessage());
    }

    @Test
    void linesShouldContainEndMarkerAsTheLastElement() {
        final LinkedBlockingQueue<String> lines = new LinkedBlockingQueue<>();
        DiskFileReader dfr = new DiskFileReader(new String[0], lines);
        dfr.run();
        assertSame(ConsoleOutput.END_MARKER, lines.peek());
    }

    @Test
    void linesShouldContainEachNonNullLineOfAnInputFile() throws IOException, InterruptedException {

        //region Arrange

        final BlockingQueue<String> linesFromDiskFileReader = new LinkedBlockingQueue<>();
        final String sep = File.separator;
        final String pathToTextFile = new File(".").getAbsolutePath()
                + sep + "src"
                + sep + "test"
                + sep + "resources"
                + sep + "TestData"
                + sep + "hamlet.txt";
        final String filePath = new File(pathToTextFile).getPath();
        List<String> expectedLines = Files.readAllLines(Paths.get(filePath));
        expectedLines.add(ConsoleOutput.END_MARKER);

        //endregion

        //region Act

        Thread reader = new Thread(new DiskFileReader(new String[]{filePath}, linesFromDiskFileReader));
        reader.start();
        reader.join();
        List<String> actualLines = new LinkedList<>();
        linesFromDiskFileReader.drainTo(actualLines);

        //endregion

        assertLinesMatch(expectedLines, actualLines);
    }
}