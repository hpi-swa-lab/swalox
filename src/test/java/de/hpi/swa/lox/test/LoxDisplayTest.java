package de.hpi.swa.lox.test;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Test;

import de.hpi.swa.lox.runtime.LoxDisplay;

public class LoxDisplayTest extends AbstractLoxTest {

    LoxDisplay display;

    @Test
    public void testLookupLoxDisplay() {
        runAndExpect("string", " var d = lookup(\"java\", \"de.hpi.swa.lox.runtime.LoxDisplay\"); print d;",
                "JavaClass[de.hpi.swa.lox.runtime.LoxDisplay]\n");
    }

    @Test
    public void testCreateLoxDisplay() {
        this.display = LoxDisplay.create();
        assertNotNull(this.display.frame);
    }

    @After
    public void tearDown() {
        if (display != null && display.frame != null) {
            display.frame.dispose();
        }
    }

}
