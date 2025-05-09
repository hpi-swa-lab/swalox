package de.hpi.swa.lox.parser;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.bytecode.BytecodeLocal;
import com.oracle.truffle.api.bytecode.BytecodeParser;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;

import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.bytecode.LoxBytecodeRootNodeGen;
import de.hpi.swa.lox.parser.LoxParser.ArrayAssignmentContext;
import de.hpi.swa.lox.parser.LoxParser.ArrayContext;
import de.hpi.swa.lox.parser.LoxParser.ArrayExprContext;
import de.hpi.swa.lox.parser.LoxParser.AssignmentContext;
import de.hpi.swa.lox.parser.LoxParser.BlockContext;
import de.hpi.swa.lox.parser.LoxParser.ComparisonContext;
import de.hpi.swa.lox.parser.LoxParser.DeclarationContext;
import de.hpi.swa.lox.parser.LoxParser.EqualityContext;
import de.hpi.swa.lox.parser.LoxParser.ExprStmtContext;
import de.hpi.swa.lox.parser.LoxParser.FactorContext;
import de.hpi.swa.lox.parser.LoxParser.FalseContext;
import de.hpi.swa.lox.parser.LoxParser.ForStmtContext;
import de.hpi.swa.lox.parser.LoxParser.HackStmtContext;
import de.hpi.swa.lox.parser.LoxParser.IfStmtContext;
import de.hpi.swa.lox.parser.LoxParser.Logic_orContext;
import de.hpi.swa.lox.parser.LoxParser.NilContext;
import de.hpi.swa.lox.parser.LoxParser.NumberContext;
import de.hpi.swa.lox.parser.LoxParser.PrintStmtContext;
import de.hpi.swa.lox.parser.LoxParser.ProgramContext;
import de.hpi.swa.lox.parser.LoxParser.StatementContext;
import de.hpi.swa.lox.parser.LoxParser.StringContext;
import de.hpi.swa.lox.parser.LoxParser.TermContext;
import de.hpi.swa.lox.parser.LoxParser.TrueContext;
import de.hpi.swa.lox.parser.LoxParser.UnaryContext;
import de.hpi.swa.lox.parser.LoxParser.VarDeclContext;
import de.hpi.swa.lox.parser.LoxParser.VariableExprContext;
import de.hpi.swa.lox.parser.LoxParser.WhileStmtContext;
import de.hpi.swa.lox.runtime.objects.Nil;

/**
 * Lox AST visitor that parses to Bytecode DSL bytecode.
 */
public final class LoxBytecodeCompiler extends LoxBaseVisitor<Void> {

    protected final LoxLanguage language;
    protected final Source source;

    private final LoxBytecodeRootNodeGen.Builder b;

    private LexicalScope curScope = null;

    private class LexicalScope {
        final LexicalScope parent;
        final Map<String, BytecodeLocal> locals;
        private Stack<Object> currentStore = new Stack<>();

        LexicalScope(LexicalScope parent) {
            this.parent = parent;
            locals = new HashMap<>();
        }

        LexicalScope() {
            this(null);
        }

        private BytecodeLocal lookupName(String name) {
            var scope = this;
            BytecodeLocal variable;
            do {
                variable = scope.locals.get(name);
                scope = scope.parent;
            } while (variable == null && scope != null);
            return variable;
        }

        public void define(String localName, ParseTree ctx) {
            if (parent != null) {
                if (locals.get(localName) != null) {
                    throw LoxParseError.build(source, ctx, "Variable " + localName + " already defined");
                }
                var local = b.createLocal(localName, null);
                locals.put(localName, local);
            } else {
                b.emitLoxDefineGlobalVariable(localName);
            }
        }

        public void beginStore(String name) {
            var variable = lookupName(name);
            this.currentStore.push(variable);
            if (variable != null) {
                b.beginStoreLocal(variable);
            } else {
                b.beginLoxWriteGlobalVariable(name);
            }
        }

        public void endStore() {
            var currentStore = this.currentStore.pop();
            if (currentStore != null) {
                b.endStoreLocal();
            } else {
                b.endLoxWriteGlobalVariable();
            }
        }

        public void load(String text) {
            var variable = lookupName(text);
            if (variable != null) {
                b.beginBlock();
                b.emitLoxCheckLocalDefined(variable);
                b.emitLoadLocal(variable);
                b.endBlock();
            } else {
                b.emitLoxReadGlobalVariable(text);
            }
        }
    }

    public static RootCallTarget parseLox(LoxLanguage language, Source source) {
        BytecodeParser<LoxBytecodeRootNodeGen.Builder> bytecodeParser = (b) -> {
            LoxBytecodeCompiler visitor = new LoxBytecodeCompiler(language, source, b);
            b.beginSource(source);
            LoxLexer lexer = new LoxLexer(CharStreams.fromString(source.getCharacters().toString()));
            LoxParser loxParser = new LoxParser(new CommonTokenStream(lexer));

            lexer.removeErrorListeners();
            loxParser.removeErrorListeners();
            BailoutErrorListener listener = new BailoutErrorListener(source);
            lexer.addErrorListener(listener);
            loxParser.addErrorListener(listener);

            loxParser.program().accept(visitor);
            b.endSource();
        };
        var config = LoxBytecodeRootNodeGen.newConfigBuilder().addSource().build();
        var nodes = LoxBytecodeRootNodeGen.create(language, config, bytecodeParser).getNodes();
        return nodes.get(nodes.size() - 1).getCallTarget();
    }

    private static final class BailoutErrorListener extends BaseErrorListener {
        private final Source source;

        BailoutErrorListener(Source source) {
            this.source = source;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                String msg, RecognitionException e) {
            throwParseError(source, line, charPositionInLine, (Token) offendingSymbol, msg);
        }
    }

    private static void throwParseError(Source source, int line, int charPositionInLine, Token token, String message) {
        throw LoxParseError.build(source, line, charPositionInLine, token, message);
    }

    private LoxBytecodeCompiler(LoxLanguage language, Source source, LoxBytecodeRootNodeGen.Builder builder) {
        this.language = language;
        this.source = source;
        this.b = builder;
    }

    // Operations

    @Override
    public Void visitProgram(ProgramContext ctx) {
        beginAttribution(ctx);
        b.beginRoot();
        curScope = new LexicalScope();
        super.visitProgram(ctx);
        b.beginReturn();
        b.emitLoadConstant(Nil.INSTANCE);
        b.endReturn();
        curScope = null;
        b.endRoot();
        endAttribution();
        return null;
    }

    @Override

    public Void visitStatement(StatementContext ctx) {
        ParseTree tree = ctx;
        beginAttribution(tree);
        super.visitStatement(ctx);
        endAttribution();
        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmtContext ctx) {
        beginAttribution(ctx);
        b.beginLoxPrint();
        super.visitPrintStmt(ctx);
        b.endLoxPrint();
        endAttribution();
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmtContext ctx) {
        if (ctx.parent instanceof StatementContext statement) {
            if (statement.parent instanceof DeclarationContext declaration) {
                if (declaration.parent instanceof ProgramContext program) {
                    if (declaration == program.getChild(program.getChildCount() - 2)) {
                        // implicit return last statement
                        b.beginReturn();
                        super.visitExprStmt(ctx);
                        b.endReturn();
                        return null;
                    }
                }
            }
        }
        super.visitExprStmt(ctx);
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmtContext ctx) {
        if (ctx.alt == null) {
            b.beginIfThen();
            beginAttribution(ctx.condition);
            b.beginLoxIsTruthy();
            visit(ctx.condition);
            b.endLoxIsTruthy();
            endAttribution();
            visit(ctx.then);
            b.endIfThen();
        } else {
            b.beginIfThenElse();
            beginAttribution(ctx.condition);
            b.beginLoxIsTruthy();
            visit(ctx.condition);
            b.endLoxIsTruthy();
            endAttribution();
            visit(ctx.then);
            visit(ctx.alt);
            b.endIfThenElse();
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmtContext ctx) {
        b.beginWhile();
        beginAttribution(ctx.condition);
        b.beginLoxIsTruthy();
        visit(ctx.condition);
        b.endLoxIsTruthy();
        endAttribution();
        visit(ctx.body);
        b.endWhile();
        return null;
    }

    @Override
    public Void visitForStmt(ForStmtContext ctx) {
        ParserRuleContext init = ctx.varDecl();
        if (init == null) {
            init = ctx.exprStmt();
        }
        if (init != null) {
            visit(init);
        }
        b.beginWhile();
        beginAttribution(ctx.condition);
        b.beginLoxIsTruthy();
        visit(ctx.condition);
        b.endLoxIsTruthy();
        endAttribution();
        b.beginBlock();
        if (ctx.body != null)
            visit(ctx.body);
        if (ctx.increment != null)
            visit(ctx.increment);
        b.endBlock();
        b.endWhile();
        return null;
    }

    // Just, a sandbox for playing around with bytecodes
    @Override
    public Void visitHackStmt(HackStmtContext ctx) {

        b.beginBlock();
        BytecodeLocal local = b.createLocal();

        b.beginStoreLocal(local);
        b.emitLoadConstant(3);
        b.endStoreLocal();

        b.beginLoxPrint();
        b.emitLoadLocal(local);
        b.endLoxPrint();

        b.beginBlock();

        BytecodeLocal local2 = b.createLocal();

        b.beginStoreLocal(local2);
        b.emitLoadConstant(4);
        b.endStoreLocal();

        b.beginLoxPrint();
        b.emitLoadLocal(local2);
        b.endLoxPrint();
        b.endBlock();

        b.beginLoxPrint();
        b.emitLoadLocal(local);
        b.endLoxPrint();

        b.endBlock();
        return null;
    }

    @Override
    public Void visitHaltStmt(LoxParser.HaltStmtContext ctx) {
        b.emitLoxHalt();
        return null;
    }

    @Override
    public Void visitBlock(BlockContext ctx) {
        b.beginBlock();
        curScope = new LexicalScope(curScope);
        super.visitBlock(ctx);
        curScope = curScope.parent;
        b.endBlock();
        return null;
    }

    @Override
    public Void visitAssignment(AssignmentContext ctx) {
        final boolean isAssignment = ctx.IDENTIFIER() != null;
        String text = null;
        if (isAssignment) {
            b.beginBlock(); // for grouping the the storing an the loading together
            text = ctx.IDENTIFIER().getText();
            curScope.beginStore(text);
        }
        super.visitAssignment(ctx);
        if (isAssignment) {
            curScope.endStore();
            curScope.load(text); // for the value of the assignment
            b.endBlock();
        }
        return null;
    }

    @Override
    public Void visitTerm(TermContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitTerm(ctx);
        } else {
            for (int i = ctx.getChildCount() - 2; i >= 0; i -= 2) {
                var operation = ctx.getChild(i);
                switch (operation.getText()) {
                    case "+":
                        b.beginLoxAdd();
                        break;
                    case "-":
                        b.beginLoxSub();
                        break;
                }
            }
            visitFactor(ctx.factor(0));
            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                visitFactor(ctx.factor((i + 1) / 2));
                var operation = ctx.getChild(i);
                switch (operation.getText()) {
                    case "+":
                        b.endLoxAdd();
                        break;
                    case "-":
                        b.endLoxSub();
                        break;
                }
            }
        }
        return null;
    }

    @Override
    public Void visitLogic_or(Logic_orContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitLogic_or(ctx);
        } else {
            for (int i = ctx.getChildCount() - 2; i >= 0; i -= 2) {
                b.beginLoxOr();
            }
            visitLogic_and(ctx.logic_and(0));
            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                visitLogic_and(ctx.logic_and((i + 1) / 2));
                b.endLoxOr();
            }
        }
        return null;
    }

    public Void visitLogic_and(LoxParser.Logic_andContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitLogic_and(ctx);
        } else {
            for (int i = ctx.getChildCount() - 2; i >= 0; i -= 2) {
                b.beginLoxAnd();
            }
            visitEquality(ctx.equality(0));
            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                visitEquality(ctx.equality((i + 1) / 2));
                b.endLoxAnd();
            }
        }
        return null;
    }

    @Override
    public Void visitFactor(FactorContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitFactor(ctx);
        } else {
            for (int i = ctx.getChildCount() - 2; i >= 0; i -= 2) {
                var operation = ctx.getChild(i);
                switch (operation.getText()) {
                    case "*":
                        b.beginLoxMul();
                        break;
                    case "/":
                        b.beginLoxDiv();
                        break;
                }
            }
            visitUnary(ctx.unary(0));
            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                visitUnary(ctx.unary((i + 1) / 2));
                var operation = ctx.getChild(i);
                switch (operation.getText()) {
                    case "*":
                        b.endLoxMul();
                        break;
                    case "/":
                        b.endLoxDiv();
                        break;
                }
            }
        }
        return null;
    }

    @Override
    public Void visitComparison(ComparisonContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitComparison(ctx);
        } else {
            beginAttribution(ctx);
            for (int i = ctx.getChildCount() - 2; i >= 0; i -= 2) {
                var operation = ctx.getChild(i);
                switch (operation.getText()) {
                    case "<":
                        b.beginLoxLess();
                        break;
                    case "<=":
                        b.beginLoxLessEqual();
                        break;
                    case ">":
                        b.beginLoxGreater();
                        break;
                    case ">=":
                        b.beginLoxGreaterEqual();
                        break;
                }
            }
            visitTerm(ctx.term(0));
            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                visitTerm(ctx.term((i + 1) / 2));
                var operation = ctx.getChild(i);
                switch (operation.getText()) {
                    case "<":
                        b.endLoxLess();
                        break;
                    case "<=":
                        b.endLoxLessEqual();
                        break;
                    case ">":
                        b.endLoxGreater();
                        break;
                    case ">=":
                        b.endLoxGreaterEqual();
                        break;
                }
            }
            endAttribution();
        }
        return null;
    }

    @Override
    public Void visitEquality(EqualityContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitEquality(ctx);
        } else {
            for (int i = ctx.getChildCount() - 2; i >= 0; i -= 2) {
                var operation = ctx.getChild(i);
                switch (operation.getText()) {
                    case "==":
                        b.beginLoxEqual();
                        break;
                    case "!=":
                        b.beginLoxNotEqual();
                        break;
                }
            }
            visitComparison(ctx.comparison(0));
            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                visitComparison(ctx.comparison((i + 1) / 2));
                var operation = ctx.getChild(i);
                switch (operation.getText()) {
                    case "==":
                        b.endLoxEqual();
                        break;
                    case "!=":
                        b.endLoxNotEqual();
                        break;
                }
            }
        }
        return null;

    }

    @Override
    public Void visitUnary(UnaryContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitUnary(ctx);
        } else {
            var operation = ctx.getChild(0);
            switch (operation.getText()) {
                case "-":
                    b.beginLoxUnaryMinus();
                    break;
                case "!":
                    b.beginLoxNegate();
                    break;
            }
            visitUnary(ctx.unary());
            switch (operation.getText()) {
                case "-":
                    b.endLoxUnaryMinus();
                    break;
                case "!":
                    b.endLoxNegate();
                    break;
            }

        }
        return null;
    }

    @Override
    public Void visitVarDecl(VarDeclContext ctx) {
        var localName = ctx.IDENTIFIER().getText();
        curScope.define(localName, ctx);
        if (ctx.expression() != null) {
            curScope.beginStore(localName);
            visit(ctx.expression());
            curScope.endStore();
        }
        return null;
    }

    @Override
    public Void visitVariableExpr(VariableExprContext ctx) {
        curScope.load(ctx.getText());
        return null;
    }

    // Data Types

    @Override
    public Void visitNumber(NumberContext ctx) {
        if (ctx.getText().contains(".")) {
            b.emitLoadConstant(Double.parseDouble(ctx.getText()));
        } else {
            try {
                b.emitLoadConstant(Long.parseLong(ctx.getText()));
            } catch (NumberFormatException e) {
                // throw LoxParseError.build(source, ctx, "Number is to big");
                // for big numbers would use need to use BigInteger
                b.emitLoadConstant(new BigInteger(ctx.getText()));
            }
        }
        return super.visitNumber(ctx);
    }

    @Override
    public Void visitString(StringContext ctx) {
        // make sure all our Strings are TruffleStrings, because performance
        var ts = TruffleString.fromJavaStringUncached(ctx.getText().substring(1, ctx.getText().length() - 1),
                TruffleString.Encoding.UTF_8);
        b.emitLoadConstant(ts);
        return super.visitString(ctx);
    }

    @Override
    public Void visitTrue(TrueContext ctx) {
        b.emitLoadConstant(true);
        return super.visitTrue(ctx);
    }

    @Override
    public Void visitFalse(FalseContext ctx) {
        b.emitLoadConstant(false);
        return super.visitFalse(ctx);
    }

    @Override
    public Void visitNil(NilContext ctx) {
        b.emitLoadConstant(Nil.INSTANCE);
        return super.visitNil(ctx);
    }

    @Override
    public Void visitArray(ArrayContext ctx) {
        b.emitLoxNewArray();
        return super.visitArray(ctx);
    }

    @Override
    public Void visitArrayExpr(ArrayExprContext ctx) {
        b.beginLoxReadArray();
        visit(ctx.left);
        visit(ctx.index);
        b.endLoxReadArray();
        return null;
    }

    @Override
    public Void visitArrayAssignment(ArrayAssignmentContext ctx) {
        if (ctx.other != null) {
            return visit(ctx.other);
        }
        b.beginLoxWriteArray();
        visit(ctx.left);
        visit(ctx.index);
        visit(ctx.right);
        b.endLoxWriteArray();
        return null;
    }

    // Attributions

    private void beginAttribution(ParseTree tree) {
        beginAttribution(getStartIndex(tree), getEndIndex(tree));
    }

    private static int getEndIndex(ParseTree tree) {
        if (tree instanceof ParserRuleContext ctx) {
            return ctx.getStop().getStopIndex();
        } else if (tree instanceof TerminalNode node) {
            return node.getSymbol().getStopIndex();
        } else {
            throw new AssertionError("unknown tree type: " + tree);
        }
    }

    private static int getStartIndex(ParseTree tree) {
        if (tree instanceof ParserRuleContext ctx) {
            return ctx.getStart().getStartIndex();
        } else if (tree instanceof TerminalNode node) {
            return node.getSymbol().getStartIndex();
        } else {
            throw new AssertionError("unknown tree type: " + tree);
        }
    }

    private void beginAttribution(int start, int end) {
        int length = end - start + 1;
        assert length >= 0;
        b.beginSourceSection(start, length);
    }

    private void endAttribution() {
        b.endSourceSection();
    }
}
