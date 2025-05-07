package de.hpi.swa.lox.nodes;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import de.hpi.swa.lox.LoxLanguage;

@NodeInfo(language = "lox", description = "The root of all Lox execution trees")
public abstract class LoxRootNode extends RootNode {
    public LoxRootNode(LoxLanguage language, FrameDescriptor frameDescriptor) {
        super(language, frameDescriptor);
    }

    @Override
    public String toString() {
        return "root " + getName();
    }
}
