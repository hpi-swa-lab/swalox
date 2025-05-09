package de.hpi.swa.lox.test;

import org.junit.Test;

public class ExpressionsTest extends AbstractLoxTest {

    @Test
    public void testLogicalNot() {
        runAndExpect("testLogical", "print !true;", "false\n");
        runAndExpect("testLogical", "print !false;", "true\n");
    }

    @Test
    public void testUnaryMinus() {
        runAndExpect("testNegation", "print -1;", "-1\n");
        runAndExpect("testNegation", "print -1.5;", "-1.5\n");
    }

    @Test
    public void testUnaryMinusError() {
        runAndExpectError("testNegation", "print -true;", "Type Error");
    }

    @Test
    public void testLogicaOr() {
        runAndExpect("testLogicalOr", "print true or true;", "true\n");
        runAndExpect("testLogicalOr", "print true or false;", "true\n");
        runAndExpect("testLogicalOr", "print false or true;", "true\n");
        runAndExpect("testLogicalOr", "print false or false;", "false\n");
    }

    @Test
    public void testLogicalOrError() {
        runAndExpectError("testLogicalOr", "print true or 1;", "Type Error");
    }

    @Test
    public void testLogicalAnd() {
        runAndExpect("testLogical", "print true and true;", "true\n");
        runAndExpect("testLogical", "print true and false;", "false\n");
        runAndExpect("testLogical", "print false and true;", "false\n");
        runAndExpect("testLogical", "print false and false;", "false\n");
    }

    @Test
    public void testLogicalAndError() {
        runAndExpectError("testLogicalAnd", "print true and 1;", "Type Error");
    }

    @Test
    public void testArithmetic() {
        runAndExpect("testArithmetic", "print 1 + 2;", "3\n");
        runAndExpect("testArithmetic", "print 1 + 2 + 3 + 4 + 5;", "15\n");
        runAndExpect("testArithmetic", "print 1 - 2;", "-1\n");
        runAndExpect("testArithmetic", "print 1 * 2;", "2\n");
        runAndExpect("testArithmetic", "print 1 / 2;", "0.5\n");
    }

    @Test
    public void testFloatArithmetic() {
        runAndExpect("testArithmetic", "print 1.0 + 2.0;", "3.0\n");
        runAndExpect("testArithmetic", "print 2.5 - 1;", "1.5\n");
        runAndExpect("testArithmetic", "print 2 * 2.5;", "5.0\n");
        runAndExpect("testArithmetic", "print 1 / 2;", "0.5\n");
    }

    @Test
    public void testArithmeticError() {
        runAndExpectError("testArithmetic", "print 1 + true;", "Type Error");
        runAndExpectError("testArithmetic", "print 1 - true;", "Type Error");
        runAndExpectError("testArithmetic", "print 1 * true;", "Type Error");
        runAndExpectError("testArithmetic", "print 1 / true;", "Type Error");
    }

    @Test
    public void testComparison() {
        runAndExpect("testComparison", "print 1 < 2;", "true\n");
        runAndExpect("testComparison", "print 1 <= 2;", "true\n");
        runAndExpect("testComparison", "print 1 > 2;", "false\n");
        runAndExpect("testComparison", "print 1 >= 2;", "false\n");
        runAndExpect("testComparison", "print 1 == 2;", "false\n");
        runAndExpect("testComparison", "print 1 == 1;", "true\n");
        runAndExpect("testComparison", "print 1 != 2;", "true\n");
        runAndExpect("testComparison", "print 1 != 1;", "false\n");
    }

    @Test
    public void testFloatComparison() {
        runAndExpect("testComparison", "print 1.0 < 2;", "true\n");
        runAndExpect("testComparison", "print 1 < 2.0;", "true\n");
        runAndExpect("testComparison", "print 1.0 < 2.0;", "true\n");

        runAndExpect("testComparison", "print 1.0 <= 2;", "true\n");
        runAndExpect("testComparison", "print 1 <= 2.0;", "true\n");
        runAndExpect("testComparison", "print 1.0 <= 2.0;", "true\n");

        runAndExpect("testComparison", "print 1.0 > 2;", "false\n");
        runAndExpect("testComparison", "print 1 > 2.0;", "false\n");
        runAndExpect("testComparison", "print 1.0 > 2.0;", "false\n");

        runAndExpect("testComparison", "print 1.0 >= 2;", "false\n");
        runAndExpect("testComparison", "print 1 >= 2.0;", "false\n");
        runAndExpect("testComparison", "print 1.0 >= 2.0;", "false\n");

        runAndExpect("testComparison", "print 1.0 == 2;", "false\n");
        runAndExpect("testComparison", "print 1 == 2.0;", "false\n");
        runAndExpect("testComparison", "print 1.0 == 2.0;", "false\n");

        runAndExpect("testComparison", "print 1.0 != 2;", "true\n");
        runAndExpect("testComparison", "print 1 != 2.0;", "true\n");
        runAndExpect("testComparison", "print 1.0 != 2.0;", "true\n");

        runAndExpect("testComparison", "print 1.0 == 1.0;", "true\n");
    }

    // @Test
    // public void testStringComparison() {
    // runAndExpect("testComparison", "print \"a\" == \"a\";", "true\n");
    // runAndExpect("testComparison", "print \"a\" != \"b\";", "true\n");
    // runAndExpect("testComparison", "print \"a\" < \"b\";", "true\n");
    // runAndExpect("testComparison", "print \"a\" <= \"b\";", "true\n");
    // runAndExpect("testComparison", "print \"a\" > \"b\";", "false\n");
    // runAndExpect("testComparison", "print \"a\" >= \"b\";", "false\n");
    // runAndExpect("testComparison", "print \"a\" == \"b\";", "false\n");
    // }

    @Test
    public void testComparisonError() {
        runAndExpectError("testComparison", "print 1 < true;", "Type Error");
        runAndExpectError("testComparison", "print 1 <= true;", "Type Error");
        runAndExpectError("testComparison", "print 1 > true;", "Type Error");
        runAndExpectError("testComparison", "print 1 >= true;", "Type Error");
        runAndExpect("testComparison", "print 1 == true;", "false\n");  
        runAndExpectError("testComparison", "print 1 != true;", "Type Error");
    }

    @Test
    public void testPrecedence() {
        runAndExpect("testPrecedence", "print 1 + 2 * 3;", "7\n");
        runAndExpect("testPrecedence", "print 1 * 2 + 3;", "5\n");
        runAndExpect("testPrecedence", "print 1 + 2 == 3;", "true\n");
        runAndExpect("testPrecedence", "print 3 == 1 + 2;", "true\n");

        runAndExpect("testPrecedence", "print 1 + 6 / 3;", "3.0\n");
        runAndExpect("testPrecedence", "print 6 / 3 + 1;", "3.0\n");

    }

    @Test
    public void testExpressionGrouping() {
        runAndExpect("testExpressionGrouping", "print (1 + 2) * 3;", "9\n");
        runAndExpect("testExpressionGrouping", "print 1 + (2 * 3);", "7\n");
    }

    // for debugging
    // e.g. nodes.getFirst().dump()
    @Test
    public void testAddExpressions() {
        run("print 2 * (3 + 4);");
    }


    @Test
    public void testAssignmentExpressionValue() {
        runAndExpect("testExpressionGrouping", "var a; print a = 3;", "3\n");

    }


    @Test
    public void testTruthiness() {
        runAndExpect("testNilIsFalse", "print !nil;", "true\n");
    }

    @Test
    public void testTruthinessCondition() {
        runAndExpect("testNumbersAreTrue", "if(1) print 3;", "3\n");
        runAndExpect("testStringsAreTrue", "if(\"hello\") print 3;", "3\n");
        runAndExpect("testNilIsFalseInCondtion", "if(nil) print 3;", "");
        runAndExpect("testNilIsFalseInCondtion", "if(nil) print 3; else print 4;", "4\n");
    }

    @Test
    public void testTruthinessWhile() {
        runAndExpect("testNumbersAreTrue", "var a = true; while(a) { print a; a = nil;}", "true\n");
        
    }

    @Test
    public void testTruthinessFor() {
        runAndExpect("testNumbersAreTrue", "for(var a=true; a ; a = nil) { print a;}", "true\n");
        
    }


}
