package de.hpi.swa.lox.parser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import de.hpi.swa.lox.bytecode.LoxBytecodeRootNode;
import de.hpi.swa.lox.bytecode.LoxBytecodeRootNodeGen;
import de.hpi.swa.lox.nodes.LoxRootNode;
import de.hpi.swa.lox.parser.LoxParser.ArgumentsContext;
import de.hpi.swa.lox.parser.LoxParser.ArrayAssignmentContext;
import de.hpi.swa.lox.parser.LoxParser.ArrayContext;
import de.hpi.swa.lox.parser.LoxParser.ArrayExprContext;
import de.hpi.swa.lox.parser.LoxParser.AssignmentContext;
import de.hpi.swa.lox.parser.LoxParser.Bit_orContext;
import de.hpi.swa.lox.parser.LoxParser.BlockContext;
import de.hpi.swa.lox.parser.LoxParser.CallArgumentsContext;
import de.hpi.swa.lox.parser.LoxParser.CallContext;
import de.hpi.swa.lox.parser.LoxParser.ClassDeclContext;
import de.hpi.swa.lox.parser.LoxParser.ComparisonContext;
import de.hpi.swa.lox.parser.LoxParser.DeclarationContext;
import de.hpi.swa.lox.parser.LoxParser.EqualityContext;
import de.hpi.swa.lox.parser.LoxParser.ExprStmtContext;
import de.hpi.swa.lox.parser.LoxParser.ExpressionContext;
import de.hpi.swa.lox.parser.LoxParser.FactorContext;
import de.hpi.swa.lox.parser.LoxParser.FalseContext;
import de.hpi.swa.lox.parser.LoxParser.ForStmtContext;
import de.hpi.swa.lox.parser.LoxParser.FunDeclContext;
import de.hpi.swa.lox.parser.LoxParser.FunctionContext;
import de.hpi.swa.lox.parser.LoxParser.HackStmtContext;
import de.hpi.swa.lox.parser.LoxParser.IfStmtContext;
import de.hpi.swa.lox.parser.LoxParser.Logic_andContext;
import de.hpi.swa.lox.parser.LoxParser.Logic_orContext;
import de.hpi.swa.lox.parser.LoxParser.NilContext;
import de.hpi.swa.lox.parser.LoxParser.NumberContext;
import de.hpi.swa.lox.parser.LoxParser.ParametersContext;
import de.hpi.swa.lox.parser.LoxParser.PrintStmtContext;
import de.hpi.swa.lox.parser.LoxParser.ProgramContext;
import de.hpi.swa.lox.parser.LoxParser.RemainderContext;
import de.hpi.swa.lox.parser.LoxParser.ReturnStmtContext;
import de.hpi.swa.lox.parser.LoxParser.StatementContext;
import de.hpi.swa.lox.parser.LoxParser.StringContext;
import de.hpi.swa.lox.parser.LoxParser.SuperExprContext;
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

    private record LocalVariable(BytecodeLocal local, int index) {
    }

    private class LexicalScope {
        public int maxFrameLevel;
        final LexicalScope parent;
        boolean isFunction;
        final Map<String, BytecodeLocal> locals;

        private Stack<LocalVariable> currentStore = new Stack<>();

        LexicalScope(LexicalScope parent, boolean isFunction) {
            this.maxFrameLevel = 0;
            this.parent = parent;
            this.isFunction = isFunction;
            locals = new HashMap<>();
        }

        LexicalScope(LexicalScope parent) {
            this(parent, false);
        }

        LexicalScope() {
            this(null);
        }

        private LocalVariable lookupName(String name) {

            var scope = this;
            BytecodeLocal variable;
            boolean isNonLocal = false;
            var countFunctions = 0;
            do {
                variable = scope.locals.get(name);
                if (variable != null && isNonLocal) {
                    return new LocalVariable(variable, countFunctions);
                }

                if (scope.isFunction) {
                    countFunctions++;
                    // any parent is outside of the function and variable should be nonlocal
                    isNonLocal = true;
                }
                scope = scope.parent;

            } while (variable == null && scope != null);

            return variable != null ? new LocalVariable(variable, -1) : null;
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
                if (variable.index() < 1) {
                    b.beginStoreLocal(variable.local());
                } else {
                    updateMaxScope(variable.index());
                    b.beginStoreLocalMaterialized(variable.local());
                    b.emitLoxLoadMaterialzedFrameN(variable.index());
                }
            } else {
                b.beginLoxWriteGlobalVariable(name);
            }
        }

        public void endStore() {
            var variable = this.currentStore.pop();
            if (variable != null) {
                if (variable.index() < 1) {
                    b.endStoreLocal();
                } else {
                    b.endStoreLocalMaterialized();
                }
            } else {
                b.endLoxWriteGlobalVariable();
            }
        }

        public void load(String text) {
            var variable = lookupName(text);
            if (variable != null) {
                if (variable.index() < 1) {
                    b.beginBlock();
                    b.emitLoxCheckLocalDefined(variable.local());
                    b.emitLoadLocal(variable.local());
                    b.endBlock();
                } else {
                    updateMaxScope(variable.index());
                    b.beginBlock();
                    // todo: check for defition of non-locally declared variables does not work yet
                    // idea: catch the error and throw a custom erorr instead
                    b.beginLoxCheckNonLocalDefined(variable.local());
                    b.emitLoxLoadMaterialzedFrameN(variable.index());
                    b.endLoxCheckNonLocalDefined();
                    b.beginLoadLocalMaterialized(variable.local());
                    b.emitLoxLoadMaterialzedFrameN(variable.index());
                    b.endLoadLocalMaterialized();
                    b.endBlock();

                }
            } else {
                b.emitLoxReadGlobalVariable(text);
            }
        }

        void updateMaxScope(int level) {
            this.maxFrameLevel = Math.max(curScope.maxFrameLevel, level);
            if (this.parent != null) {
                if (this.isFunction) {
                    // make sure the parent also keeps track of deep enough levels of scope
                    this.parent.updateMaxScope(level - 1);
                } else {
                    // non parent functions have the same level, as we count frames and not block
                    // scopes
                    this.parent.updateMaxScope(level);
                }

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
        List<LoxBytecodeRootNode> nodes = LoxBytecodeRootNodeGen.create(language, config, bytecodeParser).getNodes();
        return nodes.get(0).getCallTarget();
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
        // scope for i, etc...
        curScope = new LexicalScope(curScope);
        b.beginBlock();
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
        curScope = curScope.parent;
        b.endBlock();
        return null;
    }

    // Just, a sandbox for playing around with bytecodes
    @Override
    public Void visitHackStmt(HackStmtContext ctx) {

        // b.beginBlock();
        // BytecodeLocal local = b.createLocal();

        // b.beginStoreLocal(local);
        // b.emitLoadConstant(3);
        // b.endStoreLocal();

        // b.beginLoxPrint();
        // b.emitLoadLocal(local);
        // b.endLoxPrint();

        // b.beginBlock();

        // BytecodeLocal local2 = b.createLocal();

        // b.beginStoreLocal(local2);
        // b.emitLoadConstant(4);
        // b.endStoreLocal();

        // b.beginLoxPrint();
        // b.emitLoadLocal(local2);
        // b.endLoxPrint();
        // b.endBlock();

        // b.beginLoxPrint();
        // b.emitLoadLocal(local);
        // b.endLoxPrint();

        // b.endBlock();

        b.beginBlock();

        BytecodeLocal x = b.createLocal();
        BytecodeLocal f = b.createLocal();

        b.beginStoreLocal(x);
        b.emitLoadConstant(41L); // loccal variable x
        b.endStoreLocal();

        b.beginStoreLocal(f);
        b.emitMaterializeFrame(); // store a reference to the current frame in f
        b.endStoreLocal();

        b.beginStoreLocalMaterialized(x); // store value in x in frame f

        b.emitLoadLocal(f); // use reference to current frame

        b.beginLoxAdd();
        b.beginLoadLocalMaterialized(x); // load materialized x from frame f
        b.emitLoadLocal(f);
        b.endLoadLocalMaterialized();
        b.emitLoadConstant(1L);
        b.endLoxAdd();

        b.endStoreLocalMaterialized();

        b.beginLoxPrint();
        b.emitLoadLocal(x);
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
        final boolean isCall = ctx.left != null;
        final boolean isAssignment = ctx.IDENTIFIER() != null;
        if (isCall) {
            String name = ctx.IDENTIFIER().getText();
            b.beginBlock(); // for grouping the the storing an the loading together
            b.beginLoxWriteProperty(name);
            visit(ctx.left);
            visitAssignment(ctx.assignment());
            b.endLoxWriteProperty();
            b.endBlock();
        } else {
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
            b.beginLoxValue();
            visitFactor(ctx.factor(0));
            b.endLoxValue();

            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                b.beginLoxValue();
                visitFactor(ctx.factor((i + 1) / 2));
                b.endLoxValue();
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

    public Void visitLogic_and(Logic_andContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitLogic_and(ctx);
        } else {
            for (int i = ctx.getChildCount() - 2; i >= 0; i -= 2) {
                b.beginLoxAnd();
            }
            visitBit_or(ctx.bit_or(0));
            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                visitBit_or(ctx.bit_or((i + 1) / 2));
                b.endLoxAnd();
            }
        }
        return null;
    }

    public Void visitBit_or(Bit_orContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitBit_or(ctx);
        } else {
            for (int i = ctx.getChildCount() - 2; i >= 0; i -= 2) {
                b.beginLoxBitOr();
            }
            visitBit_and(ctx.bit_and(0));
            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                visitBit_and(ctx.bit_and((i + 1) / 2));
                b.endLoxBitOr();
            }
        }
        return null;
    }

    public Void visitBit_and(LoxParser.Bit_andContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitBit_and(ctx);
        } else {
            for (int i = ctx.getChildCount() - 2; i >= 0; i -= 2) {
                b.beginLoxBitAnd();
            }
            visitEquality(ctx.equality(0));
            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                visitEquality(ctx.equality((i + 1) / 2));
                b.endLoxBitAnd();
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
                        // IDEA b.beginLoxCastToNumber
                        break;
                    case "/":
                        b.beginLoxDiv();
                        break;
                }
            }
            b.beginLoxValue();
            visitRemainder(ctx.remainder(0));
            b.endLoxValue();
            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                b.beginLoxValue();
                visitRemainder(ctx.remainder((i + 1) / 2));
                b.endLoxValue();
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
    public Void visitRemainder(RemainderContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitRemainder(ctx);
        } else {
            for (int i = ctx.getChildCount() - 2; i >= 0; i -= 2) {
                b.beginLoxMod();
            }
            b.beginLoxValue();
            visitUnary(ctx.unary(0));
            b.endLoxValue();
            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                b.beginLoxValue();
                visitUnary(ctx.unary((i + 1) / 2));
                b.endLoxValue();
                b.endLoxMod();
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
            b.beginLoxValue();
            visitTerm(ctx.term(0));
            b.endLoxValue();
            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                b.beginLoxValue();
                visitTerm(ctx.term((i + 1) / 2));
                b.endLoxValue();
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
            b.beginLoxValue();
            visitComparison(ctx.comparison(0));
            b.endLoxValue();
            for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
                b.beginLoxValue();
                visitComparison(ctx.comparison((i + 1) / 2));
                b.endLoxValue();
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
                    b.beginLoxIsTruthy();
                    break;
            }
            b.beginLoxValue();
            visitUnary(ctx.unary());
            b.endLoxValue();
            switch (operation.getText()) {
                case "-":
                    b.endLoxUnaryMinus();
                    break;
                case "!":
                    b.endLoxIsTruthy();
                    b.endLoxNegate();
                    break;
            }

        }
        return null;
    }

    @Override
    public Void visitVarDecl(VarDeclContext ctx) {
        var localName = ctx.IDENTIFIER().getText();
        // TODO: check for reserved words, e.g. super, this, etc.
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
        var expressions = ctx.expression();
        for (int i = expressions.size() - 1; i >= 0; i -= 1) {
            b.beginLoxReadArray();
        }
        visit(ctx.left);
        for (int i = 0; i < expressions.size(); i += 1) {
            visit(expressions.get(i));
            b.endLoxReadArray();
        }
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

    // Functions

    protected final List<TerminalNode> enterFunction(FunctionContext ctx) {
        List<TerminalNode> result = new ArrayList<>();

        curScope = new LexicalScope(curScope, true);
        ParametersContext parameters = ctx.parameters();

        if (parameters != null) {
            for (int i = 0; i < parameters.IDENTIFIER().size(); i++) {
                TerminalNode param = parameters.IDENTIFIER(i);
                result.add(param);
            }
        }

        return result;
    }

    protected final void exitFunction() {
        curScope = curScope.parent;
    }

    @Override
    public Void visitReturnStmt(ReturnStmtContext ctx) {
        b.beginReturn();
        if (ctx.expression() != null) {
            visit(ctx.expression());
        } else {
            b.emitLoadConstant(Nil.INSTANCE);
        }
        b.endReturn();
        return null;
    }

    @Override
    public Void visitFunDecl(FunDeclContext ctx) {
        var function = ctx.function();
        String name = function.IDENTIFIER().getText();
        curScope.define(name, ctx); // declare the function in the outer scope
        // actually store the function in the outer scope
        curScope.beginStore(name);
        visitFunction(function);
        curScope.endStore();
        return null;
    }

    @Override
    public Void visitFunction(FunctionContext function) {
        String name = function.IDENTIFIER().getText();
        b.beginRoot();
        b.beginBlock();
        List<TerminalNode> parameters = enterFunction(function); // enter the function scope
        if (function.getParent() instanceof ClassDeclContext) {
            curScope.define("this", function);
            curScope.beginStore("this");
            b.emitLoxLoadThis();
            curScope.endStore();
        }
        for (int i = 0; i < parameters.size(); i++) {
            var param = parameters.get(i);
            var paramName = param.getText();
            curScope.define(paramName, function); // define each parameter in the innner scope
            curScope.beginStore(paramName);
            b.emitLoxLoadArgument(i); // and fill it with arguments from the stack
            curScope.endStore();
        }
        b.beginBlock();
        visit(function.block()); // visit the function body
        exitFunction();
        b.endBlock();
        b.endBlock();

        b.beginReturn();
        b.emitLoadConstant(Nil.INSTANCE); // default return nil
        b.endReturn();

        LoxRootNode node = b.endRoot();
        node.name = name;
        b.emitLoxCreateFunction(name, node.getCallTarget(), curScope.maxFrameLevel);
        return null;
    }

    @Override
    public Void visitCall(CallContext ctx) {
        var calls = ctx.callArguments();
        for (int i = calls.size() - 1; i >= 0; i -= 1) {
            CallArgumentsContext callArguments = calls.get(i);
            if (callArguments.IDENTIFIER() == null) {
                b.beginLoxCall();
            } else {
                String name = callArguments.IDENTIFIER().getText();
                b.beginLoxReadProperty(name);
            }
        }
        super.visit(ctx.primary());
        for (CallArgumentsContext callArguments : calls) {
            if (callArguments.IDENTIFIER() == null) {
                ArgumentsContext args = callArguments.arguments();
                if (args != null) {
                    List<ExpressionContext> expressions = args.expression();
                    for (int i = 0; i < expressions.size(); i++) {
                        visit(expressions.get(i));
                    }
                }
                b.endLoxCall();
            } else {
                // end lox read
                b.endLoxReadProperty();
            }
        }
        return null;
    }

    @Override
    public Void visitClassDecl(ClassDeclContext ctx) {
        String name = ctx.name.getText();
        curScope.define(name, ctx);
        curScope.beginStore(name);
        curScope = new LexicalScope(curScope);
        curScope.define("super", ctx);

        b.beginLoxDeclareClass(name);
        b.beginBlock();
        if (ctx.extends_ != null) {
            String superclassName = ctx.extends_.getText();
            curScope.beginStore("super");
            curScope.load(superclassName);
            curScope.endStore();
        } else {
            curScope.beginStore("super");
            b.emitLoadConstant(Nil.INSTANCE);
            curScope.endStore();
        }
        curScope.load("super");
        b.endBlock();
        for (var fun : ctx.function()) {
            visitFunction(fun);
        }
        b.endLoxDeclareClass();

        curScope = curScope.parent;
        curScope.endStore();
        return null;
    }

    @Override
    public Void visitSuperExpr(SuperExprContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        b.beginLoxReadSuper(name);
        b.emitLoxLoadThis();
        curScope.load("super");
        b.endLoxReadSuper();
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
