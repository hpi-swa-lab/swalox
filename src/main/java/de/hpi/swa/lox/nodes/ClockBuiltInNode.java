package de.hpi.swa.lox.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ClockBuiltInNode extends BuiltInNode {

    @Override
    public Object execute(VirtualFrame frame) {
        // return (double) System.currentTimeMillis() / 1000.0;
        return (double) System.nanoTime() / 1_000_000_000.0;
    }

}