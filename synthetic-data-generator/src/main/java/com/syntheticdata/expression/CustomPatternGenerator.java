package com.syntheticdata.expression;

import java.util.Random;

public class CustomPatternGenerator {

    private final ExpressionProcessor processor;
    private static final Random rnd = new Random();
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public CustomPatternGenerator(ExpressionProcessor processor) {
        this.processor = processor;
    }

    /**
     * Resolve nested placeholders first via ExpressionProcessor
     * then expand symbols # -> digits and $ -> uppercase letters.
     */
    public String generate(String template) {
        if (template == null) return "";

        // 1) Evaluate nested {{...}} expressions (faker, RANGE, plugin functions)
        String resolved = processor.evaluate(template);

        // 2) Expand symbol characters
        StringBuilder sb = new StringBuilder(resolved.length());
        for (int i = 0; i < resolved.length(); i++) {
            char c = resolved.charAt(i);
            if (c == '#') {
                sb.append(rnd.nextInt(10));
            } else if (c == '$') {
                sb.append(LETTERS.charAt(rnd.nextInt(LETTERS.length())));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
