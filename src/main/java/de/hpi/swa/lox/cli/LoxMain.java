package de.hpi.swa.lox.cli;

import java.io.File;
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
    private int iterations;
    private List<String> args = new ArrayList<>(); // everything after the file

    @Override
    protected List<String> preprocessArguments(List<String> arguments, Map<String, String> polyglotOptions) {
        List<String> unrecognized = new ArrayList<>();
        for (int i = 0; i < arguments.size(); i++) {
            var arg = arguments.get(i);
            if (file == null && arg.startsWith("-")) {
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
                    case "-b":
                        if (i >= arguments.size() - 1) {
                            System.err.println("-b must be followed by a number of iterations");
                            System.exit(1);
                        } else {
                            iterations = Integer.parseInt(arguments.get(i + 1));
                            i++;
                        }
                        break;
                    default:
                        unrecognized.add(arg);
                }
            } else {
                if (file == null) {
                    file = Path.of(arg).toFile();
                    if (!file.isFile()) {
                        System.err.println("Cannot access file " + arg);
                        System.exit(1);
                    }
                } else {
                    args.add(arg);
                }
            }
        }
        return unrecognized;
    }

    @Override
    protected void launch(Builder contextBuilder) {
        Source source = null;
        String[] argsArray = args.toArray(new String[args.size()]);
        try (var context = contextBuilder.arguments("lox", argsArray).build()) {

            if (iterations > 0) {
                if (file == null && command == null) {
                    System.err.println("-b cannot be used for the REPL");
                    System.exit(1);
                }
                if (file != null) {
                    // use load to enable relative paths
                    String command = "load(\"" + file.getPath().replace('\\', '/') + "\");nil;";
                    source = Source.newBuilder("lox", command, file.getPath()).buildLiteral();
                } else {
                    source = Source.newBuilder("lox", command, "<command>").buildLiteral();
                }
                long totalTime = 0;
                for (int i = 0; i < iterations; i++) {
                    long t0 = System.nanoTime();
                    try {
                        context.eval(source);
                    } catch (Exception e) {
                        printException(e);
                        System.exit(1);
                    }
                    long t1 = System.nanoTime();
                    long iterationTime = t1 - t0;
                    totalTime += iterationTime;
                    // System.err.println("Iteration " + (i + 1) + ": " + (iterationTime /
                    // 1_000_000) + "ms");
                }
                long averageTime = totalTime / iterations;
                System.err.println("Average time: " + (averageTime / 1_000_000) + "ms");
            } else {
                if (file != null) {
                    // use load to enable relative paths
                    String command = "load(\"" + file.getPath().replace('\\', '/') + "\");nil;";
                    source = Source.newBuilder("lox", command, file.getPath()).buildLiteral();
                    // source = Source.newBuilder("lox", file).build();
                    try {
                        context.eval(source);
                    } catch (Exception e) {
                        printException(e);
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
    }

    private void startEvalLoop(Context context) {
        while (true) {
            System.out.print("> ");
            String line = System.console().readLine();
            if (line == null) {
                break;
            }
            evaluateExpression(context, line);
        }
    }

    public void evaluateExpression(Context context, String line) {
        try {
            var result = context.eval("lox", line);
            if (!result.isNull()) {
                System.out.println(result);
            }
        } catch (Exception e) {
            printException(e);
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
                error.printStackTrace();
            }
        } else {
            System.err.println(error.getMessage());
            error.printStackTrace();
        }

    }
}
