package de.hpi.swa.lox.nodes;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import de.hpi.swa.lox.LoxLanguage;

public abstract class ClockBuiltInNode extends BuiltInNode {

    public ClockBuiltInNode(LoxLanguage language) {
        super(language);
    }

    @Specialization
    public Object doit(VirtualFrame frame) {
        // return (double) System.currentTimeMillis() / 1000.0;
        return (double) System.nanoTime() / 1_000_000_000.0;
    }

}
