package de.hpi.swa.lox.nodes;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.strings.TruffleString;

import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.runtime.LoxContext;

public abstract class LookupValueBuiltInNode extends BuiltInNodeWithArgs {

    public LookupValueBuiltInNode(LoxLanguage lang) {
        super(lang, 2);
    }

    @TruffleBoundary
    @Specialization
    static Object lookup(TruffleString language, TruffleString name,
            @Bind LoxContext context,
            @CachedLibrary(limit = "1") InteropLibrary interop) {
        String lang = language.toJavaStringUncached();
        if (lang.equals("java")) {
            return context.getEnv().lookupHostSymbol(name.toJavaStringUncached());
        } else {
            for (var e : context.getEnv().getPublicLanguages().entrySet()) {
                if (e.getKey().equals(lang)) {
                    try {
                        return interop.readMember(context.getEnv().getScopePublic(e.getValue()),
                                name.toJavaStringUncached());
                    } catch (UnsupportedMessageException | UnknownIdentifierException | IllegalArgumentException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            }
            throw new RuntimeException("Language not found: " + lang);
        }
    }
}
