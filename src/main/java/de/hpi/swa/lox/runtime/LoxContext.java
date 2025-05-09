package de.hpi.swa.lox.runtime;

import java.io.OutputStream;
import java.util.Map;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;

import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.nodes.BuiltInNode;
import de.hpi.swa.lox.runtime.objects.GlobalObject;
import de.hpi.swa.lox.runtime.objects.LoxArray;
import de.hpi.swa.lox.runtime.objects.LoxFunction;

@Bind.DefaultExpression("get($node)")
public final class LoxContext {
    private final Env env; // Truffle environment

    public GlobalObject globalObject;

    public LoxContext(LoxLanguage language, TruffleLanguage.Env env, Map<String, BuiltInNode> builtins) {
        this.env = env;
        this.globalObject = new GlobalObject();

        var ARGV = new LoxArray();
        this.globalObject.set("ARGV", ARGV);

        var args = env.getApplicationArguments();
        for(int i = 0; i < args.length; i++){
            ARGV.set(i, TruffleString.fromJavaStringUncached(args[i], TruffleString.Encoding.UTF_8));
        }

        for (var e : builtins.entrySet()) {
            this.globalObject.set(e.getKey(), new LoxFunction(e.getKey(), e.getValue().getCallTarget(), null));
        }
    }

    private static final ContextReference<LoxContext> REFERENCE = ContextReference.create(LoxLanguage.class);

    public static LoxContext get(Node node) {
        return REFERENCE.get(node);
    }

    public Env getEnv() {
        return env;
    }

    public OutputStream getOutput() {
        return env.out();
    }

    public GlobalObject getGlobalObject() {
        return globalObject;
    }

}
