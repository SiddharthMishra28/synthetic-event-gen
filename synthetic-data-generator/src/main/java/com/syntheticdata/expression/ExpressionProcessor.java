package com.syntheticdata.expression;

import com.github.javafaker.Faker;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionProcessor {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");
    private final Faker faker = new Faker(Locale.ENGLISH);
    private final Random random = new Random();

    /**
     * Recursively evaluate all {{ ... }} expressions inside the input.
     */
    public String evaluate(String input) {
        if (input == null || input.isEmpty()) return input;

        StringBuffer result = new StringBuffer();
        Matcher matcher = EXPRESSION_PATTERN.matcher(input);

        while (matcher.find()) {
            String expr = matcher.group(1).trim();
            String value = evaluateExpression(expr);

            // Replace {{expr}} with evaluated value
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }

        matcher.appendTail(result);

        // If new expressions appeared after evaluation (nested), recurse
        if (EXPRESSION_PATTERN.matcher(result.toString()).find()) {
            return evaluate(result.toString());
        }

        return result.toString();
    }

    /**
     * Evaluate a single expression like faker.name().firstName() or RANGE(1-10)
     */
    private String evaluateExpression(String expr) {
        try {
            if (expr.startsWith("faker.")) {
                return evaluateFaker(expr);
            }
            if (expr.startsWith("RANGE(")) {
                return evaluateRange(expr);
            }
            if (expr.startsWith("DATE(")) {
                return evaluateDate(expr);
            }
        } catch (Exception e) {
            return "ERROR(" + expr + ")";
        }
        return expr; // Unknown expression, return as-is
    }

    /**
     * Support faker dynamic method resolution via reflection.
     * Example: faker.name().lastName()
     */
    private String evaluateFaker(String expr) throws Exception {
        // Split into chain: faker.name().lastName() -> ["faker", "name()", "lastName()"]
        String[] parts = expr.split("\\.");
        Object current = faker;

        for (int i = 1; i < parts.length; i++) {
            String methodPart = parts[i].replace("()", "").trim();
            Method method = current.getClass().getMethod(methodPart);
            current = method.invoke(current);
        }

        return current.toString();
    }

    /**
     * Evaluate numeric range expression like RANGE(10-100)
     */
    private String evaluateRange(String expr) {
        String inside = expr.substring(expr.indexOf("(") + 1, expr.indexOf(")"));
        String[] parts = inside.split("-");
        int min = Integer.parseInt(parts[0].trim());
        int max = Integer.parseInt(parts[1].trim());
        return String.valueOf(random.nextInt(max - min + 1) + min);
    }

    /**
     * Simple date placeholder evaluator
     * Example: DATE(now), DATE(offset:-5)
     */
    private String evaluateDate(String expr) {
        if (expr.equalsIgnoreCase("DATE(now)")) {
            return java.time.LocalDate.now().toString();
        }
        if (expr.contains("offset:")) {
            int offset = Integer.parseInt(expr.replaceAll("[^0-9-]", ""));
            return java.time.LocalDate.now().plusDays(offset).toString();
        }
        return java.time.LocalDate.now().toString();
    }
}
