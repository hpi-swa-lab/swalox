package de.hpi.swa.lox.test;

import org.junit.Test;

public class ControlTest extends AbstractLoxTest {

    @Test
    public void testIfCondition() {
        runAndExpect("if true", "if(true) print 1;", "1\n");
        runAndExpect("if true", "if(true) { print 1;}", "1\n");
        runAndExpect("if false", "if(false) print 1;", "");
    }

    @Test
    public void testDebugIfCondition() {
        // runAndExpect("if true", "var a = 3; if(a < 4) print \"hello\";", "hello\n");
        // runAndExpect("if true", "if(true) print 3;", "3\n");
        runAndExpect("if false", "if(false) print 3;", "");

    }

    @Test
    public void testIfThenElseCondition() {
        runAndExpect("if true", "if(true) print 1; else print 2;", "1\n");
        runAndExpect("if false", "if(false) print 1; else print 2;", "2\n");
    }

    @Test
    public void testWhileLoop() {
        runAndExpect("print 1 2 3", "var i=0; while(i < 3) { i = i + 1; print i;}", "1\n2\n3\n");
    }

    @Test
    public void testForLoop() {
        runAndExpect("print 1 2 3", "for(var i=1; i <= 3; i = i + 1) {print i;}", "1\n2\n3\n");
    }

    @Test
    public void testScopeInForLoop() {
        runAndExpect("scope i",
                "var i = 8; for(var i=1; i <= 3; i = i + 1) {} print i;", "8\n");
    }

    @Test
    public void testEmptyInitializationForLoop() {
        runAndExpect("print 1 2 3", "var i = 1; for(;i <= 3;) { print i;i = i + 1;}", "1\n2\n3\n");
    }

    @Test
    public void testEmptyForLoop() {
        runAndExpect("print 1 2 3", """
                    fun f() {
                        var i = 1;
                        for(;;) {
                            if (i > 3) {
                                return;
                            }
                            print i;
                            i = i + 1;
                        }
                    }
                    f();
                """,
                "1\n2\n3\n");
    }

    @Test
    public void testForNonVarInitLoop() {
        runAndExpect("print 1 2 3", "var i; for(i=1; i <= 3; i = i + 1) {print i;}", "1\n2\n3\n");
    }

    @Test
    public void testShortCircuitedLogicalOperators() {
        runAndExpect("init", "fun a() { print \"a\"; return true;} fun b() { print \"b\"; return false;}", "");
        runAndExpect("a and b", "a() and b(); ", "a\nb\n");
        runAndExpect("b and a", " b() and a(); ", "b\n");
        runAndExpect("a or b", "a() or b(); ", "a\n");
        runAndExpect("b or a", "b() or a(); ", "b\na\n");
    }

}