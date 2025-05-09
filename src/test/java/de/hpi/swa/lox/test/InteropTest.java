package de.hpi.swa.lox.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.Before;
import org.junit.Test;

public class InteropTest extends AbstractLoxTest {

    Value add;

    @Before
    public void setUp() {
        add = context.eval("lox", "fun f(a, b){ return a + b;}; return f;");
    }

    @Test
    public void testAdd() {
        assertEquals("result", add.execute(3, 4).asInt(), 7);
        assertTrue("result", add.execute(3, 4.5).asDouble() == 7.5);
        assertEquals("result", add.execute("Hello", "World").asString(), "HelloWorld");
    }

    @Test
    public void testAddErrors() {

        try {
            var r = add.execute(3, Float.class);
            fail("Expected an exception, but result was " + r);
        } catch (Exception e) {
            assertTrue("result", e instanceof PolyglotException);
        }
    }

    @Test
    public void testInteropReadProperty() {
        Value obj = context.eval("lox", "class O {} var o = O(); o.a = 4; return o;");
        Value result = obj.getMember("a");
        assertEquals("result", result.toString(), "4");
    }

    @Test
    public void testInteropAddNumbersProperty() {

        Value obj = context.eval("lox", "class O {} var o = O(); o.a = 4; return o;");
        long result = obj.getMember("a").asLong();
        assertEquals("result", result + 1, 5);
    }

    @Test
    public void testInteropWriteProperty() {
        Value obj = context.eval("lox", "class O {} var o = O(); return o;");
        obj.putMember("c", 3);
        var result = context.eval("lox", "return o.c;");
        assertEquals("result", result.toString(), "3");
    }

    public class Dummy {
        public long a = 3;
    }

    @Test
    public void testInteropWriteForeignProperty() {
        Value obj = context.eval("lox", "class O {} var o = O(); return o;");
        var d = new Dummy();
        obj.putMember("d", d);
        context.eval("lox", "o.d.a = 4;");
        assertEquals("result", d.a, 4);
    }

    @Test
    public void testCompatibilityAddStringArray() {
        try {
            add.execute("Hello", new int[] { 1, 2, 3 });
            fail("should not reach here");
        } catch (Exception e) {
            assertTrue(e instanceof PolyglotException);
        }
    }

    @Test
    public void testCompatibilityEqual() {
        Value add = context.eval("lox", "fun equal(a, b) { return a == b ; } return equal;");
        assertEquals("int int", add.execute(3, 3).asBoolean(), true);
        assertEquals("int int", add.execute((int) 3, (long) 3).asBoolean(), true);
    }

    @Test
    public void testKeyEvent() {
        this.runAndExpect("up",
                "var KeyEvent = lookup(\"java\", \"java.awt.event.KeyEvent\"); print 38 == KeyEvent.VK_UP;", "true\n");
    }

    @Test
    public void testCompatibilityAddBig() {
        assertEquals("big",
                add.execute(-32768, new BigInteger("-9223372036854775808"))
                        .asDouble() < (new BigInteger("-9223372036854775808")).doubleValue(),
                true);
    }
}