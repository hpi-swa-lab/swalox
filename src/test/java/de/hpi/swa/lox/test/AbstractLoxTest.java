
/*
* Copyright (c) 2024, 2024, Oracle and/or its affiliates. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* The Universal Permissive License (UPL), Version 1.0
*
* Subject to the condition set forth below, permission is hereby granted to any
* person obtaining a copy of this software, associated documentation and/or
* data (collectively the "Software"), free of charge and under any and all
* copyright rights in the Software, and any and all patent rights owned or
* freely licensable by each licensor hereunder covering either (i) the
* unmodified Software as contributed to or provided by such licensor, or (ii)
* the Larger Works (as defined below), to deal in both
*
* (a) the Software, and
*
* (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
* one is included with the Software each a "Larger Work" to which the Software
* is contributed by such licensors),
*
* without restriction, including without limitation the rights to copy, create
* derivative works of, display, perform, and distribute the Software and make,
* use, sell, offer for sale, import, export, have made, and have sold the
* Software and the Larger Work(s), and to sublicense the foregoing rights on
* either these or other terms.
*
* This license is subject to the following condition:
*
* The above copyright notice and either this complete permission notice or at a
* minimum a reference to the UPL must be included in all copies or substantial
* portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package de.hpi.swa.lox.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.junit.After;
import org.junit.Before;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import de.hpi.swa.lox.cli.LoxMain;

public abstract class AbstractLoxTest {

    protected ByteArrayOutputStream outContent;
    protected PrintStream originalOut;

    protected ByteArrayOutputStream errContent;
    protected PrintStream originalErr;

    protected Context context;

    @Before
    public void initContext() {
        context = Context.newBuilder().build();
    }

    @After
    public void disposeContext() {
        context.close();
    }

    @Before
    public void captureOut() {
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        errContent = new ByteArrayOutputStream();
        originalErr = System.err;
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreOut() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    protected String normalize(String s) {
        return s.replace("\r\n", "\n");
    }

    protected void eval(String command) {
        context.eval("lox", command);
    }

    protected void run(String command) {
        try {
            eval(command);
        } catch (PolyglotException ex) {
            if (!ex.isInternalError()) {
                LoxMain.printException(ex);
            } else {
                // we print all eceptions so we can test for them
                LoxMain.printException(ex);
            }
        }
    }

    protected void runAndExpect(String testCaseName, String command, String expectedOutput) {
        outContent.reset();
        errContent.reset();
        run(command);
        String actualOutput = normalize(outContent.toString());
        assertEquals(testCaseName, expectedOutput, actualOutput);
    }

    protected void runAndExpectError(String testCaseName, String command, String expectedErrorOutput) {
        outContent.reset();
        errContent.reset();
        run(command);
        assertThat(normalize(errContent.toString()), containsString(expectedErrorOutput));
    }

}
