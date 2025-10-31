package com.syntheticdata.expression;

import java.util.Random;

public class CustomPatternGenerator {

    private static final Random rnd = new Random();
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Expands symbols # -> digits and $ -> uppercase letters.
     */
    public String process(String input) {
        if (input == null) return "";

        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
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
