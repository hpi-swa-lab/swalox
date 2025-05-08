package de.hpi.swa.lox.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BigNumbersTest extends AbstractLoxTest {

    // @Test
    public void testNumberToBigException() {
        runAndExpectError("testNumber", "print 92233720368547758070;", "Number is to big");
    }

    @Test
    // not supporeted yet
    public void bignumberTestNumber() {
        runAndExpect("testLargeNumber", "print 92233720368547758070;", "92233720368547758070\n");
    }

    @Test
    public void testAddBigLeftNumber() {
        run("print 92233720368547758070 + 1;");
        assertEquals(normalize(errContent.toString()), "");
        assertEquals(normalize(outContent.toString()), "92233720368547758071\n");
    }

    @Test
    public void testAddBigRightNumber() {
        run("print 1 + 92233720368547758070;");
        assertEquals(normalize(errContent.toString()), "");
        assertEquals(normalize(outContent.toString()), "92233720368547758071\n");
    }

    @Test
    public void testAddTwoBigNumbers() {
        run("print 52233720368547758070 + 52233720368547758070;");
        assertEquals(normalize(errContent.toString()), "");
        assertEquals(normalize(outContent.toString()), "104467440737095516140\n");
    }

    @Test
    public void testAddBigResultNumber() {
        run("print 5223372036854775807 + 5223372036854775807;");
        assertEquals(normalize(errContent.toString()), "");
        assertEquals(normalize(outContent.toString()), "10446744073709551614\n");
    }

    @Test
    public void testAddSmallNumbers() {
        run("print 3 + 4;");
        assertEquals(normalize(errContent.toString()), "");
        assertEquals(normalize(outContent.toString()), "7\n");

    }

    @Test
    public void testSubtractBigLeftNumber() {
        run("print 92233720368547758070 - 1;");
        assertEquals(normalize(errContent.toString()), "");
        assertEquals(normalize(outContent.toString()), "92233720368547758069\n");
    }

    @Test
    public void testSubtractBigRightNumber() {
        run("print 0 - 92233720368547758070;");
        assertEquals(normalize(errContent.toString()), "");
        assertEquals(normalize(outContent.toString()), "-92233720368547758070\n");
    }

    @Test
    public void testMulBigLeftNumber() {
        run("print 92233720368547758070 * 10;");
        assertEquals(normalize(errContent.toString()), "");
        assertEquals(normalize(outContent.toString()), "922337203685477580700\n");
    }

    @Test
    public void testDivBigLeftNumber() {
        run("print 92233720368547758070 / 10;");
        assertEquals(normalize(errContent.toString()), "");
        assertEquals(normalize(outContent.toString()), "9.223372036854776E18\n");
    }

    @Test
    public void testMulBigLeftNumberAndDouble() {
        run("print 92233720368547758070 * 10.0;");
        assertEquals("", normalize(errContent.toString()));
        assertEquals("9.223372036854776E20\n", normalize(outContent.toString()));
    }

    @Test
    public void testDivBigLeftNumberAndDouble() {
        run("print 92233720368547758070 / 10.0;");
        assertEquals("", normalize(errContent.toString()));
        assertEquals("9.223372036854776E18\n", normalize(outContent.toString()));
    }

}
