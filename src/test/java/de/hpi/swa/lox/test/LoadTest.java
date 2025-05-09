package de.hpi.swa.lox.test;

import org.junit.Test;

public class LoadTest extends AbstractLoxTest {

    @Test
    public void testLoadFileWithClasss() {
        runAndExpect("load", "load(\"examples/tests/a.lox\"); print A;", "Class A\n" );
    }

    @Test
    public void testLoadRelativeFileLoading() {
        runAndExpect("load", "load(\"examples/tests/b.lox\"); print B;", "Class A\n" );
    }

}

