package dev.interviewkata.sandbox;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InputParserTest {

    @Nested
    class ParseMethodSignatureTest {

        @Test
        void parsesIntArrayReturnWithTwoParams() {
            String code = """
                    public class Solution {
                        public int[] twoSum(int[] nums, int target) {
                            // solution
                        }
                    }
                    """;

            InputParser.MethodSignature sig = InputParser.parseMethodSignature(code);

            assertEquals("int[]", sig.returnType());
            assertEquals("twoSum", sig.methodName());
            assertEquals(2, sig.params().size());
            assertEquals("int[]", sig.params().get(0).type());
            assertEquals("nums", sig.params().get(0).name());
            assertEquals("int", sig.params().get(1).type());
            assertEquals("target", sig.params().get(1).name());
        }

        @Test
        void parsesStringReturnWithStringParam() {
            String code = "public String reverseString(String s) { return s; }";

            InputParser.MethodSignature sig = InputParser.parseMethodSignature(code);

            assertEquals("String", sig.returnType());
            assertEquals("reverseString", sig.methodName());
            assertEquals(1, sig.params().size());
            assertEquals("String", sig.params().get(0).type());
            assertEquals("s", sig.params().get(0).name());
        }

        @Test
        void parsesBooleanReturnWithMultipleParams() {
            String code = "public boolean isValid(String s, int maxLen) { return true; }";

            InputParser.MethodSignature sig = InputParser.parseMethodSignature(code);

            assertEquals("boolean", sig.returnType());
            assertEquals("isValid", sig.methodName());
            assertEquals(2, sig.params().size());
        }

        @Test
        void parses2DArrayParam() {
            String code = "public int maxProfit(int[][] prices) { return 0; }";

            InputParser.MethodSignature sig = InputParser.parseMethodSignature(code);

            assertEquals("int", sig.returnType());
            assertEquals("maxProfit", sig.methodName());
            assertEquals("int[][]", sig.params().get(0).type());
        }

        @Test
        void parsesListParam() {
            String code = "public List<Integer> topK(int[] nums, int k) { return null; }";

            InputParser.MethodSignature sig = InputParser.parseMethodSignature(code);

            assertEquals("List<Integer>", sig.returnType());
            assertEquals("topK", sig.methodName());
            assertEquals(2, sig.params().size());
        }

        @Test
        void throwsWhenNoPublicMethod() {
            String code = "private void helper() {}";

            assertThrows(IllegalArgumentException.class, () ->
                    InputParser.parseMethodSignature(code));
        }
    }

    @Nested
    class ParseInputAssignmentsTest {

        @Test
        void parsesSimpleIntArrayAndInt() {
            Map<String, String> result = InputParser.parseInputAssignments(
                    "nums = [2,7,11,15], target = 9");

            assertEquals("[2,7,11,15]", result.get("nums"));
            assertEquals("9", result.get("target"));
        }

        @Test
        void parsesStringValue() {
            Map<String, String> result = InputParser.parseInputAssignments(
                    "s = \"hello\"");

            assertEquals("\"hello\"", result.get("s"));
        }

        @Test
        void parsesSingleParam() {
            Map<String, String> result = InputParser.parseInputAssignments("n = 5");

            assertEquals("5", result.get("n"));
        }

        @Test
        void parsesNestedArray() {
            Map<String, String> result = InputParser.parseInputAssignments(
                    "matrix = [[1,2],[3,4]], k = 2");

            assertEquals("[[1,2],[3,4]]", result.get("matrix"));
            assertEquals("2", result.get("k"));
        }

        @Test
        void parsesEmptyArray() {
            Map<String, String> result = InputParser.parseInputAssignments(
                    "nums = [], target = 0");

            assertEquals("[]", result.get("nums"));
            assertEquals("0", result.get("target"));
        }

        @Test
        void returnsEmptyMapForNull() {
            assertTrue(InputParser.parseInputAssignments(null).isEmpty());
        }

        @Test
        void returnsEmptyMapForBlank() {
            assertTrue(InputParser.parseInputAssignments("   ").isEmpty());
        }
    }

    @Nested
    class BuildInvocationTest {

        @Test
        void buildsIntArrayAndIntInvocation() {
            InputParser.MethodSignature sig = new InputParser.MethodSignature(
                    "int[]", "twoSum",
                    List.of(new InputParser.Parameter("int[]", "nums"),
                            new InputParser.Parameter("int", "target")));
            Map<String, String> assignments = Map.of("nums", "[2,7,11,15]", "target", "9");

            String invocation = InputParser.buildInvocation(sig, assignments);

            assertEquals("new Solution().twoSum(new int[]{2,7,11,15}, 9)", invocation);
        }

        @Test
        void buildsStringInvocation() {
            InputParser.MethodSignature sig = new InputParser.MethodSignature(
                    "String", "reverse",
                    List.of(new InputParser.Parameter("String", "s")));
            Map<String, String> assignments = Map.of("s", "\"hello\"");

            String invocation = InputParser.buildInvocation(sig, assignments);

            assertEquals("new Solution().reverse(\"hello\")", invocation);
        }

        @Test
        void builds2DArrayInvocation() {
            InputParser.MethodSignature sig = new InputParser.MethodSignature(
                    "int", "maxSum",
                    List.of(new InputParser.Parameter("int[][]", "matrix")));
            Map<String, String> assignments = Map.of("matrix", "[[1,2],[3,4]]");

            String invocation = InputParser.buildInvocation(sig, assignments);

            assertEquals("new Solution().maxSum(new int[][]{{1,2},{3,4}})", invocation);
        }

        @Test
        void throwsWhenMissingParameter() {
            InputParser.MethodSignature sig = new InputParser.MethodSignature(
                    "int", "solve",
                    List.of(new InputParser.Parameter("int", "n")));
            Map<String, String> assignments = Map.of("x", "5");

            assertThrows(IllegalArgumentException.class, () ->
                    InputParser.buildInvocation(sig, assignments));
        }
    }

    @Nested
    class BuildResultToStringTest {

        @Test
        void intArrayUsesArraysToString() {
            assertEquals("java.util.Arrays.toString(__result__)",
                    InputParser.buildResultToString("int[]"));
        }

        @Test
        void int2DArrayUsesDeepToString() {
            assertEquals("java.util.Arrays.deepToString(__result__)",
                    InputParser.buildResultToString("int[][]"));
        }

        @Test
        void stringReturnsDirect() {
            assertEquals("__result__", InputParser.buildResultToString("String"));
        }

        @Test
        void primitiveUsesValueOf() {
            assertEquals("String.valueOf(__result__)", InputParser.buildResultToString("int"));
            assertEquals("String.valueOf(__result__)", InputParser.buildResultToString("boolean"));
        }

        @Test
        void objectUsesValueOf() {
            assertEquals("String.valueOf(__result__)", InputParser.buildResultToString("List<Integer>"));
        }
    }

    @Nested
    class ConvertValueTest {

        @Test
        void intPassesThrough() {
            assertEquals("42", InputParser.convertValue("42", "int"));
        }

        @Test
        void longAppendsL() {
            assertEquals("42L", InputParser.convertValue("42", "long"));
        }

        @Test
        void longWithLPassesThrough() {
            assertEquals("42L", InputParser.convertValue("42L", "long"));
        }

        @Test
        void stringWrapsInQuotes() {
            assertEquals("\"hello\"", InputParser.convertValue("hello", "String"));
        }

        @Test
        void stringAlreadyQuotedPassesThrough() {
            assertEquals("\"hello\"", InputParser.convertValue("\"hello\"", "String"));
        }

        @Test
        void intArrayConverts() {
            assertEquals("new int[]{1,2,3}", InputParser.convertValue("[1,2,3]", "int[]"));
        }

        @Test
        void emptyIntArray() {
            assertEquals("new int[]{}", InputParser.convertValue("[]", "int[]"));
        }

        @Test
        void stringArrayConverts() {
            assertEquals("new String[]{\"a\",\"b\",\"c\"}",
                    InputParser.convertValue("[a,b,c]", "String[]"));
        }

        @Test
        void booleanPassesThrough() {
            assertEquals("true", InputParser.convertValue("true", "boolean"));
        }

        @Test
        void charWrapsInSingleQuotes() {
            assertEquals("'x'", InputParser.convertValue("x", "char"));
        }

        @Test
        void listOfIntegerConverts() {
            assertEquals("java.util.List.of(1,2,3)",
                    InputParser.convertValue("[1,2,3]", "List<Integer>"));
        }

        @Test
        void listOfStringConverts() {
            assertEquals("java.util.List.of(\"a\",\"b\")",
                    InputParser.convertValue("[a,b]", "List<String>"));
        }

        @Test
        void int2DArrayConverts() {
            assertEquals("new int[][]{{1,2},{3,4}}",
                    InputParser.convertValue("[[1,2],[3,4]]", "int[][]"));
        }

        @Test
        void emptyListConverts() {
            assertEquals("java.util.List.of()",
                    InputParser.convertValue("[]", "List<Integer>"));
        }
    }

    @Nested
    class SplitTopLevelTest {

        @Test
        void splitsSimpleAssignments() {
            List<String> parts = InputParser.splitTopLevel("nums = [2,7,11,15], target = 9");
            assertEquals(2, parts.size());
            assertEquals("nums = [2,7,11,15]", parts.get(0).trim());
            assertEquals("target = 9", parts.get(1).trim());
        }

        @Test
        void preservesNestedBrackets() {
            List<String> parts = InputParser.splitTopLevel("matrix = [[1,2],[3,4]], k = 2");
            assertEquals(2, parts.size());
            assertEquals("matrix = [[1,2],[3,4]]", parts.get(0).trim());
            assertEquals("k = 2", parts.get(1).trim());
        }

        @Test
        void singleAssignment() {
            List<String> parts = InputParser.splitTopLevel("n = 5");
            assertEquals(1, parts.size());
            assertEquals("n = 5", parts.get(0).trim());
        }

        @Test
        void preservesQuotedStrings() {
            List<String> parts = InputParser.splitTopLevel("s = \"hello, world\", n = 3");
            assertEquals(2, parts.size());
            assertEquals("s = \"hello, world\"", parts.get(0).trim());
            assertEquals("n = 3", parts.get(1).trim());
        }
    }
}
