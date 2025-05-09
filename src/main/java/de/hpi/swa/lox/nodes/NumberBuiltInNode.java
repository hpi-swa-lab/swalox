package de.hpi.swa.lox.nodes;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.strings.TruffleString;

import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.runtime.objects.Nil;

public abstract class NumberBuiltInNode extends BuiltInNodeWithArgs {

    public NumberBuiltInNode(LoxLanguage lang) {
        super(lang, 1);
    }

    @Specialization
    @TruffleBoundary
    static Object parseNumber(TruffleString string) {
        var s = string.toJavaStringUncached();
        if (s.matches("^[0-9]+$")) {
            return Long.parseLong(s);
        } else if (s.matches("^[0-9]+\\.[0-9]+$")) {
            return Double.parseDouble(s);
        }
        return Double.NaN;
    }


    @Fallback
    static Object fallback(Object arg) {
        return Double.NaN;
    }

}
