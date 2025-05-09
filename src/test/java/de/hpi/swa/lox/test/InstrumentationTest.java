package de.hpi.swa.lox.test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import de.hpi.swa.lox.cli.LoxMain;

public class InstrumentationTest extends AbstractLoxTest {

    static private String fib_code = """
            fun fib(n) {
                if (n <= 1) {
                    return n;
                }
                return fib(n - 1) + fib(n - 2);
                }
            print fib(30);
                """;

    @Test
    public void testRunFib() {
        LoxMain.main(new String[] { "-c", fib_code });
        assertEquals("Should calc fib", "832040\n", normalize(outContent.toString()));
    }

    @Test
    public void testTraceFib() {
        LoxMain.main(new String[] { "--cputracer", "--cputracer.TraceCalls", "-c", fib_code });
        var result = outContent.toString().contains("fib");
        assertEquals("Should contain fib", true, result);
    }

}