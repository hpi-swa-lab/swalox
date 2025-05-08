package de.hpi.swa.lox.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.graalvm.launcher.AbstractLanguageLauncher;
import org.graalvm.options.OptionCategory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Context.Builder;
import org.graalvm.polyglot.PolyglotException;

import org.graalvm.polyglot.Source;

public class LoxMain extends AbstractLanguageLauncher {
    public static void main(String[] args) {
        new LoxMain().launch(args);
    }

    private String command;
    private File file;

    @Override
    protected List<String> preprocessArguments(List<String> arguments, Map<String, String> polyglotOptions) {
        List<String> unrecognized = new ArrayList<>();
        for (int i = 0; i < arguments.size(); i++) {
            var arg = arguments.get(i);
            if (arg.startsWith("-")) {
                switch (arg) {
                    case "-c":
                        if (i != arguments.size() - 2) {
                            System.err.println("-c must be the last argument followed by a lox expression");
                            System.exit(1);
                        } else {
                            command = arguments.get(i + 1);
                            i++;
                        }
                        break;
                    default:
                        unrecognized.add(arg);
                }
            } else if (i != arguments.size() - 1) {
                System.err.println("filename must be the last argument");
                System.exit(1);
            } else {
                file = Path.of(arg).toFile();
                if (!file.isFile()) {
                    System.err.println("Cannot access file " + arg);
                    System.exit(1);
                }
            }
        }
        return unrecognized;
    }

    @Override
    protected void launch(Builder contextBuilder) {
        Source source;
        try (var context = contextBuilder.build()) {

            // FOR TESTING
            // command = "print true;";

            if (file != null) {
                try {
                    source = Source.newBuilder("lox", file).build();
                    try {
                        context.eval(source);
                    } catch (Exception e) {
                        printException(e);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (command != null) {
                try {
                    context.eval("lox", command);
                } catch (Exception e) {
                    printException(e);
                }
            } else {
                startEvalLoop(context);
            }
        }
    }

    private void startEvalLoop(Context context) {
        while (true) {
            System.out.print("> ");
            String line = System.console().readLine();
            if (line == null) {
                break;
            }
            try {
                context.eval("lox", line);
            } catch (Exception e) {
                printException(e);
            }
        }
    }

    @Override
    protected String getLanguageId() {
        return "lox";
    }

    @Override
    protected void printHelp(OptionCategory maxCategory) {
        System.out.println("Usage: lox [option] ... (@filename | command)");
    }

    public static void printException(Exception e) {
        if (e instanceof PolyglotException error) {
            runtimeError(error);
        } else {
            System.err.println("Error: " + e.getMessage());
        }
    }

    static void runtimeError(PolyglotException error) {
        if (error.isSyntaxError()) {
            System.err.println(error.getMessage());
        } else if (error.isGuestException()) {
            var sourceLocation = error.getSourceLocation();
            if (sourceLocation != null) {
                System.err.println("[line " + sourceLocation.getStartLine() + "] " + "Error: " + error.getMessage());
            } else {
                System.err.println("Error: " + error.getMessage());
            }
        } else {
            System.err.println(error.getMessage());
        }

    }
}
