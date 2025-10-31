package com.syntheticdata.expression;

import java.util.concurrent.ThreadLocalRandom;

public class RangeExpressionEvaluator {

    /**
     * Handles expressions like {{RANGE(10-1000)}}
     */
    public String evaluate(String expression) {
        try {
            // Cleanup
            String cleanExpr = expression
                    .replace("{{", "")
                    .replace("}}", "")
                    .replace("RANGE(", "")
                    .replace(")", "")
                    .trim();

            // Parse range
            String[] bounds = cleanExpr.split("-");
            if (bounds.length != 2) {
                throw new IllegalArgumentException("Invalid RANGE format: " + expression);
            }

            int min = Integer.parseInt(bounds[0].trim());
            int max = Integer.parseInt(bounds[1].trim());

            // Validate
            if (min > max) {
                throw new IllegalArgumentException("Min cannot be greater than max in " + expression);
            }

            // Generate random number
            int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
            return String.valueOf(randomNum);

        } catch (Exception e) {
            return "[RANGE_ERR:" + e.getMessage() + "]";
        }
    }
}
