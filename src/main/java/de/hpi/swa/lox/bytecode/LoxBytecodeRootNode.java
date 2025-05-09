package de.hpi.swa.lox.bytecode;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.bytecode.BytecodeNode;
import com.oracle.truffle.api.bytecode.BytecodeRootNode;
import com.oracle.truffle.api.bytecode.ConstantOperand;
import com.oracle.truffle.api.bytecode.GenerateBytecode;
import com.oracle.truffle.api.bytecode.LocalAccessor;

import com.oracle.truffle.api.bytecode.MaterializedLocalAccessor;
import com.oracle.truffle.api.bytecode.Operation;
import com.oracle.truffle.api.bytecode.ShortCircuitOperation;
import com.oracle.truffle.api.bytecode.Variadic;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.source.SourceSection;

import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.nodes.LoxConvertValueNode;
import de.hpi.swa.lox.nodes.LoxRootNode;
import de.hpi.swa.lox.nodes.operations.BinaryOperations.AddNode;
import de.hpi.swa.lox.nodes.operations.BinaryOperations.MultiplyNode;
import de.hpi.swa.lox.nodes.operations.BinaryOperations.SubtractNode;
import de.hpi.swa.lox.nodes.operations.LoxCallFunctionNode;
import de.hpi.swa.lox.runtime.LoxContext;
import de.hpi.swa.lox.runtime.objects.GlobalObject;
import de.hpi.swa.lox.runtime.objects.LoxArray;
import de.hpi.swa.lox.runtime.objects.LoxClass;
import de.hpi.swa.lox.runtime.objects.LoxFunction;
import de.hpi.swa.lox.runtime.objects.LoxObject;
import de.hpi.swa.lox.runtime.objects.Nil;
import de.hpi.swa.lox.parser.LoxRuntimeError;

import com.oracle.truffle.api.bytecode.ShortCircuitOperation.Operator;
import com.oracle.truffle.api.debug.DebuggerTags;

@GenerateBytecode(//
        languageClass = LoxLanguage.class, //
        boxingEliminationTypes = { long.class }, // , boolean.class
        enableUncachedInterpreter = true, //
        enableMaterializedLocalAccesses = true, //
        enableRootTagging = true, //
        enableRootBodyTagging = false, //
        enableTagInstrumentation = true, //
        // DISABLED untile custom scope is finished
        // tagTreeNodeLibrary = LoxBytecodeScopeExports.class,
        // storeBytecodeIndexInFrame = true, //
        // defaultLocalValue = Nil.INSTANCE, // does not work?
        enableBlockScoping = true, // should be enabled by default
        enableSerialization = true)

@ShortCircuitOperation(name = "LoxAnd", booleanConverter = LoxBytecodeRootNode.LoxIsTruthy.class, operator = Operator.AND_RETURN_CONVERTED)
@ShortCircuitOperation(name = "LoxOr", booleanConverter = LoxBytecodeRootNode.LoxIsTruthy.class, operator = Operator.OR_RETURN_CONVERTED)
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

    @Operation
    public static final class LoxIsTruthy {

        @Specialization
        public static boolean fromLong(long x) {
            return x != 0;
        }

        @Specialization
        public static boolean fromBool(boolean x) {
            return x;
        }

        @Fallback
        public static boolean fromObject(Object x) {
            return x != Nil.INSTANCE;
        }
    }

    @Fallback
    public static boolean fromObject(Object x) {
        return x != Nil.INSTANCE;
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
    public static final class LoxValue {
        @Specialization
        static Object doDefault(Object value, @Cached LoxConvertValueNode convertValueNode) {
            return convertValueNode.execute(value);
        }
    }

    @Operation(tags = DebuggerTags.AlwaysHalt.class)
    public static final class LoxHalt {
        @TruffleBoundary
        @Specialization
        static void doDefault(@Bind LoxContext context) {
            System.err.println("Halt");
        }
    }

    // @Operation
    // public static final class LoxOr {
    // @Specialization
    // static boolean doBoolean(boolean left, boolean right) {
    // return left || right;
    // }

    // @Fallback
    // }
    // }

    // public static final class LoxAnd {
    // @Specialization
    // return left && right;
    // }

    // @Fallback
    // static Object fallback(Object left, Object right, @Bind Node node) {
    // throw typeError(node, "&&", left, right);
    // }
    // }

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
                @Cached MultiplyNode multiplyNode) { // Idea @Cached CastToLongNode castToLongNode
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

        @TruffleBoundary
        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {
            // maybe add .equals() here?

            return left.equals(right);
            // return left == right; // equality is special... one should be able to ask any
            // question and get
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
            return left != right; // (not) equality is special... one should be able to ask any question and get
                                  // false...
        }
    }

    @Operation
    public static final class LoxBitAnd {
        @Specialization
        static long doLong(long left, long right) {
            return left & right;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {
            throw typeError(node, "&", left, right);
        }
    }

    @Operation
    public static final class LoxBitOr {
        @Specialization
        static long doLong(long left, long right) {
            return left | right;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {
            throw typeError(node, "|", left, right);
        }
    }

    @Operation
    public static final class LoxMod {
        @Specialization
        static long doLong(long left, long right) {
            return left % right;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {
            throw typeError(node, "|", left, right);
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

        @Specialization(guards = { "index >= 0", "array.size() > index" })
        static Object writeArrayInSize(LoxArray array, long index, Object value) {
            array.setInSize((int) index, value);
            return value;
        }

        @Specialization(guards = { "index >= 0", "array.capacity() > index" }, replaces = "writeArrayInSize")
        static Object writeArrayInCapacity(LoxArray array, long index, Object value) {
            array.setInCapacity((int) index, value);
            return value;
        }

        @Specialization(guards = "index >= 0", replaces = "writeArrayInCapacity")
        static Object writeArray(LoxArray array, long index, Object value) {
            array.set((int) index, value);
            return value;
        }

        // @Specialization(guards = "index >= 0")
        // static Object writeArray(LoxArray array, long index, Object value) {
        // array.set((int) index, value);
        // return value;
        // }

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
    @ConstantOperand(type = MaterializedLocalAccessor.class)
    public static final class LoxCheckNonLocalDefined {
        @Specialization
        static void doDefault(MaterializedLocalAccessor accessor, MaterializedFrame materializedFrame,
                @Bind BytecodeNode bytecodeNode,
                @Bind LoxContext context,
                @Bind Node node) {
            if (accessor.isCleared(bytecodeNode, materializedFrame)) {
                throw new LoxRuntimeError(formatError(accessor), node);
            }
        }

        @TruffleBoundary
        private static String formatError(MaterializedLocalAccessor accessor) {
            // TODO: rember name of variable... pass name as constant arg
            return "Variable " + accessor.toString() + "was not defined";
        }
    }

    @Operation
    @ConstantOperand(type = int.class)
    public static final class LoxLoadArgument {
        @Specialization
        static Object doDefault(VirtualFrame frame, int index) {
            return LoxFunction.getArgument(frame, index);
        }
    }

    @Operation
    public static final class LoxLoadThis {
        @Specialization
        static Object doDefault(VirtualFrame frame) {
            return LoxFunction.getThis(frame);
        }
    }

    @Operation
    @ConstantOperand(type = String.class)
    @ConstantOperand(type = RootNode.class)
    @ConstantOperand(type = int.class)
    public static final class LoxCreateFunction {

        @Specialization
        static LoxFunction doDefault(VirtualFrame frame, String name, RootNode node, int frameLevel) {
            MaterializedFrame materializedFrame = frameLevel > 0 ? frame.materialize() : null;
            return new LoxFunction(name, node, materializedFrame);
        }
    }

    @Operation
    public static final class LoxCall {

        @Specialization(limit = "1")
        static Object classInstationation(LoxClass klass, @Variadic Object[] arguments,
                // @CachedLibrary("klass") DynamicObjectLibrary dylib,
                // @CachedLibrary(limit = "1") InteropLibrary interop,
                @Cached LoxCallFunctionNode callNode,
                @Cached LookupMethodNode lookupMethod) {
            var object = new LoxObject(klass);
            // Object javaClass;
            // Object javaObject = null;
            // if ((javaClass = dylib.getOrDefault(klass, "javaSuperclass", null)) != null)
            // {
            // Object[] javaConstructorArguments = Arrays.copyOf(arguments, arguments.length
            // + 1);
            // javaConstructorArguments[arguments.length] = object;
            // javaObject = interop.instantiate(javaClass, javaConstructorArguments);
            // dylib.put(object, "javaThis", javaObject);
            // }
            LoxFunction function = lookupMethod.execute(object, klass, "init");
            if (function != null) {
                callNode.execute(function, arguments);
            }
            // if (javaObject != null) { return javaObject; }
            return object;

        }

        @Specialization
        static Object callFunction(LoxFunction obj, @Variadic Object[] arguments,
                @Cached LoxCallFunctionNode callNode) {
            return callNode.execute(obj, arguments);
        }

        @Specialization
        static Object doDefault(Object obj, @Variadic Object[] arguments, @Bind Node node,
                @CachedLibrary(limit = "1") InteropLibrary interop) {
            if (interop.isExecutable(obj)) {
                try {
                    return interop.execute(obj, arguments);
                } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                    return error(obj, node);
                }
            } else {
                try {
                    return interop.instantiate(obj, arguments);
                } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                    return error(obj, node);
                }
            }
        }

        @TruffleBoundary
        static Object error(Object obj, Node node) {
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

    @Operation
    @ConstantOperand(type = String.class)
    public static final class LoxDeclareClass {

        @Specialization
        @TruffleBoundary
        public static LoxClass declareWithoutSuperclass(String name, Nil nil, @Variadic Object[] methods,
                @CachedLibrary(limit = "1") DynamicObjectLibrary dylib) {
            var klass = new LoxClass(name);
            for (var m : methods) {
                dylib.putConstant(klass, ((LoxFunction) m).name, m, 0);
            }
            return klass;
        }

        @Specialization
        public static LoxClass declare(String name, LoxClass superclass, @Variadic Object[] methods,
                @CachedLibrary(limit = "1") DynamicObjectLibrary dylib) {
            var klass = declareWithoutSuperclass(name, Nil.INSTANCE, methods, dylib);
            dylib.putConstant(klass, "super", superclass, 0);
            return klass;
        }

        // @Fallback
        // @TruffleBoundary
        // public static LoxClass declare(String name, Object superclass, @Variadic
        // Object[] methods,
        // @CachedLibrary(limit = "1") DynamicObjectLibrary dylib,
        // @Bind LoxContext context,
        // @Bind Node node) {
        // if (context.getEnv().isHostObject(superclass)) {
        // var sc = context.getEnv().asHostObject(superclass);
        // if (sc instanceof Class<?> superClass) {
        // var newJavaSubclass = context.getEnv().createHostAdapter(new
        // Object[]{context.getEnv().asHostSymbol(superClass)});
        // var klass = new LoxClass(name);
        // for (var m : methods) {
        // dylib.putConstant(klass, ((LoxFunction ) m).name, m, 0);
        // }
        // dylib.putConstant(klass, "javaSuperclass", newJavaSubclass, 0);
        // return klass;
        // }
        // }
        // throw new LoxRuntimeError("Superclass must be a class", node);
        // }
    }

    @GenerateUncached
    public static abstract class ReadPropertyNode extends Node {
        public abstract Object execute(String name, Object obj);

        // The usage of String and equals can "explode" and may need @TruffleBoundary
        // a solution would be to use TruffleStrings as property names
        @Specialization
        public static Object read(String name, LoxArray array) {
            if (name.equals("length")) {
                return (long) array.size(); // Lox does not handle ints
            } else {
                return Nil.INSTANCE;
            }
        }

        @Specialization(limit = "1")
        public static Object read(String name, LoxObject obj,
                @CachedLibrary("obj") DynamicObjectLibrary dylib,
                @Cached LookupMethodNode lookupMethod) {
            var result = dylib.getOrDefault(obj, name, Nil.INSTANCE);
            if (result == Nil.INSTANCE) {
                var m = lookupMethod.execute(obj, (LoxClass) dylib.getOrDefault(obj, "Class", null), name);
                if (m != null) {
                    return m;
                }
            }
            return result;
        }

        @Specialization
        public static Object read(String name, Object obj,
                @Bind Node node,
                @CachedLibrary(limit = "1") InteropLibrary interop) {
            try {
                return interop.readMember(obj, name);
            } catch (UnsupportedMessageException | UnknownIdentifierException e) {
                return error(name, obj, node);
            }
        }

        @TruffleBoundary
        static Object error(String name, Object obj, @Bind Node node) {
            throw new LoxRuntimeError("Cannot read property " + name + " of " + obj, node);
        }

    }

    @Operation
    @ConstantOperand(type = String.class)
    public static final class LoxReadProperty {

        @Specialization
        public static Object read(String name, Object obj, @Cached ReadPropertyNode readProperty) {
            return readProperty.execute(name, obj);
        }
    }

    @Operation
    @ConstantOperand(type = String.class)
    public static final class LoxReadSuper {

        @Specialization
        public static Object read(String name, LoxObject obj, LoxClass superClass,
                @Cached LookupMethodNode lookupMethod) {
            var m = lookupMethod.execute(obj, superClass, name);
            if (m != null) {
                return m;
            } else {
                return methodNotFound(name);
            }
        }

        @TruffleBoundary
        public static Object methodNotFound(String name) {
            throw new LoxRuntimeError("Method " + name + " not found in super class", null);
        }
    }

    @GenerateUncached
    protected static abstract class LookupMethodNode extends Node {
        public abstract LoxFunction execute(LoxObject obj, LoxClass startingClass, String name);

        @Specialization(limit = "1", guards = { "startingClass == cachedStartingClass", "name == cachedName" })
        public LoxFunction doCached(LoxObject obj, LoxClass startingClass, String name,
                @Cached("name") String cachedName, @Cached("startingClass") LoxClass cachedStartingClass,
                @CachedLibrary("startingClass") DynamicObjectLibrary dylib,
                @Cached("lookupMethod(startingClass, name, dylib)") LoxFunction cachedMethod) {
            if (cachedMethod != null) {
                return new LoxFunction(obj, cachedMethod); // bind method to object
            } else {
                return null;
            }
        }

        @Specialization(limit = "1", replaces = "doCached")
        public LoxFunction doUncached(LoxObject obj, LoxClass startingClass, String name,
                @CachedLibrary("startingClass") DynamicObjectLibrary dylib) {

            var method = lookupMethod(startingClass, name, dylib);
            if (method != null) {
                return new LoxFunction(obj, method); // bind method to object
            } else {
                return null;
            }
        }

        // @Specialization(limit = "1")
        // public LoxFunction doUncached(LoxObject obj, LoxClass startingClass, String
        // name,
        // @CachedLibrary("startingClass") DynamicObjectLibrary dylib) {

        // var method = lookupMethod(startingClass, name, dylib);
        // if (method != null) {
        // return new LoxFunction(obj, method); // bind method to object
        // } else {
        // return null;
        // }
        // }

        public LoxFunction lookupMethod(LoxClass startingClass, String name,
                @CachedLibrary("startingClass") DynamicObjectLibrary dylib) {
            LoxClass klass = startingClass;
            while (klass != null) {
                var m = dylib.getOrDefault(klass, name, null);
                if (m != null) {
                    return (LoxFunction) m;
                }
                klass = (LoxClass) dylib.getOrDefault(klass, "super", null);
            }
            return null;
        }
    }

    @Operation
    @ConstantOperand(type = String.class)
    public static final class LoxWriteProperty {

        @Specialization
        public static Object write(String name, Object obj, Object value, @Cached WritePropertyNode writeProperty) {
            return writeProperty.execute(name, obj, value);
        }

    }

    @GenerateUncached
    public static abstract class WritePropertyNode extends Node {

        public abstract Object execute(String name, Object obj, Object value);

        @Specialization(limit = "1")
        public static Object write(String name, LoxObject obj, Object value,
                @CachedLibrary("obj") DynamicObjectLibrary dylib) {
            dylib.put(obj, name, value);
            return value;
        }

        @Specialization(limit = "1")
        public static Object interopWrite(String name, Object obj, Object value,
                @CachedLibrary(limit = "1") InteropLibrary interop,
                @Bind Node node) {
            try {
                interop.writeMember(obj, name, value);
            } catch (UnsupportedTypeException | UnsupportedMessageException | UnknownIdentifierException e) {
                error(name, obj, node);
            }
            return value;
        }

        @TruffleBoundary
        private static void error(String name, Object obj, Node node) {
            throw new LoxRuntimeError("Cannot write property " + name + " of " + obj, node);
        }
    }

    // Helper

    @TruffleBoundary
    public static LoxRuntimeError typeError(Node node, String operation, Object... values) {
        StringBuilder result = new StringBuilder();
        result.append("Type Error for Operation " + operation + " with values:");
        for (Object value : values) {
            result.append(" ");
            result.append(value);
        }

        return new LoxRuntimeError(result.toString(), node);
    }

}
