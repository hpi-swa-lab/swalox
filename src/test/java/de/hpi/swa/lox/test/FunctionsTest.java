package de.hpi.swa.lox.test;

import org.junit.Test;

public class FunctionsTest extends AbstractLoxTest {


    @Test
    public void testFunctionDeclaration() { 
        runAndExpect("function declaration", "print 2;fun f() { print 1; }; print 3;", "2\n3\n");
    
    }

    @Test
    public void testFunctionCall() { 
        runAndExpect("function declaration", "fun f() { print 1; } f();f();", "1\n1\n" + //
                        "");
    }

    @Test
    public void testFunctionAsValues() { 
        runAndExpect("function expression", "fun x() { print 1; };  var f = x; f();", "1\n");
    }

    @Test
    public void testFunctionDeclarationWithParameter() { 
        runAndExpect("function declaration with parameter", "fun f(a) { print a; } f(1);", "1\n");
    }

    @Test
    public void testFunctionDeclarationWithTwoParameters() { 
        runAndExpect("function declaration with two parameter", "fun f(a, b) { print a + b; } f(3,4);", "7\n");
    }



    @Test
    public void testFunctionReturn() { 
        runAndExpect("function return", "fun f() { return 1; } print f();", "1\n");
    }

    @Test
    public void testFunctionDeclarationWithTwoParametersAndReturn() { 
        runAndExpect("function declaration with two parameter and return", "fun f(a, b) { return a + b; }; print f(3,4);", "7\n");
    }


    @Test
    public void testInnerFunction() { 
        runAndExpect("inner function",  
            "fun outer() { "+
            "    fun inner() { " +
            "       return 1;"+
            "    }" +
            "    return inner();"+
            "}"+ 
            "print outer();", 
            "1\n");
    }

    @Test
    public void testInnerFunctionReadsOuterVariable() { 
        runAndExpect("inner function uses outer variable",  
            "fun outer() { "+
            "    var a = 1;"+
            "    fun inner() { " +
            "       return a;"+
            "    }" +
            "    return inner();"+
            "}"+ 
            "print outer();", 
            "1\n");
    }


    @Test
    public void testInnerFunctionStoreOuterVariable() { 
        runAndExpect("inner function uses outer variable",  
            "fun outer() { "+
            "    var a = 1;"+
            "    fun inner() { " +
            "       a = 2;"+
            "       return a;"+
            "    }" +
            "    return inner();"+
            "}"+ 
            "print outer();", 
            "2\n");
    }


    // TODO: this test fails, because not clear how to check for defined variables in the materialized frame
    // @Test
    public void testInnerFunctionReadUndefinedOuterVariable() { 
        runAndExpectError("inner function uses outer variable",  
            "fun outer() { "+
            "    var a;"+
            "    fun inner() { " +
            "       return a;"+
            "    }" +
            "    return inner();"+
            "}"+ 
            "print outer();", 
            "a not defined\n");
    }


    @Test
    public void testInnerFunctionUsesOuterVariable2() { 
        runAndExpect("inner function uses outer variable",  
            "fun outer() {" +
            "  var a = 1;" +
            "  fun inner() {" +
            "      return a;" +
            "  }" +
            "  return inner;" +
            "}" +
            "var f = outer();" +
            "print f();", 
            "1\n");
    }

    @Test
    public void testFunctionReturnsFunction() { 
        runAndExpect("function returns function",  
            "fun outer() {" +
            "  fun inner() {" +
            "      return 1;" +
            "  }" +
            "  return inner;" +
            "}" +
            "print outer()();", 
            "1\n");
    }


    @Test
    public void testClosuredFunc() { 
        runAndExpect("count function",  
            "fun counter() { "+
            "    var i = 0;" + 
            "    fun count() { " +
            "       i = i + 1; " +
            "       return i;"+
            "    }" +
            "    return count;" +
            "}"+ 
            "var c = counter(); " +
            "print c(); " + 
            "print c();", 
            "1\n2\n");
    }

    @Test
    public void testPrintSum() { 
        runAndExpect("slide example",
            "fun printSum(a, b) {" +
            "  print a + b; " +
            "} " +
            "" +
            "printSum(3, 4); ", "7\n");
    }


    
}
