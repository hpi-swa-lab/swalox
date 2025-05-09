package de.hpi.swa.lox.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.tck.LanguageProvider;
import org.graalvm.polyglot.tck.Snippet;
import org.graalvm.polyglot.tck.TypeDescriptor;

public class LoxLanguageProvider implements LanguageProvider {

    @Override
    public String getId() {
        return "lox";
    }

    @Override
    public Value createIdentityFunction(Context context) {
        return context.eval("lox", "fun identity(x) { return x; }; return identity;");
    }

    @Override
    public Collection<? extends Snippet> createValueConstructors(Context context) {
        return List.of(
                Snippet.newBuilder(
                        "boolean",
                        context.eval("lox",
                                "fun makeTrue() { return true; }; return makeTrue;"),
                        TypeDescriptor.BOOLEAN).build(),
                Snippet.newBuilder(
                        "number",
                        context.eval("lox", "fun makeOne() { return 1; }; return makeOne;"),
                        TypeDescriptor.NUMBER).build(),
                Snippet.newBuilder(
                        "string",
                        context.eval("lox",
                                "fun makeString() { return \"Hello, World!\"; }; return makeString;"),
                        TypeDescriptor.STRING).build(),
                Snippet.newBuilder(
                        "array",
                        context.eval("lox",
                                "fun makeArray() { var a = []; a[0] = 1; a[1] = 2; return a; }; return makeArray;"),
                        TypeDescriptor.ARRAY).build(),
                Snippet.newBuilder(
                        "object",
                        context.eval("lox",
                                "class X {}; fun makeAnObject() { var x = X(); x.foo = 21; return x; }; return makeAnObject;"),
                        TypeDescriptor.OBJECT).build());
    }

    @Override
    public Collection<? extends Snippet> createExpressions(Context context) {
        final Collection<Snippet> expressions = new ArrayList<>();
        final TypeDescriptor numeric = TypeDescriptor.union(
                TypeDescriptor.NUMBER);
        Snippet.Builder builder = Snippet.newBuilder(
                "+",
                context.eval("lox",
                        "fun f(a, b){ return a + b;}; return f;"),
                TypeDescriptor.NUMBER).parameterTypes(numeric, numeric);
        expressions.add(builder.build());
        builder = Snippet.newBuilder(
                "+",
                context.eval("lox",
                        "fun f(a, b){ return a + b;}; return f;"),
                TypeDescriptor.STRING).parameterTypes(TypeDescriptor.STRING, TypeDescriptor.STRING);
        expressions.add(builder.build());

        // TODO add all other binary operators here

        return expressions;
    }

    @Override
    public Collection<? extends Snippet> createStatements(Context context) {
        final Collection<Snippet> statements = new ArrayList<>();
        Snippet.Builder builder = Snippet.newBuilder(
                "if",
                context.eval("lox", """
                        fun f(p){
                            if (p) return true ; else  return false;
                        };
                        return f;
                        """),
                TypeDescriptor.BOOLEAN).parameterTypes(TypeDescriptor.ANY);
        statements.add(builder.build());
        return statements;
    }

    @Override
    public Collection<? extends Snippet> createScripts(Context context) {
        return List.of(
                Snippet.newBuilder(
                        "src1",
                        context.eval("lox", """
                                    fun foo() {
                                    fun f() {
                                        return 42;
                                    }
                                    print f;
                                    print f();
                                    class X {
                                        foo() {
                                        return 21;
                                        }
                                    }
                                    var x = X();
                                    print x.foo();
                                    };
                                    return foo;
                                """),
                        TypeDescriptor.NULL).build());
    }

    @Override
    public Collection<? extends Source> createInvalidSyntaxScripts(Context context) {
        return List.of(
                Source.create("lox", "fooasosaos sassa assas"));
    }

}
