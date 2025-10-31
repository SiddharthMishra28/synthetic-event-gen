package com.syntheticdata.expression.plugins;

import java.security.SecureRandom;

public class AlphaPlugin implements FunctionPlugin {
    private static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom rnd = new SecureRandom();

    @Override public String name() { return "ALPHA"; }

    @Override
    public String execute(String[] args) {
        int len = 6;
        if (args != null && args.length > 0 && !args[0].isEmpty()) {
            try { len = Integer.parseInt(args[0].trim()); } catch (Exception ignored) {}
        }
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(ALPHA.charAt(rnd.nextInt(ALPHA.length())));
        return sb.toString();
    }
}
