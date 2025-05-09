package de.hpi.swa.lox.test;

import org.junit.Test;

public class SyntaxTest extends AbstractLoxTest {
    
    @Test
    public void testComments() {
       
        runAndExpect("line comments", "print 1;  //print 1", "1\n");
       
    }


}
