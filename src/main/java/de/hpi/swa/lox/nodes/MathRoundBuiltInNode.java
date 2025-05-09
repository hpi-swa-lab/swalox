package de.hpi.swa.lox.nodes;

import java.math.BigInteger;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.parser.LoxRuntimeError;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.nodes.Node;

public abstract class MathRoundBuiltInNode extends BuiltInNodeWithArgs {

    public MathRoundBuiltInNode(LoxLanguage lang) {
        super(lang, 1);
    }

    @Specialization
    static Object roundLong(Long number) {
        return number;
    }

    @Specialization
    static Object roundBigInteger(BigInteger number) {
        return number;
    }

    @Specialization
    static Object roundDouble(Double number) {
        return Math.round(number);
    }

    @Fallback
    @TruffleBoundary
    static Object fallback(Object arg, @Bind Node node) {
        throw new LoxRuntimeError("Type Error: Argument must be a Number: " + arg, node);
    }

}
