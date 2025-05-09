package de.hpi.swa.lox.test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import de.hpi.swa.lox.cli.LoxMain;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class MainTest extends AbstractLoxTest {

    @Test
    public void testCommand() {
        // capture system out
        LoxMain.main(new String[]{"-c", "print true;"});

        assertEquals("Should print true", "true\n", normalize(outContent.toString()));
    }


    @Test
    public void testEvaluateExpression() {
        // capture system out
        var main = new LoxMain();

        main.evaluateExpression(context, "3 + 4;");

        assertEquals("Should print 7", "7\n", normalize(outContent.toString()));
    }

    @Test
    public void testCommandPrintsErrors() {
        // capture system out
        LoxMain.main(new String[]{"-c", "print 1 + true;"});

        assertThat( normalize(errContent.toString()), containsString("Error"));
    }

    @Test
    public void testFile() throws IOException {
        // Create a temporary file
        File tempFile = File.createTempFile("testFile", ".lox");
        tempFile.deleteOnExit();

        // Write to the temporary file
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("print true;");
        }

        // Execute the LoxMain with the temporary file
        LoxMain.main(new String[]{tempFile.getAbsolutePath()});

        // Verify the output
        assertEquals("Should print true", "true\n", normalize(outContent.toString()));
    }


    @Test
    public void testFilePrintsError() throws IOException {
        // Create a temporary file
        File tempFile = File.createTempFile("testFile", ".lox");
        tempFile.deleteOnExit();

        // Write to the temporary file
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("print 1 + true;");
        }

        // Execute the LoxMain with the temporary file
        LoxMain.main(new String[]{tempFile.getAbsolutePath()});

        assertThat( normalize(errContent.toString()), containsString("Error"));
    }

}
