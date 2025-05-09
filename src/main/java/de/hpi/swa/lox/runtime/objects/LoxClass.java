package de.hpi.swa.lox.runtime.objects;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

public class LoxClass extends DynamicObject {
    static final Shape classShape = Shape.newBuilder().allowImplicitCastIntToLong(true).build();

    final String name;

    public final Shape instanceShape = Shape.newBuilder()
        .addConstantProperty("Class", this, 0)
        .allowImplicitCastIntToLong(true).build();

    public LoxClass(String name) {
        super(classShape);
        this.name = name;
    }

    @TruffleBoundary
    @Override
    public String toString() {
        return "Class " + name;
    }

}
