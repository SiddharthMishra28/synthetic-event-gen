package com.syntheticdata.expression;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateExpressionEvaluator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    /**
     * Handles expressions like {{T+3}}, {{t+5}}, {{T-1}}, {{t-12}}
     */
    public String addTime(String expression) {
        try {
            expression = expression.replace("{{", "").replace("}}", "").trim();

            boolean isDate = expression.startsWith("T") || expression.startsWith("t");
            char unitType = expression.charAt(0);  // 'T' or 't'

            String operator = expression.substring(1, 2); // '+' or '-'
            int amount = Integer.parseInt(expression.substring(2));

            LocalDateTime now = LocalDateTime.now();

            if (unitType == 'T') { // add/subtract days
                now = operator.equals("+") ? now.plusDays(amount) : now.minusDays(amount);
            } else { // add/subtract hours
                now = operator.equals("+") ? now.plusHours(amount) : now.minusHours(amount);
            }

            return now.format(FORMATTER);
        } catch (Exception e) {
            return "[DATE_ERR:" + e.getMessage() + "]";
        }
    }
}
