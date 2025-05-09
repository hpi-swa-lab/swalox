package de.hpi.swa.lox.nodes.operations;

import java.math.BigInteger;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;

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
    
        @Fallback
        @TruffleBoundary
        Object doOO(Object left, Object right) {
            throw new RuntimeException("Type Error: Invalid operands for binary operation");
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
