package de.hpi.swa.lox.nodes.operations;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.bytecode.Variadic;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import de.hpi.swa.lox.runtime.objects.LoxFunction;


@GenerateUncached
public abstract class LoxCallFunctionNode extends Node {
    
    public abstract Object execute(Object left, Object right);

    @Specialization(limit = "3", //
                    guards = "function.getCallTarget() == cachedTarget")
    protected static Object doDirect(LoxFunction function, @Variadic Object[] arguments,
                    @Cached("function.getCallTarget()") RootCallTarget cachedTarget,
                    @Cached("create(cachedTarget)") DirectCallNode directCallNode) {            
        return directCallNode.call(function.createArguments(arguments));
    }

    @Specialization(replaces = "doDirect")
    static Object doIndirect(LoxFunction function, @Variadic Object[] arguments,
            @Cached IndirectCallNode callNode) {
        return callNode.call(function.getCallTarget(),  function.createArguments(arguments));
    }
}