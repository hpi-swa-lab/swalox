package de.hpi.swa.lox.bytecode;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.bytecode.BytecodeNode;
import com.oracle.truffle.api.bytecode.BytecodeRootNode;
import com.oracle.truffle.api.bytecode.ConstantOperand;
import com.oracle.truffle.api.bytecode.GenerateBytecode;
import com.oracle.truffle.api.bytecode.LocalAccessor;
import com.oracle.truffle.api.bytecode.Operation;
import com.oracle.truffle.api.bytecode.Variadic;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceSection;

import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.nodes.LoxRootNode;
import de.hpi.swa.lox.nodes.operations.BinaryOperations.AddNode;
import de.hpi.swa.lox.nodes.operations.BinaryOperations.MultiplyNode;
import de.hpi.swa.lox.nodes.operations.BinaryOperations.SubtractNode;
import de.hpi.swa.lox.runtime.LoxContext;
import de.hpi.swa.lox.runtime.objects.GlobalObject;
import de.hpi.swa.lox.runtime.objects.LoxArray;
import de.hpi.swa.lox.runtime.objects.LoxFunction;
import de.hpi.swa.lox.runtime.objects.Nil;
import de.hpi.swa.lox.parser.LoxRuntimeError;

@GenerateBytecode(//
        languageClass = LoxLanguage.class, //
        boxingEliminationTypes = { long.class, boolean.class }, //
        enableUncachedInterpreter = true, //
        enableMaterializedLocalAccesses = true, //
        // storeBytecodeIndexInFrame = true, //
        // defaultLocalValue = Nil.INSTANCE, // does not work?
        enableBlockScoping = true, // should be enabled by default
        enableSerialization = true)
public abstract class LoxBytecodeRootNode extends LoxRootNode implements BytecodeRootNode {

    protected LoxBytecodeRootNode(LoxLanguage language, FrameDescriptor frameDescriptor) {
        super(language, frameDescriptor);
        // environment = new LoxEnvironment();

    }

    // gets implemented by the generated code
    @Override
    public abstract SourceSection getSourceSection();

    @Override
    public final SourceSection ensureSourceSection() {
        return BytecodeRootNode.super.ensureSourceSection();
    }

    static private boolean isTruthy(Object object) {
        if (object == Nil.INSTANCE)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    @Operation
    public static final class LoxIsTruthy {

        @Specialization
        static boolean bool(boolean value) {
            return value == true;
        }

        @Fallback
        static boolean doDefault(Object value) {
            return isTruthy(value);
        }
    }

    // Print Statement
    @Operation
    public static final class LoxPrint {
        @TruffleBoundary
        @Specialization
        static void doDefault(Object value, @Bind LoxContext context) {
            var out = context.getOutput();
            var outputText = Objects.toString(value);
            try {
                out.write(outputText.getBytes());
                out.write(System.lineSeparator().getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Operation
    public static final class LoxHalt {
        @TruffleBoundary
        @Specialization
        static void doDefault(@Bind LoxContext context) {
            System.err.println("Halt");
        }
    }

    @Operation
    public static final class LoxOr {
        @Specialization
        static boolean doBoolean(boolean left, boolean right) {
            return left || right;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {
            throw typeError(node, "|", left, right);
        }
    }

    @Operation
    public static final class LoxAnd {
        @Specialization
        static boolean doBoolean(boolean left, boolean right) {
            return left && right;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {
            throw typeError(node, "&&", left, right);
        }
    }

    @Operation
    public static final class LoxAdd {
        @Specialization
        static Object doOp(Object left, Object right,
                @Bind Node node,
                @Cached AddNode addNode) {
            return addNode.execute(left, right);
        }
    }

    @Operation
    public static final class LoxSub {
        @Specialization
        static Object doOp(Object left, Object right,
                @Bind Node node,
                @Cached SubtractNode subtractNode) {
            return subtractNode.execute(left, right);
        }
    }

    @Operation
    public static final class LoxMul {
        @Specialization
        static Object doOp(Object left, Object right,
                @Bind Node node,
                @Cached MultiplyNode multiplyNode) {
            return multiplyNode.execute(left, right);
        }
    }

    @Operation
    public static final class LoxDiv {

        @Specialization
        static double doLong(long left, long right) {
            return (double) left / right;
        }

        @Specialization
        @TruffleBoundary // because BigInteger methods are not safe for Truffle to partially evaluate
        public static double doBigInteger(BigInteger left, BigInteger right) {
            return left.doubleValue() / right.doubleValue();
        }

        @Specialization
        static double doDouble(double left, double right) {
            return left / right;
        }

        @Fallback
        @TruffleBoundary
        static Object fallback(Object left, Object right, @Bind Node node) {
            if (left instanceof Double leftDouble) {
                if (right instanceof Double rightDouble) {
                    return doDouble(leftDouble, rightDouble);
                } else if (right instanceof Long rightLong) {
                    return doDouble(leftDouble, (double) rightLong);
                } else if (right instanceof BigInteger righBigInteger) {
                    return doDouble(leftDouble, righBigInteger.doubleValue());
                }
            }
            if (right instanceof Double rightDouble) {
                if (left instanceof Long leftLong) {
                    return doDouble((double) leftLong, rightDouble);
                } else if (left instanceof BigInteger leftBigInteger) {
                    return doDouble(leftBigInteger.doubleValue(), rightDouble);
                }
            }
            if (left instanceof Long leftLong) {
                if (right instanceof Long rightLong) {
                    return doLong(leftLong, rightLong);
                } else if (right instanceof BigInteger rightBigInteger) {
                    return doBigInteger(BigInteger.valueOf(leftLong), rightBigInteger);
                }
            }
            if (left instanceof BigInteger leftBigInteger) {
                if (right instanceof Long rightLong) {
                    return doBigInteger(leftBigInteger, BigInteger.valueOf(rightLong));
                } else if (right instanceof BigInteger rightBigInteger) {
                    return doBigInteger(leftBigInteger, rightBigInteger);
                }
            }

            throw typeError(node, "/", left, right);
        }
    }

    // Equality
    @Operation
    public static final class LoxLess {
        @Specialization
        static boolean doLong(long left, long right) {
            return left < right;
        }

        @Specialization
        static boolean doDoubleDouble(double left, double right) {
            return left < right;
        }

        @Specialization
        static boolean doLongDouble(long left, double right) {
            return left < right;
        }

        @Specialization
        static boolean doDoubleLong(double left, long right) {
            return left < right;
        }

        @Specialization
        @TruffleBoundary
        public static boolean doBigNumber(BigInteger left, BigInteger right) {
            return left.compareTo(right) < 0;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {
            throw typeError(node, "<", left, right);
        }
    }

    @Operation
    public static final class LoxLessEqual {
        @Specialization
        static boolean doLong(long left, long right) {
            return left <= right;
        }

        @Specialization
        static boolean doDoubleDouble(double left, double right) {
            return left <= right;
        }

        @Specialization
        static boolean doLongDouble(long left, double right) {
            return left <= right;
        }

        @Specialization
        static boolean doDoubleLong(double left, long right) {
            return left <= right;
        }

        @Specialization
        @TruffleBoundary
        public static boolean doBigNumber(BigInteger left, BigInteger right) {
            return left.compareTo(right) <= 0;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {
            throw typeError(node, "<=", left, right);
        }
    }

    @Operation
    public static final class LoxGreater {
        @Specialization
        static boolean doLong(long left, long right) {
            return left > right;
        }

        @Specialization
        static boolean doDoubleDouble(double left, double right) {
            return left > right;
        }

        @Specialization
        static boolean doLongDouble(long left, double right) {
            return left > right;
        }

        @Specialization
        static boolean doDoubleLong(double left, long right) {
            return left > right;
        }

        @Specialization
        @TruffleBoundary
        public static boolean doBigNumber(BigInteger left, BigInteger right) {
            return left.compareTo(right) > 0;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {
            throw typeError(node, ">", left, right);
        }
    }

    @Operation
    public static final class LoxGreaterEqual {
        @Specialization
        static boolean doLong(long left, long right) {
            return left >= right;
        }

        @Specialization
        static boolean doDoubleDouble(double left, double right) {
            return left >= right;
        }

        @Specialization
        static boolean doLongDouble(long left, double right) {
            return left >= right;
        }

        @Specialization
        static boolean doDoubleLong(double left, long right) {
            return left >= right;
        }

        @Specialization
        @TruffleBoundary
        public static boolean doBigNumber(BigInteger left, BigInteger right) {
            return left.compareTo(right) >= 0;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {
            throw typeError(node, ">=", left, right);
        }
    }

    @Operation
    public static final class LoxEqual {
        @Specialization
        static boolean doLong(long left, long right) {
            return left == right;
        }

        @Specialization
        static boolean doDoubleDouble(double left, double right) {
            return left == right;
        }

        @Specialization
        static boolean doLongDouble(long left, double right) {
            return left == right;
        }

        @Specialization
        static boolean doDoubleLong(double left, long right) {
            return left == right;
        }

        @Specialization
        @TruffleBoundary
        public static boolean doLongAndBig(long left, BigInteger right) {
            return doBigNumber(BigInteger.valueOf(left), right);
        }

        @Specialization
        @TruffleBoundary
        public static boolean doLongAndBig(BigInteger left, long right) {
            return doBigNumber(left, BigInteger.valueOf(right));
        }

        @Specialization
        @TruffleBoundary
        public static boolean doBigNumber(BigInteger left, BigInteger right) {
            return left.equals(right);
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {
            return left == right; // equality is special... one should be able to ask any question and get
                                  // false...

            // throw typeError(node, "==", left, right);
        }
    }

    @Operation
    public static final class LoxNotEqual {
        @Specialization
        static boolean doLong(long left, long right) {
            return left != right;
        }

        @Specialization
        static boolean doDoubleDouble(double left, double right) {
            return left != right;
        }

        @Specialization
        static boolean doLongDouble(long left, double right) {
            return left != right;
        }

        @Specialization
        static boolean doDoubleLong(double left, long right) {
            return left != right;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {
            throw typeError(node, "!=", left, right);
        }
    }

    @Operation
    public static final class LoxUnaryMinus {
        @Specialization
        static long doLong(long value) {
            return -value;
        }

        @Specialization
        static double doDouble(double value) {
            return -value;
        }

        @Fallback
        static Object fallback(Object value, @Bind Node node) {
            throw typeError(node, "-", value);
        }

    }

    @Operation
    public static final class LoxNegate {
        @Specialization
        static boolean doBoolean(boolean value) {
            return !value;
        }

        @Fallback
        static Object fallback(Object value, @Bind Node node) {
            return !isTruthy(value);
        }
    }

    @TruffleBoundary
    static Object checkDeclared(String name, GlobalObject globalObject, Node node) {
        if (!globalObject.hasKey(name)) {
            throw new LoxRuntimeError("Variable " + name + " was not declared", node);
        }
        return globalObject.get(name);
    }

    @Operation
    @ConstantOperand(type = String.class)
    public static final class LoxDefineGlobalVariable {
        @Specialization
        static void doDefault(String name,
                @Bind LoxContext context,
                @Bind Node node) {
            GlobalObject globalObject = context.getGlobalObject();
            if (globalObject.get(name) != null) {
                printWarning(name, context);
            }
            globalObject.set(name, null);
        }

        @TruffleBoundary
        private static void printWarning(String name, LoxContext context) {
            var out = context.getOutput();
            try {
                out.write(("Warning: Variable " + name + " was already declared").getBytes());
                out.write(System.lineSeparator().getBytes());
                out.flush();
            } catch (IOException e) {
                // pass
            }
        }
    }

    @Operation
    @ConstantOperand(type = String.class)
    public static final class LoxWriteGlobalVariable {
        @Specialization
        static void doDefault(String name, Object value,
                @Bind LoxContext context,
                @Bind Node node) {
            GlobalObject globalObject = context.getGlobalObject();
            checkDeclared(name, globalObject, node);
            globalObject.set(name, value);
        }
    }

    @Operation
    @ConstantOperand(type = String.class)
    public static final class LoxReadGlobalVariable {
        @Specialization
        static Object doDefault(String name,
                @Bind LoxContext context,
                @Bind Node node) {
            GlobalObject globalObject = context.getGlobalObject();
            var result = checkDeclared(name, globalObject, node);
            if (result == null) {
                throw new LoxRuntimeError(formatMessage(name), node);
            }
            return result;
        }

        @TruffleBoundary
        private static String formatMessage(String name) {
            return "Variable " + name + " was not defined";
        }
    }

    @Operation
    public static final class LoxNewArray {

        @Specialization
        static Object fallback() {
            return new LoxArray();
        }
    }

    @Operation
    public static final class LoxReadArray {

        @Specialization(guards = "index >= 0")

        static Object readArray(LoxArray array, long index) {
            return array.get((int) index);
        }

        @Fallback
        static Object fallback(Object array, Object index, @Bind Node node) {
            throw typeError(node, "array[index]", array, index);
        }
    }

    @Operation
    public static final class LoxWriteArray {

        @Specialization(guards = "index >= 0")
        static Void writeArray(LoxArray array, long index, Object value) {
            array.set((int) index, value);
            return null;
        }

        @Fallback
        static Object fallback(Object array, Object index, Object value, @Bind Node node) {
            throw typeError(node, "array[index]=value", array, index, value);
        }
    }

    @Operation
    @ConstantOperand(type = LocalAccessor.class)
    public static final class LoxCheckLocalDefined {
        @Specialization
        static void doDefault(VirtualFrame frame, LocalAccessor accessor,
                @Bind BytecodeNode bytecodeNode,
                @Bind LoxContext context,
                @Bind Node node) {
            if (accessor.isCleared(bytecodeNode, frame)) {
                throw new LoxRuntimeError(formatError(accessor), node);
            }
        }

        @TruffleBoundary
        private static String formatError(LocalAccessor accessor) {
            return "Variable " + accessor.toString() + " was not defined";
        }
    }

    @Operation
    @ConstantOperand(type = LocalAccessor.class)
    public static final class LoxCheckNonLocalDefined {
        @Specialization
        static void doDefault(LocalAccessor accessor, MaterializedFrame materializedFrame,
                @Bind BytecodeNode bytecodeNode,
                @Bind LoxContext context,
                @Bind Node node) {
            if (accessor.isCleared(bytecodeNode, materializedFrame)) {
                throw new LoxRuntimeError(formatError(accessor), node);
            }
        }

        @TruffleBoundary
        private static String formatError(LocalAccessor accessor) {
            return "Variable " + accessor.toString() + " was not defined";
        }
    }

    @Operation
    @ConstantOperand(type = int.class)
    public static final class LoxLoadArgument {
        @Specialization(guards = "index <= arguments.length")
        static Object doDefault(VirtualFrame frame, int index,
                @Bind("frame.getArguments()") Object[] arguments) {
            return arguments[index + 1];
        }

        @Fallback
        static Object doLoadOutOfBounds(int index) {
            /* Use the default null value. */
            return Nil.INSTANCE;
        }
    }

    @Operation
    @ConstantOperand(type = String.class)
    @ConstantOperand(type = RootCallTarget.class)
    @ConstantOperand(type = int.class)
    public static final class LoxCreateFunction {

        @Specialization
        static LoxFunction doDefault(VirtualFrame frame, String name, RootCallTarget callTarget, int frameLevel) {
            MaterializedFrame materializedFrame = frameLevel > 0 ? frame.materialize() : null;
            return new LoxFunction(name, callTarget, materializedFrame);
        }
    }

    @Operation
    public static final class LoxCall {

        @Specialization(limit = "3", //
                guards = "function.getCallTarget() == cachedTarget")
        protected static Object doDirect(LoxFunction function, @Variadic Object[] arguments,
                @Cached("function.getCallTarget()") RootCallTarget cachedTarget,
                @Cached("create(cachedTarget)") DirectCallNode directCallNode) {
            return directCallNode.call(function, function.createArguments(arguments));
        }

        @Specialization(replaces = "doDirect")
        static Object doIndirect(LoxFunction function, @Variadic Object[] arguments,
                @Cached IndirectCallNode callNode) {
            return callNode.call(function.getCallTarget(), function.createArguments(arguments));
        }

        @TruffleBoundary
        @Specialization
        static Object doDefault(Object obj, @Variadic Object[] arguments, @Bind Node node) {
            throw new LoxRuntimeError("cannot call " + obj, node);
        }
    }

    @Operation
    @ConstantOperand(type = int.class)
    public static final class LoxLoadMaterialzedFrameN {

        @Specialization
        public static MaterializedFrame doDefault(VirtualFrame frame, int index) {
            return LoxFunction.getFrameAtLevelN(frame, index);
        }
    }

    // For DEBUGGING / TESTING BytecodeDSL, see visitHack

    @Operation
    public static final class MaterializeFrame {
        @Specialization
        public static MaterializedFrame materialize(VirtualFrame frame) {
            return frame.materialize();
        }
    }

    // Helper

    @TruffleBoundary
    private static LoxRuntimeError typeError(Node node, String operation, Object... values) {
        StringBuilder result = new StringBuilder();
        result.append("Type Error for Operation " + operation + " with values:");
        for (Object value : values) {
            result.append(" ");
            result.append(value);
        }

        return new LoxRuntimeError(result.toString(), node);
    }

}
