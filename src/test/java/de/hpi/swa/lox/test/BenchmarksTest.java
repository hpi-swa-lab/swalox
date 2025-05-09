package de.hpi.swa.lox.test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import de.hpi.swa.lox.cli.LoxMain;

import java.io.IOException;


public class BenchmarksTest extends AbstractLoxTest {


    // WIP
    // @Test
    public void testSieve() throws IOException {
        // Execute the LoxMain with the temporary file
        LoxMain.main(new String[]{"examples/benchmarks/harness.lox", "sieve"});

        assertEquals("no errors", "xx", normalize(errContent.toString()));

        // assertEquals("Should print true", "true\n", normalize(outContent.toString()));
    }


}
