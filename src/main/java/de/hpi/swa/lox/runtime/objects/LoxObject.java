package de.hpi.swa.lox.runtime.objects;

import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;

public final class LoxObject extends DynamicObject implements TruffleObject {

    public final LoxClass klass;
        
    public LoxObject(LoxClass klass) {
        super(klass.instanceShape);
        this.klass = klass;
    }
   
    @Override
    public String toString() {
        return klass.name;
    }

}
