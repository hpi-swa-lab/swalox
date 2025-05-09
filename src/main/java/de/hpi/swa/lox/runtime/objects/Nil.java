package de.hpi.swa.lox.runtime.objects;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(InteropLibrary.class)
public final class Nil implements TruffleObject {
    public static final Nil INSTANCE = new Nil();

    private Nil() {
    }

    @Override
    public String toString() {
        return "nil";
    }

    @ExportMessage
    boolean isNull() {
        return true;
    }
}
