package de.hpi.swa.lox.nodes;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.strings.TruffleString;

import de.hpi.swa.lox.LoxLanguage;

public abstract class StringBuiltInNode extends BuiltInNodeWithArgs {

    public StringBuiltInNode(LoxLanguage lang) {
        super(lang, 1);
    }

    @Specialization
    @TruffleBoundary
    static Object parseString(Object string) {
        return TruffleString.fromJavaStringUncached(string.toString(), TruffleString.Encoding.UTF_8);
    }

}
