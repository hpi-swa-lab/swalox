package de.hpi.swa.lox.test;

import org.junit.Test;


public class VariablesTest extends AbstractLoxTest{
    
    // @Test
    // public void testVariableDeclaration() {
    //     runAndExpect("assignment and usage", "var a = 3; print a;", "3\n");
    // }   

    // @Test
    // public void testUndefinedVariableUsageError() {
    //     runAndExpectError("variable usage", "print a;", "not defined\n");
    // }


    // @Test
    // public void testReadGlobalVariable() {

    //     runAndExpect("read undefined variable", "print a;", "nil\n");
    // }


    // @Test
    // public void testReadScopedVariable() {
    //     runAndExpect("read undefined variable", "print 1; {var a = 3; print a;}", "3\n");
    // }


    @Test
    public void testWriteAndReadVariable() {
        runAndExpect("read write and read variable", "var a = 3; print a;", "3\n");
    }

    @Test
    public void testReadUndefinedLocalVariable() {
        runAndExpectError("read undefined variable", "{var a; print a;}", "not defined");
    }

    @Test
    public void testReadUndeclaredLocalVariable() {
        runAndExpectError("read undefined variable", "{print a;}", "not declared");
    }

    @Test
    public void testReadUndefinedGlobalVariable() {
        runAndExpectError("read undefined variable", "var a; print a;", "not defined");
    }
    
    @Test
    public void testReadUndeclaredVariable() {
        runAndExpectError("read undeclared variable", "print a;", "not declared");
    }

    @Test
    public void testWriteUndeclaredGlobalVariable() {
        runAndExpectError("write undeclared global variable", "a = 3;", "not declared");
    }


    @Test
    public void testBlockWriteAndReadVariable() {
        runAndExpect("read write and read variable", "{var a = 4; print a;}", "4\n");
    }

    @Test
    public void testShadowedWriteAndReadVariable() {
        runAndExpect("read write and read variable", "var a = 3; print a; {var a = 4; print a;} print a;", "3\n4\n3\n");
    }

    @Test
    public void testVariableAssignment() {
        runAndExpect("read write and read variable", "var a = 4; a=2; print a;", "2\n");
    }

    // @Test
    // public void testReadUnitializedVariable() {
    //     runAndExpect("read nil", "var a; print a;", "nil\n");
    // }


}
