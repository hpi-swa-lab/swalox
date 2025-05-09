package de.hpi.swa.lox.test;

import org.junit.Test;

public class ArrayTest extends AbstractLoxTest {

    @Test
    public void testNewArray() {
        runAndExpect("new array", "var a = []; print a;", "[]\n");
    }


    @Test
    public void testGetNil() {
        runAndExpect("get nil", "var a = []; print a[1];", "nil\n");
    }

    @Test
    public void testSetArray() {
        runAndExpect("set a[0]=3", "var a = []; a[0]=3; print a[0];", "3\n");
    }


    @Test
    public void testSetNegative() {
        runAndExpectError("set a[-1]=3", "var a = []; a[-1]=3;", "Type Error");
    }


    @Test
    public void testPrintArray() {
        runAndExpect("print contents", "var a = []; a[0]=3; a[1]=\"hello\"; print a;", "[3, \"hello\"]\n");
    }

    @Test
    public void testBigGrowArray() {
        runAndExpect("print contents", "var a = []; a[100]=42; print a[100];", "42\n");
    }

    @Test
    public void testArrayLength() {
        runAndExpect("print length", "var a = []; a[10]=1; print a.length;", "11\n");
    }

    @Test
    public void testArraysAsObjectProperties() {
        runAndExpect("arrays as properties", 
            "class O {} \n" +
            "var o = O();\n" +
            "o.p = [];\n" + 
            "o.p[0] = 3;\n" + 
            "print o.p[0];", "3\n");
    }



    @Test
    public void testMultiArrarys() {
        runAndExpect("multi arrays", 
            "var a = [];" +
            "a[0] = [];\n" +
            "a[0][0] = 3;\n" + 
            "print a[0][0];", "3\n");
    }



}