package com.syntheticdata.expression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionProcessor {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\{\\{([^{}]+)}}");

    private final FakerReflectionInvoker fakerInvoker = new FakerReflectionInvoker();
    private final DateExpressionEvaluator dateEvaluator = new DateExpressionEvaluator();
    private final RangeExpressionEvaluator rangeEvaluator = new RangeExpressionEvaluator();
    private final CustomPatternGenerator patternGenerator = new CustomPatternGenerator();

    public String evaluate(String expression) {
        Matcher matcher = EXPRESSION_PATTERN.matcher(expression);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String rawExpr = matcher.group(1).trim();
            String value = resolveExpression(rawExpr);
            matcher.appendReplacement(sb, value != null ? value : matcher.group(0));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveExpression(String rawExpr) {
        try {
            if (rawExpr.startsWith("faker.")) {
                // return fakerInvoker.invoke(rawExpr); // FIXME: Uncomment when implemented
                return null;
            } else if (rawExpr.startsWith("T+") || rawExpr.startsWith("t+")) {
                // return dateEvaluator.addTime(rawExpr); // FIXME: Uncomment when implemented
                return null;
            } else if (rawExpr.startsWith("RANGE(")) {
                // return rangeEvaluator.evaluate(rawExpr); // FIXME: Uncomment when implemented
                return null;
            } else if (rawExpr.contains("#") || rawExpr.contains("$")) {
                // return patternGenerator.generate(rawExpr); // FIXME: Uncomment when implemented
                return null;
            } else if (rawExpr.equalsIgnoreCase("UUID")) {
                return java.util.UUID.randomUUID().toString();
            } else {
                // Unknown expression — return unchanged
                return rawExpr;
            }
        } catch (Exception e) {
            return "[ERR:" + e.getMessage() + "]";
        }
    }
}
