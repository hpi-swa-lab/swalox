package de.hpi.swa.lox.runtime.objects;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class LoxFunction implements TruffleObject {

    public final String name;
    private final RootCallTarget callTarget;
    private final MaterializedFrame outerFrame;
    private final LoxObject self;

    private LoxFunction(String name, RootCallTarget callTarget, MaterializedFrame outerFrame, LoxObject self) {
        this.name = name;
        this.callTarget = callTarget;
        this.outerFrame = outerFrame;
        this.self = self;
    }

    public LoxFunction(String name, RootCallTarget callTarget, MaterializedFrame outerFrame) {
        this(name, callTarget, outerFrame, null);
    }

    public LoxFunction(LoxObject obj, LoxFunction m) {
        this(m.name, m.callTarget, m.outerFrame, obj);
    }

    public RootCallTarget getCallTarget() {
        return callTarget;
    }

    public Object[] createArguments(Object[] userArguments) {
        Object[] result = new Object[userArguments.length + 1];
        System.arraycopy(userArguments, 0, result, 1, userArguments.length);
        result[0] = this; // give the static function itself as first argument
        return result;
    }

    public static Object getArgument(VirtualFrame frame, int index) {
        // adjust position because we have hidden arguments at the front
        return frame.getArguments()[index + 1]; 
    }

    private static LoxFunction getCurrentFunction(Frame frame) {
        return (LoxFunction) frame.getArguments()[0];
    }

    public static LoxObject getThis(VirtualFrame frame) {
        return getCurrentFunction(frame).self;
    }

    public static MaterializedFrame getFrameAtLevelN(VirtualFrame frame, int index) {
        assert index > 0;
        LoxFunction func = getCurrentFunction(frame);
        for (int i = index - 1; i > 0; i--) {
            func = getCurrentFunction(func.outerFrame);
        }
        return func.outerFrame;
    }

    @TruffleBoundary
    public String toString() {
        return self == null ? "Function " + name : self.klass.name + "#" + name;
    }
}