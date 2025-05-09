package de.hpi.swa.lox.test;

import org.junit.Test;

public class BounceTest extends AbstractLoxTest {

    // Test
    public void testBounce() {
        runAndExpect("bounce ", "load(\"examples/bounce.lox\");", "");
    }

}