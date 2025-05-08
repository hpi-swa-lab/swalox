package de.hpi.swa.lox.test;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StatementsTest extends AbstractLoxTest {
    
    @Test
    public void testExtressionStatement() {
        run("3 + 4;");

        assertThat(errContent.toString(), is(""));
    }


    @Test
    public void testBlockStatement() {
        run("{3 + 4;}");    
        assertThat(errContent.toString(), is(""));
    }



    @Test
    public void testTwoStatement() {
        runAndExpect("two statements", "print 1; print 2;", "1\n2\n");    
    }

    @Test
    public void testTwoStatementsWithNewline() {
        runAndExpect("two statements", "print 1;\n print 2;", "1\n2\n");    
    }


    @Test
    public void testHalt() {
        runAndExpectError("halt statement", "halt;\n print 2;", "Halt\n");    
    }


}
