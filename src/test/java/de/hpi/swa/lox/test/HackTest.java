package de.hpi.swa.lox.test;

import org.junit.Test;


public class HackTest extends AbstractLoxTest{
    

    @Test
    public void testHack() {
        runAndExpect("hack", "hack;", "3\n4\n3\n");
    }

}
