package dev.interviewkata.sandbox;

import dev.interviewkata.config.JShellConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class JShellSandboxTest {

    private JShellSandbox sandbox;

    @BeforeEach
    void setUp() {
        JShellConfig config = new JShellConfig();
        config.setTimeoutMs(10000);
        config.setMaxHeapMb(128);
        config.setMaxOutputBytes(10240);
        config.setAllowedModules(List.of("java.base", "jdk.jshell"));
        sandbox = new JShellSandbox(config);
    }

    @Test
    void executeSimpleCode() {
        ExecutionResult result = sandbox.execute("int x = 2 + 3;");

        assertTrue(result.success());
        assertNull(result.errorMessage());
    }

    @Test
    void executeCodeWithCompilationError() {
        ExecutionResult result = sandbox.execute("int x = ;");

        assertFalse(result.success());
        assertNotNull(result.errorMessage());
    }

    @Test
    void correctTwoSumSolutionPassesAllTests() {
        String starterCode = """
                public class Solution {
                    public int[] twoSum(int[] nums, int target) {
                        // solution
                    }
                }
                """;

        String userCode = """
                public class Solution {
                    public int[] twoSum(int[] nums, int target) {
                        for (int i = 0; i < nums.length; i++) {
                            for (int j = i + 1; j < nums.length; j++) {
                                if (nums[i] + nums[j] == target) {
                                    return new int[]{i, j};
                                }
                            }
                        }
                        return new int[]{};
                    }
                }
                """;

        List<TestCase> testCases = List.of(
                new TestCase("nums = [2,7,11,15], target = 9", "[0,1]", "Basic case"),
                new TestCase("nums = [3,2,4], target = 6", "[1,2]", "Non-adjacent pair")
        );

        TestResult result = sandbox.executeWithTests(userCode, starterCode, testCases);

        assertTrue(result.allPassed());
        assertEquals(2, result.passed());
        assertEquals(0, result.failed());
        assertEquals(2, result.total());
        assertFalse(result.isTimeout());
        assertFalse(result.hasCompilationError());
    }

    @Test
    void wrongSolutionFailsTests() {
        String starterCode = """
                public class Solution {
                    public int[] twoSum(int[] nums, int target) {
                        // solution
                    }
                }
                """;

        String userCode = """
                public class Solution {
                    public int[] twoSum(int[] nums, int target) {
                        return new int[]{0, 0};
                    }
                }
                """;

        List<TestCase> testCases = List.of(
                new TestCase("nums = [2,7,11,15], target = 9", "[0,1]", "Basic case"),
                new TestCase("nums = [3,2,4], target = 6", "[1,2]", "Non-adjacent pair")
        );

        TestResult result = sandbox.executeWithTests(userCode, starterCode, testCases);

        assertFalse(result.allPassed());
        assertEquals(2, result.total());
        assertTrue(result.failed() > 0);

        // Verify details contain actual vs expected
        TestCaseResult firstFail = result.details().stream()
                .filter(d -> !d.passed())
                .findFirst()
                .orElseThrow();
        assertNotNull(firstFail.actual());
        assertNotNull(firstFail.expected());
    }

    @Test
    void compilationErrorReturnsErrorResult() {
        String starterCode = """
                public class Solution {
                    public int solve(int n) { return 0; }
                }
                """;

        String userCode = """
                public class Solution {
                    public int solve(int n) {
                        reutrn n * 2;
                    }
                }
                """;

        List<TestCase> testCases = List.of(
                new TestCase("n = 5", "10", "Simple double")
        );

        TestResult result = sandbox.executeWithTests(userCode, starterCode, testCases);

        assertTrue(result.hasCompilationError());
        assertNotNull(result.compilationError());
        assertFalse(result.compilationError().isBlank());
    }

    @Test
    void infiniteLoopTimesOut() {
        // Use short timeout for this test
        JShellConfig shortConfig = new JShellConfig();
        shortConfig.setTimeoutMs(2000);
        shortConfig.setMaxHeapMb(128);
        shortConfig.setMaxOutputBytes(10240);
        shortConfig.setAllowedModules(List.of("java.base", "jdk.jshell"));
        JShellSandbox shortSandbox = new JShellSandbox(shortConfig);

        String starterCode = """
                public class Solution {
                    public int solve(int n) { return 0; }
                }
                """;

        String userCode = """
                public class Solution {
                    public int solve(int n) {
                        while (true) {}
                    }
                }
                """;

        List<TestCase> testCases = List.of(
                new TestCase("n = 5", "10", "Simple case")
        );

        TestResult result = shortSandbox.executeWithTests(userCode, starterCode, testCases);

        assertTrue(result.isTimeout());
        assertTrue(result.totalDurationMs() >= 2000);
    }

    @Test
    void stringSolutionComparesCorrectly() {
        String starterCode = """
                public class Solution {
                    public String reverse(String s) { return ""; }
                }
                """;

        String userCode = """
                public class Solution {
                    public String reverse(String s) {
                        return new StringBuilder(s).reverse().toString();
                    }
                }
                """;

        List<TestCase> testCases = List.of(
                new TestCase("s = \"hello\"", "olleh", "Simple reverse"),
                new TestCase("s = \"ab\"", "ba", "Two chars")
        );

        TestResult result = sandbox.executeWithTests(userCode, starterCode, testCases);

        assertTrue(result.allPassed());
        assertEquals(2, result.passed());
    }

    @Test
    void intReturnComparesCorrectly() {
        String starterCode = """
                public class Solution {
                    public int add(int a, int b) { return 0; }
                }
                """;

        String userCode = """
                public class Solution {
                    public int add(int a, int b) {
                        return a + b;
                    }
                }
                """;

        List<TestCase> testCases = List.of(
                new TestCase("a = 2, b = 3", "5", "Basic addition"),
                new TestCase("a = -1, b = 1", "0", "Negative number")
        );

        TestResult result = sandbox.executeWithTests(userCode, starterCode, testCases);

        assertTrue(result.allPassed());
    }

    @Test
    void booleanReturnComparesCorrectly() {
        String starterCode = """
                public class Solution {
                    public boolean isEven(int n) { return false; }
                }
                """;

        String userCode = """
                public class Solution {
                    public boolean isEven(int n) {
                        return n % 2 == 0;
                    }
                }
                """;

        List<TestCase> testCases = List.of(
                new TestCase("n = 4", "true", "Even number"),
                new TestCase("n = 3", "false", "Odd number")
        );

        TestResult result = sandbox.executeWithTests(userCode, starterCode, testCases);

        assertTrue(result.allPassed());
    }

    @Test
    void runtimeExceptionReportsError() {
        String starterCode = """
                public class Solution {
                    public int divide(int a, int b) { return 0; }
                }
                """;

        String userCode = """
                public class Solution {
                    public int divide(int a, int b) {
                        return a / b;
                    }
                }
                """;

        List<TestCase> testCases = List.of(
                new TestCase("a = 10, b = 2", "5", "Normal division"),
                new TestCase("a = 10, b = 0", "0", "Division by zero")
        );

        TestResult result = sandbox.executeWithTests(userCode, starterCode, testCases);

        // First test should pass, second should error
        assertEquals(2, result.total());
        assertTrue(result.details().get(0).passed());
        assertFalse(result.details().get(1).passed());
        assertNotNull(result.details().get(1).error());
    }

    @Test
    void emptyTestCasesReturnsEmptyResult() {
        String starterCode = """
                public class Solution {
                    public int solve(int n) { return 0; }
                }
                """;

        String userCode = """
                public class Solution {
                    public int solve(int n) { return n; }
                }
                """;

        TestResult result = sandbox.executeWithTests(userCode, starterCode, List.of());

        assertEquals(0, result.total());
        assertFalse(result.allPassed()); // 0 total means not "all passed"
    }

    @Test
    void normalizeIgnoresWhitespace() {
        assertEquals("", JShellSandbox.normalize(null));
        assertEquals("[0,1]", JShellSandbox.normalize("[0, 1]"));
        assertEquals("[0,1]", JShellSandbox.normalize(" [ 0 , 1 ] "));
        assertEquals("hello", JShellSandbox.normalize("  hello  "));
    }

    @Test
    void partiallyCorrectSolutionReportsMixedResults() {
        String starterCode = """
                public class Solution {
                    public int[] twoSum(int[] nums, int target) {
                        // solution
                    }
                }
                """;

        // Only works for first test case by accident
        String userCode = """
                public class Solution {
                    public int[] twoSum(int[] nums, int target) {
                        return new int[]{0, 1};
                    }
                }
                """;

        List<TestCase> testCases = List.of(
                new TestCase("nums = [2,7,11,15], target = 9", "[0,1]", "Passes by luck"),
                new TestCase("nums = [3,2,4], target = 6", "[1,2]", "Should fail")
        );

        TestResult result = sandbox.executeWithTests(userCode, starterCode, testCases);

        assertFalse(result.allPassed());
        assertEquals(1, result.passed());
        assertEquals(1, result.failed());
        assertEquals(2, result.total());

        // Verify individual test details
        assertTrue(result.details().get(0).passed());
        assertFalse(result.details().get(1).passed());
        assertEquals("[0, 1]", result.details().get(1).actual());
        assertEquals("[1,2]", result.details().get(1).expected());
    }
}
