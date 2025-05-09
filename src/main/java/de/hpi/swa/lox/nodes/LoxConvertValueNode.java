package de.hpi.swa.lox.nodes;

import java.math.BigInteger;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import de.hpi.swa.lox.runtime.objects.LoxObject;
import de.hpi.swa.lox.runtime.objects.Nil;

@GenerateUncached
// @GenerateInline(true)
public abstract class LoxConvertValueNode extends Node {
    // @Specialization
    // protected Object convert(Nil object) {
    // return object;
    // }

    // @Specialization
    // protected Object convert(LoxObject object) {
    // return object;
    // }

    // @Specialization
    // protected Object convert(Long object) {
    // return object;
    // }

    // @Specialization
    // protected Object convert(Double object) {
    // return object;
    // }

    // @Specialization
    // protected Object convert(BigInteger object) {
    // return object;
    // }

    // @Specialization
    // protected Object convert(TruffleString object) {
    // return object;
    // }

    @Specialization(guards = "isLoxType(object)")
    protected Object convert(Object object) {
        return object;
    }

    @Specialization(guards = "!isLoxType(object)")
    protected Object convert(Object object,
            @CachedLibrary(limit = "1") InteropLibrary interop) {
        try {
            if (object instanceof Character c) {
                return TruffleString.fromCodePointUncached(c, TruffleString.Encoding.UTF_8);
            } else if (interop.fitsInLong(object)) {
                return interop.asLong(object);
            } else if (interop.fitsInDouble(object)) {
                return interop.asDouble(object);
            } else if (interop.fitsInBigInteger(object)) {
                return interop.asBigInteger(object);
            } else if (interop.isString(object)) {
                return TruffleString.fromJavaStringUncached(interop.asString(object), TruffleString.Encoding.UTF_8);
            }
        } catch (UnsupportedMessageException e) {
            // pass
        }
        return object;
    }

    protected static final boolean isLoxType(Object object) {
        return object instanceof Nil || object instanceof LoxObject || object instanceof Long
                || object instanceof Double || object instanceof BigInteger || object instanceof TruffleString;
    }

    public abstract Object execute(Object object);
}
