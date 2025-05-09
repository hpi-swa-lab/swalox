/*
 * Copyright (c) 2017, 2022, Oracle and/or its affiliates. All rights reserved.
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

package de.hpi.swa.lox.parser;



import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.ExceptionType;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
 
@ExportLibrary(InteropLibrary.class)
public class LoxParseError extends AbstractTruffleException {
    private final Source source;
    private final int line;
    private final int column;
    private final int length;

    public static LoxParseError build(Source source, ParseTree tree, String message) {
    

        String s = message;
        var line = 0;
        var column = 0;
        var length = 0;

        if (tree.getPayload() instanceof ParserRuleContext context) {
            Token startToken = context.getStart();
            Token stopToken = context.getStop();
            line = startToken.getLine();
            column = startToken.getCharPositionInLine();
            length = stopToken.getStopIndex() - startToken.getStartIndex() + 1;

            s = formatMessage(message, formatLocation(source, line, column));
        }     
        return new LoxParseError(source, line, column, length, s); 
    }

    private static String formatMessage(String message, String location) {
        return location + " Error:" + message;
    }

    private static String formatLocation(Source source, int line, int column) {
        return "[" + source.getName() + " line " + line + " column " + column + "] ";
    }   


    public static LoxParseError build(Source source, int line, int charPositionInLine, Token token, String message) {
    
        int col = charPositionInLine + 1;
        String location = formatLocation(source, line, col);
        int length = token == null ? 1 : Math.max(token.getStopIndex() - token.getStartIndex(), 0);
        
        throw new LoxParseError(source, line, col, length, formatMessage(message, location));
    }

    public LoxParseError(Source source, int line, int column, int length, String message) {
        super(message);
        this.source = source;
        this.line = line;
        this.column = column;
        this.length = length;
    }

    @ExportMessage
    ExceptionType getExceptionType() {
        return ExceptionType.PARSE_ERROR;
    }

    @ExportMessage
    boolean hasSourceLocation() {
        return source != null;
    }

    @ExportMessage(name = "getSourceLocation")
    @TruffleBoundary
    SourceSection getSourceSection() throws UnsupportedMessageException {
        if (source == null) {
            throw UnsupportedMessageException.create();
        }
        return source.createSection(line, column, length);
    }
}
 