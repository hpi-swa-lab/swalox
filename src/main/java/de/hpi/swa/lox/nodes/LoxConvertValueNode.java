package de.hpi.swa.lox.nodes;

import java.math.BigInteger;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;

@GenerateUncached
// @GenerateInline(true)
public abstract class LoxConvertValueNode extends Node {

    @Specialization
    protected Object convert(Long object) {
        return object;
    }

    @Specialization
    protected Object convert(Double object) {
        return object;
    }

    @Specialization
    protected Object convert(BigInteger object) {
        return object;
    }

    @Specialization
    protected Object convert(Object object, @CachedLibrary(limit = "1") InteropLibrary interop) {
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
                if (object instanceof TruffleString) {
                    return object;
                }
                return TruffleString.fromJavaStringUncached(interop.asString(object), TruffleString.Encoding.UTF_8);
            }
        } catch (UnsupportedMessageException e) {
            // pass
        }
        return object;
    }

    public abstract Object execute(Object object);
}
