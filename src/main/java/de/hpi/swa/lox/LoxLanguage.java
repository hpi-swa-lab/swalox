package de.hpi.swa.lox;

import de.hpi.swa.lox.parser.LoxBytecodeCompiler;

import com.oracle.truffle.api.source.Source;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.TruffleLanguage;

import de.hpi.swa.lox.runtime.LoxContext;

@TruffleLanguage.Registration(id = LoxLanguage.ID)
public class LoxLanguage extends TruffleLanguage<LoxContext> {

    public static final String ID = "lox";

    @Override
    protected LoxContext createContext(Env env) {
        return new LoxContext(this, env);
    }

    @Override
    protected CallTarget parse(ParsingRequest request) {
        Source source = request.getSource();
        RootCallTarget rootTarget = LoxBytecodeCompiler.parseLox(this, source);
        return rootTarget;
    }
}
