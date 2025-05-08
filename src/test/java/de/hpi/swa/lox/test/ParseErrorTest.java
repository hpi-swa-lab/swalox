package de.hpi.swa.lox.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.graalvm.polyglot.PolyglotException;

import org.junit.Test;

public class ParseErrorTest extends AbstractLoxTest {

    @Test
    public void testParseError() {
        try {
            context.eval("lox", "print 3+;");
            assertTrue("Should not reach here.", false);
        } catch (PolyglotException e) {
            assertTrue("Should be a syntax error.", e.isSyntaxError());
            assertNotNull("Should have source section.", e.getSourceLocation());
        }
    }

    
    @Test
    public void testPrintSyntaxError() {
        runAndExpectError("print error label", "print 3+;", "Error");
    }


}

