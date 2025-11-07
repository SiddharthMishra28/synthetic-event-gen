package com.syntheticdata.expression;

import com.syntheticdata.expression.plugins.PluginRegistry;
import com.syntheticdata.expression.plugins.FunctionPlugin;
import net.datafaker.Faker;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionProcessor {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\s*\\((.*?)\\)\\s*$", Pattern.DOTALL);

    private final Faker faker = new Faker(Locale.ENGLISH);
    private final Random random = new Random();
    private final PluginRegistry pluginRegistry;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    public ExpressionProcessor(PluginRegistry registry) {
        this.pluginRegistry = registry;
    }

    public String evaluate(String input) {
        if (input == null || input.isEmpty() || !input.contains("{{")) {
            return input;
        }

        String prev;
        String curr = input;
        int guard = 0;
        do {
            prev = curr;
            curr = evaluateOnce(curr);
            guard++;
            if (guard > 10) {
                break;
            }
        } while (!curr.equals(prev) && EXPRESSION_PATTERN.matcher(curr).find());

        return curr;
    }

    private String evaluateOnce(String input) {
        Matcher matcher = EXPRESSION_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String inner = matcher.group(1).trim();
            String replacement = evaluateSingle(inner);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String evaluateSingle(String expr) {
        Matcher fnMatcher = FUNCTION_PATTERN.matcher(expr);
        if (fnMatcher.matches()) {
            String fname = fnMatcher.group(1).trim();
            String argsRaw = fnMatcher.group(2).trim();
            String[] args = parseArgs(argsRaw);
            if (pluginRegistry.contains(fname)) {
                FunctionPlugin plugin = pluginRegistry.get(fname);
                try {
                    return plugin.execute(args);
                } catch (Exception e) {
                    return "[PLUGIN_ERR:" + fname + ":" + e.getMessage() + "]";
                }
            }
        }

        if (expr.startsWith("REF:")) {
            return "{{" + expr + "}}";
        }

        if (expr.startsWith("faker.")) {
            try {
                return evalFakerChain(expr);
            } catch (Exception e) {
                return "[FAKER_ERR:" + e.getMessage() + "]";
            }
        }

        if (expr.startsWith("RANGE(")) {
            return evalRange(expr);
        }

        if (expr.matches("^[Tt][+-].*")) {
            return evalTime(expr);
        }

        if (expr.equalsIgnoreCase("UUID") || expr.equalsIgnoreCase("UUID()")) {
            return java.util.UUID.randomUUID().toString();
        }

        if (expr.contains("|")) {
            String[] options = expr.split("\\|");
            for (int i = 0; i < options.length; i++) {
                options[i] = options[i].trim();
            }
            return options[random.nextInt(options.length)];
        }

        return expr;
    }

    private String[] parseArgs(String argsRaw) {
        if (argsRaw == null || argsRaw.isEmpty()) return new String[0];
        String[] parts = argsRaw.split("\\s*,\\s*");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].replaceAll("^['\"]|['\"]$", "").trim();
        }
        return parts;
    }

    private String evalFakerChain(String chain) throws Exception {
        String[] parts = chain.split("\\.");
        Object current = faker;
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.endsWith("()")) {
                String method = part.substring(0, part.length() - 2);
                Method m = current.getClass().getMethod(method);
                current = m.invoke(current);
            } else if (part.contains("(") && part.endsWith(")")) {
                String method = part.substring(0, part.indexOf("("));
                String argsInside = part.substring(part.indexOf("(") + 1, part.length() - 1).replaceAll("^['\"]|['\"]$", "").trim();
                Method m = current.getClass().getMethod(method, String.class);
                current = m.invoke(current, argsInside);
            } else {
                Method m = current.getClass().getMethod(part);
                current = m.invoke(current);
            }
        }
        return current == null ? "" : current.toString();
    }

    private String evalRange(String expr) {
        String inside = expr.substring(expr.indexOf('(') + 1, expr.lastIndexOf(')'));
        String[] bounds = inside.split("-");
        try {
            int min = Integer.parseInt(bounds[0].trim());
            int max = Integer.parseInt(bounds[1].trim());
            int val = random.nextInt(max - min + 1) + min;
            return String.valueOf(val);
        } catch (Exception e) {
            return "[RANGE_ERR]";
        }
    }

    private String evalTime(String expr) {
        try {
            char unit = expr.charAt(0);
            char sign = expr.charAt(1);
            int val = Integer.parseInt(expr.substring(2).trim());
            LocalDateTime now = LocalDateTime.now();
            if (Character.isUpperCase(unit)) {
                now = (sign == '+') ? now.plusDays(val) : now.minusDays(val);
            } else {
                now = (sign == '+') ? now.plusHours(val) : now.minusHours(val);
            }
            return now.format(dtf);
        } catch (Exception e) {
            return "[TIME_ERR]";
        }
    }
}
