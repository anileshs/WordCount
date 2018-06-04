package com.company;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for Main.
 * <p>
 * Since the tests in this class will exercise the entire functionality, these
 * tests are not unit tests but component/regression/system/functional tests.
 * <p>
 * The tests in this class should not just be automated-verified, but also manually
 * verified that they print relevant message on console.
 */
class MainTest {

    //Since this class contains functional tests, there are clearly more tests possible
    //than the three tests written below.
    //
    //1. Tests about concurrent scenarios, where threads terminate abruptly;
    //2. Tests about race conditions;
    //3. Tests about deadlocks;
    //4. Tests about live-locks;
    //
    //Those tests would require adding Concurrent Testing libraries.
    //So logging that information and skipping them.

    @Test
    void resultShouldBeEmptyForInvalidInput() {
        assertThrows(Exception.class, () -> Main.main(new String[]{}));
        //Verify validation message and no result output on the screen
    }

    @Test
    void resultShouldPrintMapForSampleText() {
        final String sep = File.separator;
        final String pathToTextFile = new File(".").getAbsolutePath()
                + sep + "src"
                + sep + "test"
                + sep + "resources"
                + sep + "TestData"
                + sep + "problemText.txt";
        try {
            Main.main(new String[]{pathToTextFile});
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Verify validation message and no result output on the screen
        //The resulting map should be:
        //{
        //    “I”: 1,
        //    “like”: 2,
        //    “dogs”: 2,
        //    “are”: 2,
        //    “cute”: 1,
        //    “these”: 1,
        //    “things”: 1,
        //    “the”: 1,
        //    “others”: 1
        //}
    }

    @Test
    void resultShouldBeEmptyForBadFilePaths() {
        assertThrows(Exception.class, () -> Main.main(new String[]{"bad File Path @#$#@%#$"}));
        //Verify validation message and no result output on the screen
    }

}