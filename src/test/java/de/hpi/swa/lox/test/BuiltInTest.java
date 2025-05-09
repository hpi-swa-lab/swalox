package de.hpi.swa.lox.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class BuiltInTest extends AbstractLoxTest {
    
    @Test
    public void testClock() {
        run("print clock();");
        String output = this.outContent.toString().trim();
        assertTrue("Expected a number but got: " + output, output.matches("[0-9.E]+"));
    }

    @Test
    public void testNumberInt() {
        runAndExpect("parse number", 
            "print 3 + Number(\"4\");",  "7\n");
    }

    @Test
    public void testNumberDouble() {
        runAndExpect("parse double", 
            "print 1 + Number(\"0.5\");",  "1.5\n");
    }


    @Test
    public void testStringConversion() {
        runAndExpect("convert number to string", 
            "print \"Hello\" + String(3);",  "Hello3\n");
    }


}