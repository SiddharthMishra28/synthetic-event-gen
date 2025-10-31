package com.syntheticdata.expression;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles symbolic random patterns (####, $$$$)
 * and delegates nested {{ ... }} evaluation to ExpressionProcessor.
 */
public class CustomPatternGenerator {

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[#$]");
    private static final Random RANDOM = new Random();
    private final ExpressionProcessor processor;

    public CustomPatternGenerator(ExpressionProcessor processor) {
        this.processor = processor;
    }

    /**
     * Entry point – supports mixing literal symbols and nested expressions
     * e.g. {{faker.name().lastName()}}_{{RANGE(1-10)}} or TRAN{{RANGE(1-5)}}##
     */
    public String generate(String template) {
        if (template == null || template.isEmpty()) return "";

        // 1️⃣ First, evaluate nested expressions (faker, range, etc.)
        String resolved = processor.evaluate(template);

        // 2️⃣ Then, expand pattern symbols (# -> digit, $ -> letter)
        return expandSymbols(resolved);
    }

    /**
     * Replace all # and $ with random digits or uppercase letters.
     */
    private String expandSymbols(String input) {
        StringBuilder result = new StringBuilder();

        for (char c : input.toCharArray()) {
            switch (c) {
                case '#':
                    result.append(RANDOM.nextInt(10));
                    break;
                case '$':
                    result.append((char) ('A' + RANDOM.nextInt(26)));
                    break;
                default:
                    result.append(c);
                    break;
            }
        }

        return result.toString();
    }
}
