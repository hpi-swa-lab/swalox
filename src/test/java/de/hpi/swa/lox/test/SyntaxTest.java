package de.hpi.swa.lox.test;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SyntaxTest extends AbstractLoxTest {
    
    @Test
    public void testComments() {
       
        runAndExpect("line comments", "print 1;  //print 1", "1\n");
       
    }


}
