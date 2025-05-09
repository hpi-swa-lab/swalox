package de.hpi.swa.lox.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.runtime.objects.LoxFunction;

public abstract class BuiltInNodeWithArgs extends BuiltInNode {
    private final int numArgs;

    BuiltInNodeWithArgs(LoxLanguage language, int numArgs) {
        super(language);
        this.numArgs = numArgs;
    }

    public abstract Object execute(VirtualFrame frame, Object... arguments);

    @Override
    @ExplodeLoop
    public final Object execute(VirtualFrame frame) {
        Object[] arguments = new Object[numArgs];
        for (int i = 0; i < numArgs; i++) {
            arguments[i] = LoxFunction.getArgument(frame, i);
        }
        return execute(frame, arguments);
    }
}
