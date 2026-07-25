package dev.interviewkata.sandbox;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses test case input strings and method signatures to generate Java invocation code.
 * Converts natural language test format ("nums = [2,7,11,15], target = 9") into
 * valid Java expressions based on method parameter types.
 */
public class InputParser {

    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "public\\s+([\\w\\[\\]<>,\\s]+?)\\s+(\\w+)\\s*\\(([^)]*)\\)"
    );

    private static final Pattern PARAM_PATTERN = Pattern.compile(
            "\\s*([\\w\\[\\]<>,\\s]+?)\\s+(\\w+)\\s*"
    );

    /**
     * Extracts method signature from starter code.
     */
    public static MethodSignature parseMethodSignature(String starterCode) {
        Matcher m = METHOD_PATTERN.matcher(starterCode);
        if (!m.find()) {
            throw new IllegalArgumentException("Cannot find public method in starter code");
        }

        String returnType = m.group(1).trim();
        String methodName = m.group(2);
        String paramsStr = m.group(3);

        List<Parameter> params = new ArrayList<>();
        if (!paramsStr.isBlank()) {
            Matcher pm = PARAM_PATTERN.matcher(paramsStr);
            int lastEnd = 0;
            // Split by comma but respect generics
            for (String part : splitParams(paramsStr)) {
                Matcher single = PARAM_PATTERN.matcher(part);
                if (single.matches()) {
                    params.add(new Parameter(single.group(1).trim(), single.group(2).trim()));
                }
            }
        }

        return new MethodSignature(returnType, methodName, params);
    }

    /**
     * Parses test case input string into variable assignments.
     * Input format: "nums = [2,7,11,15], target = 9"
     * Returns: {nums → "[2,7,11,15]", target → "9"}
     */
    public static Map<String, String> parseInputAssignments(String input) {
        Map<String, String> assignments = new LinkedHashMap<>();
        if (input == null || input.isBlank()) {
            return assignments;
        }

        // Split on top-level commas (not inside brackets or quotes)
        List<String> parts = splitTopLevel(input);
        for (String part : parts) {
            int eqIdx = part.indexOf('=');
            if (eqIdx == -1) continue;
            String name = part.substring(0, eqIdx).trim();
            String value = part.substring(eqIdx + 1).trim();
            assignments.put(name, value);
        }
        return assignments;
    }

    /**
     * Builds a Java invocation expression: new Solution().methodName(arg1, arg2, ...)
     */
    public static String buildInvocation(MethodSignature sig, Map<String, String> assignments) {
        StringBuilder sb = new StringBuilder();
        sb.append("new Solution().").append(sig.methodName()).append("(");

        List<String> args = new ArrayList<>();
        for (Parameter param : sig.params()) {
            String rawValue = assignments.get(param.name());
            if (rawValue == null) {
                throw new IllegalArgumentException(
                        "Missing value for parameter '" + param.name() + "' in test input");
            }
            args.add(convertValue(rawValue, param.type()));
        }
        sb.append(String.join(", ", args));
        sb.append(")");
        return sb.toString();
    }

    /**
     * Builds the result-to-string expression based on return type.
     */
    public static String buildResultToString(String returnType) {
        if (returnType.endsWith("[][]")) {
            return "java.util.Arrays.deepToString(__result__)";
        } else if (returnType.endsWith("[]")) {
            return "java.util.Arrays.toString(__result__)";
        } else if (returnType.equals("String")) {
            return "__result__";
        } else if (isPrimitive(returnType)) {
            return "String.valueOf(__result__)";
        } else {
            // Objects (List, etc.) - use toString()
            return "String.valueOf(__result__)";
        }
    }

    /**
     * Converts a raw value string to a Java expression based on the target type.
     */
    static String convertValue(String rawValue, String type) {
        if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
            // Already a string literal
            return rawValue;
        }

        return switch (type) {
            case "int" -> rawValue;
            case "long" -> rawValue.endsWith("L") ? rawValue : rawValue + "L";
            case "double" -> rawValue;
            case "float" -> rawValue.endsWith("f") ? rawValue : rawValue + "f";
            case "boolean" -> rawValue;
            case "char" -> "'" + rawValue.replace("'", "") + "'";
            case "String" -> wrapString(rawValue);
            case "int[]" -> convertIntArray(rawValue);
            case "long[]" -> convertLongArray(rawValue);
            case "double[]" -> convertDoubleArray(rawValue);
            case "String[]" -> convertStringArray(rawValue);
            case "char[]" -> convertCharArray(rawValue);
            case "int[][]" -> convertInt2DArray(rawValue);
            case "boolean[]" -> convertBooleanArray(rawValue);
            default -> {
                if (type.startsWith("List<")) {
                    yield convertList(rawValue, type);
                }
                yield rawValue;
            }
        };
    }

    private static String wrapString(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value;
        }
        // Escape internal quotes
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String convertIntArray(String value) {
        String inner = stripBrackets(value);
        if (inner.isEmpty()) return "new int[]{}";
        return "new int[]{" + inner + "}";
    }

    private static String convertLongArray(String value) {
        String inner = stripBrackets(value);
        if (inner.isEmpty()) return "new long[]{}";
        String[] parts = inner.split(",");
        StringBuilder sb = new StringBuilder("new long[]{");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(",");
            String v = parts[i].trim();
            sb.append(v.endsWith("L") ? v : v + "L");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String convertDoubleArray(String value) {
        String inner = stripBrackets(value);
        if (inner.isEmpty()) return "new double[]{}";
        return "new double[]{" + inner + "}";
    }

    private static String convertStringArray(String value) {
        String inner = stripBrackets(value);
        if (inner.isEmpty()) return "new String[]{}";
        String[] parts = inner.split(",");
        StringBuilder sb = new StringBuilder("new String[]{");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(",");
            String v = parts[i].trim();
            sb.append(wrapString(v.replace("\"", "")));
        }
        sb.append("}");
        return sb.toString();
    }

    private static String convertCharArray(String value) {
        String inner = stripBrackets(value);
        if (inner.isEmpty()) return "new char[]{}";
        String[] parts = inner.split(",");
        StringBuilder sb = new StringBuilder("new char[]{");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(",");
            String v = parts[i].trim().replace("'", "").replace("\"", "");
            sb.append("'").append(v).append("'");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String convertBooleanArray(String value) {
        String inner = stripBrackets(value);
        if (inner.isEmpty()) return "new boolean[]{}";
        return "new boolean[]{" + inner + "}";
    }

    private static String convertInt2DArray(String value) {
        // Input: [[1,2],[3,4]]
        String trimmed = value.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return value;
        }
        // Remove outer brackets
        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        if (inner.isEmpty()) return "new int[][]{}";

        List<String> rows = splitNestedArrays(inner);
        StringBuilder sb = new StringBuilder("new int[][]{");
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) sb.append(",");
            String row = rows.get(i).trim();
            String rowInner = stripBrackets(row);
            sb.append("{").append(rowInner).append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String convertList(String value, String type) {
        String inner = stripBrackets(value);
        if (inner.isEmpty()) return "java.util.List.of()";

        // Extract generic type
        String genericType = type.substring(5, type.length() - 1); // List<X> → X

        String[] parts = inner.split(",");
        StringBuilder sb = new StringBuilder("java.util.List.of(");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(",");
            String v = parts[i].trim();
            if (genericType.equals("String")) {
                sb.append(wrapString(v.replace("\"", "")));
            } else {
                sb.append(v);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private static String stripBrackets(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    private static boolean isPrimitive(String type) {
        return switch (type) {
            case "int", "long", "double", "float", "char", "boolean", "byte", "short" -> true;
            default -> false;
        };
    }

    /**
     * Splits comma-separated parameters respecting generics angle brackets.
     */
    static List<String> splitParams(String paramsStr) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < paramsStr.length(); i++) {
            char c = paramsStr.charAt(i);
            if (c == '<') depth++;
            else if (c == '>') depth--;
            else if (c == ',' && depth == 0) {
                result.add(paramsStr.substring(start, i));
                start = i + 1;
            }
        }
        result.add(paramsStr.substring(start));
        return result;
    }

    /**
     * Splits top-level comma-separated assignments, respecting brackets and quotes.
     * "nums = [2,7,11,15], target = 9" → ["nums = [2,7,11,15]", "target = 9"]
     */
    static List<String> splitTopLevel(String input) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        boolean inQuote = false;
        int start = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"' && (i == 0 || input.charAt(i - 1) != '\\')) {
                inQuote = !inQuote;
            } else if (!inQuote) {
                if (c == '[' || c == '(') depth++;
                else if (c == ']' || c == ')') depth--;
                else if (c == ',' && depth == 0) {
                    // Check if this looks like a new assignment (has = after it)
                    String remaining = input.substring(i + 1);
                    if (looksLikeAssignment(remaining)) {
                        result.add(input.substring(start, i));
                        start = i + 1;
                    }
                }
            }
        }
        result.add(input.substring(start));
        return result;
    }

    private static boolean looksLikeAssignment(String s) {
        // Check if the next non-whitespace token is "word =" pattern
        String trimmed = s.trim();
        return trimmed.matches("\\w+\\s*=.*");
    }

    /**
     * Splits nested array elements: "[1,2],[3,4]" → ["[1,2]", "[3,4]"]
     */
    private static List<String> splitNestedArrays(String inner) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') depth--;
            else if (c == ',' && depth == 0) {
                result.add(inner.substring(start, i));
                start = i + 1;
            }
        }
        result.add(inner.substring(start));
        return result;
    }

    public record MethodSignature(String returnType, String methodName, List<Parameter> params) {
    }

    public record Parameter(String type, String name) {
    }
}
