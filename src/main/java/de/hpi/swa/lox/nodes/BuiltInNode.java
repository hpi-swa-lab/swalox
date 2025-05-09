package de.hpi.swa.lox.nodes;

import com.oracle.truffle.api.nodes.RootNode;

import de.hpi.swa.lox.LoxLanguage;

public abstract class BuiltInNode extends RootNode {
    BuiltInNode(LoxLanguage language) {
        super(language);
    }
}
