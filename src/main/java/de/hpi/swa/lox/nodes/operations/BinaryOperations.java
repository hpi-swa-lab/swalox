package de.hpi.swa.lox.nodes.operations;

import java.math.BigInteger;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;


import com.oracle.truffle.api.dsl.Bind;

import de.hpi.swa.lox.parser.LoxRuntimeError;

public final class BinaryOperations {

    // @GenerateInline(true)
    // @GenerateUncached
    static public abstract class BinaryOperationNode extends Node {

        public abstract Object execute(Object left, Object right);

        // override in subclasses
        @Specialization
        protected Object doOp(long left, long right) {
            throw CompilerDirectives.shouldNotReachHere();
        }

        @Specialization
        protected Object doOp(BigInteger left, BigInteger right) {
            throw CompilerDirectives.shouldNotReachHere();
        }

        @Specialization
        protected double doOp(double left, double right) {
            throw CompilerDirectives.shouldNotReachHere();
        }

        // dispatch to actual behavior
        @Specialization
        double doLD(long left, double right) {
            return doOp((double) left, right);
        }

        @Specialization
        @TruffleBoundary
        Object doLB(long left, BigInteger right) {
            return doOp(BigInteger.valueOf(left), right);
        }

        @Specialization
        @TruffleBoundary
        Object doLD(BigInteger left, long right) {
            return doOp(left, BigInteger.valueOf(right));
        }

        @Specialization
        @TruffleBoundary
        Object doLD(BigInteger left, double right) {
            return doOp(left.doubleValue(), right);
        }

        @Specialization
        @TruffleBoundary
        Object doLB(double left, BigInteger right) {
            return doOp(left, right.doubleValue());
        }

        @Specialization
        Object doLD(double left, long right) {
            return doOp(left, (double) right);
        }

        // create foreign number to long, biginterger or double CastToNumberNode

        @Fallback
        Object doOO(Object left, Object right) { // , @CachedLibrary(limit = "2") InteropLibrary interop
            // if (interop.isNumber(left) && interop.isNumber(right)) {
            // try {
            // Object convertedLeft;
            // Object convertedRight;
            // if (interop.fitsInBigInteger(right)) {
            // convertedRight = interop.asBigInteger(right);
            // } else {
            // convertedRight = interop.asDouble(right);
            // }
            // if (interop.fitsInBigInteger(left)) {
            // convertedLeft = interop.asBigInteger(left);
            // } else {
            // convertedLeft = interop.asDouble(left);
            // }
            // return this.execute(convertedLeft, convertedRight);
            // // return doOp(convertedLeft, convertedRight);
            // } catch (UnsupportedMessageException e) {
            // // pass
            // }
            // }
            return error(left, right);
        }

        @TruffleBoundary
        private Object error(Object left, Object right) {
            throw new LoxRuntimeError("Type Error: Invalid operands for binary operation " + left + " and " + right,
                    this);
        }

    }

    @GenerateUncached
    public abstract static class AddNode extends BinaryOperationNode {

        @Override
        @Specialization
        protected Object doOp(long left, long right) {
            try {
                return Math.addExact(left, right);
            } catch (RuntimeException e) {
                return addAsBigInteger(left, right);
            }
        }

        @TruffleBoundary
        BigInteger addAsBigInteger(long left, long right) {
            return BigInteger.valueOf(left).add(BigInteger.valueOf(right));
        }

        @Override
        @TruffleBoundary
        @Specialization
        protected Object doOp(BigInteger left, BigInteger right) {
            return left.add(right);
        }

        @Override
        @Specialization
        protected double doOp(double left, double right) {
            return left + right;
        }

        @Specialization
        @TruffleBoundary
        static TruffleString addStrings(TruffleString left, Object right, @Bind Node node) {
            if (! (right instanceof TruffleString)) {
                throw new LoxRuntimeError("Type Error: No automatic String conversion for " + right, node);
            }
            return TruffleString.fromJavaStringUncached(left + right.toString(), TruffleString.Encoding.UTF_8);
        }

    }

    @GenerateUncached
    public abstract static class SubtractNode extends BinaryOperationNode {

        @Override
        @Specialization
        protected Object doOp(long left, long right) {
            try {
                return Math.subtractExact(left, right);
            } catch (RuntimeException e) {
                return subtractAsBigInteger(left, right);
            }
        }

        @TruffleBoundary
        BigInteger subtractAsBigInteger(long left, long right) {
            return BigInteger.valueOf(left).subtract(BigInteger.valueOf(right));
        }

        @Override
        @TruffleBoundary
        @Specialization
        protected Object doOp(BigInteger left, BigInteger right) {
            return left.subtract(right);
        }

        @Override
        @Specialization
        protected double doOp(double left, double right) {
            return left - right;
        }
    }

    @GenerateUncached
    public abstract static class MultiplyNode extends BinaryOperationNode {

        @Override
        @Specialization
        protected Object doOp(long left, long right) {
            try {
                return Math.multiplyExact(left, right);
            } catch (RuntimeException e) {
                return multiplyAsBigInteger(left, right);
            }
        }

        @TruffleBoundary
        BigInteger multiplyAsBigInteger(long left, long right) {
            return BigInteger.valueOf(left).multiply(BigInteger.valueOf(right));
        }

        @Override
        @TruffleBoundary
        @Specialization
        protected Object doOp(BigInteger left, BigInteger right) {
            return left.multiply(right);
        }

        @Override
        @Specialization
        protected double doOp(double left, double right) {
            return left * right;
        }
    }

}
