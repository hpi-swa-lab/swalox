package de.hpi.swa.lox.test;

import org.junit.Test;


public class StringTest extends AbstractLoxTest {
    
    @Test
    public void testConcatStrings() {
        runAndExpect("concat strings ", "print \"hello \" + \"world\";", "hello world\n");
    }


}