package com.syntheticdata.expression.plugins;

import java.security.SecureRandom;

public class RandStrPlugin implements FunctionPlugin {
    private static final String ALPHANUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom rnd = new SecureRandom();

    @Override public String name() { return "RANDSTR"; }

    @Override
    public String execute(String[] args) {
        int len = 8;
        if (args != null && args.length > 0 && !args[0].isEmpty()) {
            try { len = Integer.parseInt(args[0].trim()); } catch (Exception ignored) {}
        }
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(ALPHANUM.charAt(rnd.nextInt(ALPHANUM.length())));
        return sb.toString();
    }
}
