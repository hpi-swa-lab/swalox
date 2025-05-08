package de.hpi.swa.lox.bytecode;

import java.io.IOException;
import java.util.Objects;


import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.bytecode.BytecodeRootNode;
import com.oracle.truffle.api.bytecode.GenerateBytecode;
import com.oracle.truffle.api.bytecode.Operation;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;

import com.oracle.truffle.api.dsl.Fallback;

import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.nodes.LoxRootNode;
import de.hpi.swa.lox.parser.LoxRuntimeError;
import de.hpi.swa.lox.runtime.LoxContext;


@GenerateBytecode(//
        languageClass = LoxLanguage.class, //
        boxingEliminationTypes = { long.class, boolean.class }, //
        enableUncachedInterpreter = true, //
        enableSerialization = true)
public abstract class LoxBytecodeRootNode extends LoxRootNode implements BytecodeRootNode {

    protected LoxBytecodeRootNode(LoxLanguage language, FrameDescriptor frameDescriptor) {
        super(language, frameDescriptor);
    }

    // gets implemented by the generated code 
    @Override
    public abstract SourceSection getSourceSection();
    

    @Override
    public final SourceSection ensureSourceSection() {
        return BytecodeRootNode.super.ensureSourceSection();
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
        static long doLong(long left, long right) {
            return left + right;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {   
            throw typeError(node, "+", left, right);
        }
    }

    @Operation
    public static final class LoxSub {
        @Specialization
        static long doLong(long left, long right) {
            return left - right;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {   
            throw typeError(node, "-", left, right);
        }
    }

    @Operation
    public static final class LoxMul {
        @Specialization
        static long doLong(long left, long right) {
            return left * right;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {   
            throw typeError(node, "*", left, right);
        }
    }

    @Operation
    public static final class LoxDiv {
        @Specialization
        static double doLong(long left, long right) {
            return (double)left / right;
        }

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {   
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

        @Fallback
        static Object fallback(Object left, Object right, @Bind Node node) {   
            throw typeError(node, "==", left, right);
        }
    }

    @Operation
    public static final class LoxNotEqual {
        @Specialization
        static boolean doLong(long left, long right) {
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
            throw typeError(node, "!", value);
        }
    }


    private static LoxRuntimeError typeError(Node node, String operation, Object... values) {
        StringBuilder result = new StringBuilder();
        result.append("Type Error for Operation " + operation + " with values:");   
        for (Object value : values) {
            result.append(" ");
            result.append(value);
        };

        return new LoxRuntimeError(result.toString(), node);
    }                

}
