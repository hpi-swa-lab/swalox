package de.hpi.swa.lox.runtime.objects;

import java.math.BigInteger;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

@ExportLibrary(InteropLibrary.class)
public class LoxFunction implements TruffleObject {

    public final String name;
    private final RootNode node;
    private final MaterializedFrame outerFrame;
    private final LoxObject self;

    private LoxFunction(String name, RootNode node, MaterializedFrame outerFrame, LoxObject self) {
        this.name = name;
        this.node = node;
        this.outerFrame = outerFrame;
        this.self = self;
    }

    public LoxFunction(String name, RootNode node, MaterializedFrame outerFrame) {
        this(name, node, outerFrame, null);
    }

    public LoxFunction(LoxObject obj, LoxFunction m) {
        this(m.name, m.node, m.outerFrame, obj);
    }

    public RootNode node() {
        return node;
    }

    public RootCallTarget getCallTarget() {
        return node.getCallTarget();
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

    @ExportMessage
    public boolean isExecutable() {
        return true;
    }

    @ExportMessage
    public Object execute(Object[] arguments, @Cached IndirectCallNode callNode) {
        Object[] args = createArguments(arguments);

        // we don't need to convert it here... because we do it in the operations
        // for (int i = 0; i < args.length; i++) {
        // if (args[i] instanceof Byte) {
        // args[i] = (long) (byte) args[i];
        // } else if (args[i] instanceof Short) {
        // args[i] = (long) (short) args[i];
        // } else if (args[i] instanceof Integer) {
        // args[i] = (long) (int) args[i];
        // } else if (args[i] instanceof Character) {
        // args[i] = TruffleString.fromCodePointUncached((char) args[i],
        // Encoding.UTF_8);
        // } else if (args[i] instanceof Float) {
        // args[i] = (double) (float) args[i];
        // }
        // }

        var result = callNode.call(this.getCallTarget(), args);
        if (result instanceof BigInteger) {
            return convertBigIntegerToDouble(result); // Truffle does not support BigInteger directly
        }
        return result;
    }

    @TruffleBoundary
    private double convertBigIntegerToDouble(Object result) {
        return ((BigInteger) result).doubleValue();
    }

}
