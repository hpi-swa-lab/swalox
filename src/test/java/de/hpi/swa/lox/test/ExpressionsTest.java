package de.hpi.swa.lox.test;

import org.junit.Test;

public class ExpressionsTest extends AbstractLoxTest {
    
    @Test
    public void testLogicalNot() {
        runAndExpect("testLogical", "print !true;", "false\n");
        runAndExpect("testLogical", "print !false;", "true\n");
    }
    
    @Test
    public void testLogicalNotError() {
        runAndExpectError("testLogical", "print !1;", "Type Error");
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
        runAndExpect("testComparison", "print 1 != 2;", "true\n");
    }

    @Test
    public void testComparisonError() {
        runAndExpectError("testComparison", "print 1 < true;", "Type Error");
        runAndExpectError("testComparison", "print 1 <= true;", "Type Error");
        runAndExpectError("testComparison", "print 1 > true;", "Type Error");
        runAndExpectError("testComparison", "print 1 >= true;", "Type Error");
        runAndExpectError("testComparison", "print 1 == true;", "Type Error");
        runAndExpectError("testComparison", "print 1 != true;", "Type Error");
    }

    @Test
    public void testPrecedence() {
        runAndExpect("testPrecedence", "print 1 + 2 * 3;", "7\n");
        runAndExpect("testPrecedence", "print 1 * 2 + 3;", "5\n");
        runAndExpect("testPrecedence", "print 1 + 2 == 3;", "true\n");
        runAndExpect("testPrecedence", "print 3 == 1 + 2;", "true\n");
    }

    @Test
    public void testExpressionGrouping() {
        runAndExpect("testExpressionGrouping", "print (1 + 2) * 3;", "9\n");
        runAndExpect("testExpressionGrouping", "print 1 + (2 * 3);", "7\n");
    }

}
