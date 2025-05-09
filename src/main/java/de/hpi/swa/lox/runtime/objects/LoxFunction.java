package de.hpi.swa.lox.runtime.objects;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class LoxFunction {

    private final String name;
    private final RootCallTarget callTarget;
    private final MaterializedFrame outerFrame;

    public LoxFunction(String name, RootCallTarget callTarget, MaterializedFrame outerFrame) {
        this.name = name;
        this.callTarget = callTarget;
        this.outerFrame = outerFrame;
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

    static Object getArgument(VirtualFrame frame, int index) {
        // adjust position because we have hidden arguments at the front
        return frame.getArguments()[index + 1]; 
    }

    static LoxFunction getCurrentFunction(Frame frame) {
        return (LoxFunction) frame.getArguments()[0];
    }

    static public MaterializedFrame getFrameAtLevelN(VirtualFrame frame, int index) {
        assert index > 0;
        LoxFunction func = getCurrentFunction(frame);
        for (int i = index - 1; i > 0; i--) {
            func = getCurrentFunction(func.outerFrame);
        }
        return func.outerFrame;
    }

    @TruffleBoundary
    public String toString() {
        return "Function " + this.name;
    }
}