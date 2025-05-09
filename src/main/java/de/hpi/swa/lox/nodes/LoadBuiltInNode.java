package de.hpi.swa.lox.nodes;

import java.io.IOException;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;

import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.parser.LoxBytecodeCompiler;
import de.hpi.swa.lox.parser.LoxRuntimeError;
import de.hpi.swa.lox.runtime.LoxContext;

public abstract class LoadBuiltInNode extends BuiltInNodeWithArgs {
    public LoadBuiltInNode(LoxLanguage language) {
        super(language, 1);
    }

    // Load and evaluate a file in the current context
    // Objects, functions, classes can be shared via global scope
    // since this is a low level functionality, the path has to be
    // - absolute or
    // - relative to the working directory or last file was loaded from
    @TruffleBoundary
    @Specialization
    static Object loadFile(TruffleString pathRelativeToWorkingDirectory,
            @Cached IndirectCallNode callNode,
            @Bind Node node,
            @Bind LoxLanguage language,
            @Bind LoxContext context) {
        var env = context.getEnv();
        var file = env.getPublicTruffleFile(pathRelativeToWorkingDirectory.toJavaStringUncached()).getAbsoluteFile();
        var cwd = env.getCurrentWorkingDirectory();
        env.setCurrentWorkingDirectory(file.getParent());
        try {
            var src = Source.newBuilder("lox", file).build();
            RootCallTarget rootTarget = LoxBytecodeCompiler.parseLox(language, src);
            return callNode.call(rootTarget);
        } catch (IOException e) {
            throw new LoxRuntimeError(e.getMessage(), node);
        } finally {
            env.setCurrentWorkingDirectory(cwd);
        }
    }
}
