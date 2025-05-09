package de.hpi.swa.lox.test;

import org.junit.Test;

public class TruthynessTest extends AbstractLoxTest {
    
    @Test
    public void testTruthyness() {
        runAndExpect("true is true", "print true and true;", "true\n");
        runAndExpect("true is false", "print true and false;", "false\n");

        runAndExpect("1 is true", "print true and 1;", "true\n");
        runAndExpect("0 is false", "print true and 0;", "false\n");

        runAndExpect("nil is false", "print true and nil;", "false\n");
        
        runAndExpect("objects are true", "class O{} var o = O();  print true and o;", "true\n");
  
    }
}
