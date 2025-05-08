package de.hpi.swa.lox.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.graalvm.polyglot.PolyglotException;

import org.junit.Test;


public class RuntimeErrorTest extends AbstractLoxTest {

    @Test
    public void testRuntimeError() {
        try {
            context.eval("lox", "print true  < 4;");
            assertTrue("Should not reach here.", false);
        } catch (PolyglotException e) {
            assertTrue("Should be a runtime error.", e.isGuestException());
            var sourceLocation = e.getSourceLocation();
            assertNotNull("Should have source section.", sourceLocation);
            assertEquals("Should have a line number.", sourceLocation.getStartLine(), 1);
            assertEquals("Should have a column number.", sourceLocation.getStartColumn(), 7);
        }
    }

    @Test
    public void testPrintRuntimeError() {
        runAndExpectError("print error label", "print 3 < true;", "Error:");
    }


}
