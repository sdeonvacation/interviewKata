package dev.interviewkata.sandbox;

import dev.interviewkata.config.JShellConfig;
import jdk.jshell.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.*;

/**
 * Executes user-submitted Java code in a JShell sandbox with timeout enforcement.
 * Provides method-level execution and test case validation.
 */
@Component
public class JShellSandbox {

    private static final Logger log = LoggerFactory.getLogger(JShellSandbox.class);

    private final JShellConfig config;
    private final ExecutorService executor;

    public JShellSandbox(JShellConfig config) {
        this.config = config;
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "jshell-sandbox");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Executes user code against all test cases with timeout enforcement.
     */
    public TestResult executeWithTests(String userCode, String starterCode, List<TestCase> testCases) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = config.getTimeoutMs();

        Future<TestResult> future = executor.submit(() ->
                runTests(userCode, starterCode, testCases, startTime));

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("JShell execution timed out after {}ms", elapsed);
            return TestResult.ofTimeout(elapsed);
        } catch (ExecutionException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            log.error("JShell execution error: {}", msg);
            return TestResult.ofCompilationError(msg, elapsed);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long elapsed = System.currentTimeMillis() - startTime;
            return TestResult.ofCompilationError("Execution interrupted", elapsed);
        }
    }

    /**
     * Executes raw code and returns the output (for simple evaluation).
     */
    public ExecutionResult execute(String code) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = config.getTimeoutMs();

        Future<ExecutionResult> future = executor.submit(() -> runCode(code, startTime));

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return ExecutionResult.timeout(System.currentTimeMillis() - startTime);
        } catch (ExecutionException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            return ExecutionResult.error("", msg, elapsed);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long elapsed = System.currentTimeMillis() - startTime;
            return ExecutionResult.error("", "Execution interrupted", elapsed);
        }
    }

    private TestResult runTests(String userCode, String starterCode, List<TestCase> testCases, long startTime) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream);

        try (JShell jshell = createJShell(printStream)) {
            // Load the user's Solution class
            String compilationError = loadUserCode(jshell, userCode);
            if (compilationError != null) {
                long elapsed = System.currentTimeMillis() - startTime;
                return TestResult.ofCompilationError(compilationError, elapsed);
            }

            // Parse method signature from starter code
            InputParser.MethodSignature sig = InputParser.parseMethodSignature(starterCode);

            // Execute each test case
            List<TestCaseResult> results = new ArrayList<>();
            for (TestCase testCase : testCases) {
                if (Thread.currentThread().isInterrupted()) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    return TestResult.ofTimeout(elapsed);
                }
                TestCaseResult tcResult = runSingleTest(jshell, sig, testCase);
                results.add(tcResult);
            }

            long elapsed = System.currentTimeMillis() - startTime;
            return TestResult.ofResults(results, elapsed);
        }
    }

    private TestCaseResult runSingleTest(JShell jshell, InputParser.MethodSignature sig, TestCase testCase) {
        try {
            Map<String, String> assignments = InputParser.parseInputAssignments(testCase.input());
            String invocation = InputParser.buildInvocation(sig, assignments);
            String resultToString = InputParser.buildResultToString(sig.returnType());

            // Assign result to variable
            String evalCode = sig.returnType() + " __result__ = " + invocation + ";";
            String evalError = evalSnippet(jshell, evalCode);
            if (evalError != null) {
                return TestCaseResult.error(testCase.description(), testCase.expected(), evalError);
            }

            // Convert result to string for comparison
            String toStringCode = "String __actual__ = " + resultToString + ";";
            evalError = evalSnippet(jshell, toStringCode);
            if (evalError != null) {
                return TestCaseResult.error(testCase.description(), testCase.expected(), evalError);
            }

            // Get the actual value
            String actual = getVariableValue(jshell, "__actual__");
            if (actual == null) {
                return TestCaseResult.error(testCase.description(), testCase.expected(),
                        "Could not retrieve result value");
            }

            // Normalize and compare
            String normalizedActual = normalize(actual);
            String normalizedExpected = normalize(testCase.expected());

            if (normalizedActual.equals(normalizedExpected)) {
                return TestCaseResult.success(testCase.description(), actual, testCase.expected());
            } else {
                return TestCaseResult.failure(testCase.description(), actual, testCase.expected());
            }
        } catch (Exception e) {
            return TestCaseResult.error(testCase.description(), testCase.expected(), e.getMessage());
        }
    }

    private ExecutionResult runCode(String code, long startTime) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream);

        try (JShell jshell = createJShell(printStream)) {
            String error = loadUserCode(jshell, code);
            if (error != null) {
                long elapsed = System.currentTimeMillis() - startTime;
                return ExecutionResult.error("", error, elapsed);
            }
            long elapsed = System.currentTimeMillis() - startTime;
            String output = outStream.toString().trim();
            return ExecutionResult.success(output, elapsed);
        }
    }

    private JShell createJShell(PrintStream out) {
        return JShell.builder()
                .out(out)
                .err(out)
                .remoteVMOptions(
                        "-Xmx" + config.getMaxHeapMb() + "m",
                        "--add-modules", String.join(",", config.getAllowedModules())
                )
                .build();
    }

    /**
     * Loads user code into JShell. Returns compilation error message or null on success.
     */
    private String loadUserCode(JShell jshell, String code) {
        // JShell handles class definitions directly
        List<SnippetEvent> events = jshell.eval(code);

        StringBuilder errors = new StringBuilder();
        for (SnippetEvent event : events) {
            if (event.status() == Snippet.Status.REJECTED) {
                // Collect diagnostic messages
                List<Diag> diagnostics = jshell.diagnostics(event.snippet()).toList();
                for (Diag diag : diagnostics) {
                    if (errors.length() > 0) errors.append("\n");
                    errors.append(diag.getMessage(Locale.ENGLISH));
                }
                if (errors.isEmpty()) {
                    errors.append("Compilation failed");
                }
            } else if (event.exception() != null) {
                if (errors.length() > 0) errors.append("\n");
                errors.append(event.exception().getMessage());
            }
        }

        return errors.isEmpty() ? null : truncateOutput(errors.toString());
    }

    /**
     * Evaluates a single snippet, returns error message or null on success.
     */
    private String evalSnippet(JShell jshell, String code) {
        List<SnippetEvent> events = jshell.eval(code);

        for (SnippetEvent event : events) {
            if (event.status() == Snippet.Status.REJECTED) {
                List<Diag> diagnostics = jshell.diagnostics(event.snippet()).toList();
                StringBuilder errors = new StringBuilder();
                for (Diag diag : diagnostics) {
                    if (errors.length() > 0) errors.append("\n");
                    errors.append(diag.getMessage(Locale.ENGLISH));
                }
                return errors.isEmpty() ? "Evaluation failed" : errors.toString();
            }
            if (event.exception() != null) {
                return event.exception().getMessage();
            }
        }
        return null;
    }

    /**
     * Retrieves the string value of a variable from JShell.
     */
    private String getVariableValue(JShell jshell, String varName) {
        List<SnippetEvent> events = jshell.eval(varName);
        for (SnippetEvent event : events) {
            if (event.value() != null) {
                String val = event.value();
                // JShell wraps string values in quotes
                if (val.startsWith("\"") && val.endsWith("\"")) {
                    return val.substring(1, val.length() - 1);
                }
                return val;
            }
        }
        return null;
    }

    /**
     * Normalizes output for comparison: removes whitespace differences.
     */
    static String normalize(String value) {
        if (value == null) return "";
        return value.replaceAll("\\s+", "");
    }

    private String truncateOutput(String output) {
        int max = config.getMaxOutputBytes();
        if (output.length() > max) {
            return output.substring(0, max) + "... (truncated)";
        }
        return output;
    }
}
