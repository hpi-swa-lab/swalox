package de.hpi.swa.lox.test;

import org.junit.Test;

public class DatatypesTest extends AbstractLoxTest {
    @Test
    public void testString() {
        runAndExpect("testString", "print \"Hello, World!\";", "Hello, World!\n");
        runAndExpect("testString", "print \"\";", "\n");
        runAndExpect("testString", "print \"😍\";", "😍\n");
    }

    @Test
    public void testNumber() {
        runAndExpect("testNumber", "print 42;", "42\n");
        runAndExpect("testNumber", "print 2.5;", "2.5\n");
    }
    @Test
    public void testNumberToBigException() {
        runAndExpectError("testNumber", "print 92233720368547758070;", "Number is to big"); 
    }

    // @Test
    // not supporeted yet
    // public void bignumberTestNumber() {
    //     runAndExpect("testLargeNumber", "print 92233720368547758070;", "92233720368547758070\n");
    // }


    @Test
    public void testBoolean() {
        runAndExpect("testBoolean", "print true;", "true\n");
        runAndExpect("testBoolean", "print false;", "false\n");
    }

    @Test
    public void testNil() {
        runAndExpect("testNil", "print nil;", "nil\n");
    }    
}
