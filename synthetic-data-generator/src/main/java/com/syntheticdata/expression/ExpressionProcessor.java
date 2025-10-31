package com.syntheticdata.expression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionProcessor {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\{\\{([^{}]+)}}");

    private final FakerReflectionInvoker fakerInvoker = new FakerReflectionInvoker();
    private final DateExpressionEvaluator dateEvaluator = new DateExpressionEvaluator();
    private final RangeExpressionEvaluator rangeEvaluator = new RangeExpressionEvaluator();
    private final CustomPatternGenerator patternGenerator = new CustomPatternGenerator();

    /**
     * Main entry point — evaluates string with one or many {{expressions}}.
     */
    public String evaluate(String input) {
        if (input == null || !input.contains("{{")) {
            return input;
        }

        Matcher matcher = EXPRESSION_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String rawExpr = matcher.group(1).trim();
            String evaluatedValue = resolveExpression(rawExpr);
            matcher.appendReplacement(result, Matcher.quoteReplacement(evaluatedValue));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Resolves individual {{...}} expressions.
     */
    private String resolveExpression(String rawExpr) {
        try {
            if (rawExpr.startsWith("faker.")) {
                return fakerInvoker.invoke(rawExpr);
            } else if (rawExpr.startsWith("T+") || rawExpr.startsWith("t+")) {
                // return dateEvaluator.addTime(rawExpr); // FIXME: Uncomment when implemented
                return rawExpr;
            } else if (rawExpr.startsWith("RANGE(")) {
                // return rangeEvaluator.evaluate(rawExpr); // FIXME: Uncomment when implemented
                return rawExpr;
            } else if (rawExpr.contains("#") || rawExpr.contains("$")) {
                // return patternGenerator.generate(rawExpr); // FIXME: Uncomment when implemented
                return rawExpr;
            } else if (rawExpr.equalsIgnoreCase("UUID")) {
                return java.util.UUID.randomUUID().toString();
            } else {
                return rawExpr;
            }
        } catch (Exception e) {
            return "[ERR:" + e.getMessage() + "]";
        }
    }
}
