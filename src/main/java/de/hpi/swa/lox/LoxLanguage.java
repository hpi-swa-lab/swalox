package de.hpi.swa.lox;

import java.util.Map;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;

import de.hpi.swa.lox.nodes.BuiltInNode;
import de.hpi.swa.lox.nodes.ClockBuiltInNodeGen;
import de.hpi.swa.lox.nodes.LoadBuiltInNodeGen;
import de.hpi.swa.lox.nodes.LookupValueBuiltInNodeGen;
import de.hpi.swa.lox.nodes.MathRoundBuiltInNodeGen;
import de.hpi.swa.lox.nodes.NumberBuiltInNodeGen;
import de.hpi.swa.lox.nodes.StringBuiltInNodeGen;
import de.hpi.swa.lox.parser.LoxBytecodeCompiler;
import de.hpi.swa.lox.runtime.LoxContext;

@Bind.DefaultExpression("get($node)")
@TruffleLanguage.Registration(id = LoxLanguage.ID)
public class LoxLanguage extends TruffleLanguage<LoxContext> {

    public static final String ID = "lox";

    private static final LanguageReference<LoxLanguage> REFERENCE = LanguageReference.create(LoxLanguage.class);

    private Map<String, BuiltInNode> builtins;

    private Map<String, BuiltInNode> getBuiltins() {
        if (builtins == null) {
            builtins = Map
                    .of(
                            "clock", ClockBuiltInNodeGen.create(this),
                            "Number", NumberBuiltInNodeGen.create(this),
                            "String", StringBuiltInNodeGen.create(this),
                            "load", LoadBuiltInNodeGen.create(this),
                            "round", MathRoundBuiltInNodeGen.create(this),
                            "lookup", LookupValueBuiltInNodeGen.create(this));
        }
        return builtins;
    }

    @Override
    protected LoxContext createContext(Env env) {
        return new LoxContext(this, env, getBuiltins());
    }

    @Override
    protected CallTarget parse(ParsingRequest request) {
        Source source = request.getSource();
        RootCallTarget rootTarget = LoxBytecodeCompiler.parseLox(this, source);
        return rootTarget;
    }

    public static LoxLanguage get(Node node) {
        return REFERENCE.get(node);
    }

    @Override
    protected boolean isThreadAccessAllowed(Thread thread, boolean singleThreaded) {
        return true;
    }

}
