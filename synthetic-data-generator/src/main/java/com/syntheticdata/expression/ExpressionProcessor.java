package com.syntheticdata.expression;

import com.syntheticdata.expression.plugins.PluginRegistry;
import com.syntheticdata.expression.plugins.FunctionPlugin;

import net.datafaker.Faker;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ExpressionProcessor with plugin support for function-style expressions like UUID(), RANDSTR(8), ALPHA(5)
 * and existing support for faker.*, RANGE(...), T+/t+ etc.
 */
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

    /**
     * Recursively evaluate expressions until no more {{...}} are present.
     * Safety: limits recursion by checking if evaluation yields the same string repeatedly.
     */
    public String evaluate(String input) {
        if (input == null || input.isEmpty()) return input;

        String prev;
        String curr = input;
        int guard = 0;
        do {
            prev = curr;
            curr = evaluateOnce(curr);
            guard++;
            if (guard > 10) { // hard guard to prevent runaway recursion
                break;
            }
        } while (!curr.equals(prev) && EXPRESSION_PATTERN.matcher(curr).find());

        return curr;
    }

    /**
     * Evaluate all top-level {{...}} in the input once (one pass).
     */
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

    /**
     * Evaluate a single expression (no surrounding {{}}).
     */
    private String evaluateSingle(String expr) {
        // 1) Function / plugin style: NAME(arg1,arg2)
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
            // if plugin not found, continue to other handlers (maybe RANDSTR used as custom)
        }

        // 2) faker.* chain
        if (expr.startsWith("faker.")) {
            try {
                return evalFakerChain(expr);
            } catch (Exception e) {
                return "[FAKER_ERR:" + e.getMessage() + "]";
            }
        }

        // 3) RANGE(x-y)
        if (expr.startsWith("RANGE(")) {
            return evalRange(expr);
        }

        // 4) T+, t+ (days/hours)
        if (expr.startsWith("T+") || expr.startsWith("T-") || expr.startsWith("t+") || expr.startsWith("t-")) {
            return evalTime(expr);
        }

        // 5) UUID (allow both UUID and UUID() as synonyms)
        if (expr.equalsIgnoreCase("UUID") || expr.equalsIgnoreCase("UUID()")) {
            return java.util.UUID.randomUUID().toString();
        }

        // 6) Default: return expr unchanged
        return expr;
    }

    // ----- helpers -----

    private String[] parseArgs(String argsRaw) {
        if (argsRaw == null || argsRaw.isEmpty()) return new String[0];
        // split on commas that are not inside quotes — basic split good for numeric/simple args
        String[] parts = argsRaw.split("\\s*,\\s*");
        for (int i = 0; i < parts.length; i++) {
            // strip surrounding quotes if present
            parts[i] = parts[i].replaceAll("^['\"]|['\"]$", "").trim();
        }
        return parts;
    }

    private String evalFakerChain(String chain) throws Exception {
        // chain like faker.name().firstName()
        String[] parts = chain.split("\\.");
        Object current = faker;
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            // method with args or no-arg
            if (part.endsWith("()")) {
                String method = part.substring(0, part.length() - 2);
                Method m = current.getClass().getMethod(method);
                current = m.invoke(current);
            } else if (part.contains("(") && part.endsWith(")")) {
                // future: parse args and try to match method signature
                String method = part.substring(0, part.indexOf("("));
                // for now assume single string arg
                String argsInside = part.substring(part.indexOf("(") + 1, part.length() - 1).replaceAll("^['\"]|['\"]$", "").trim();
                Method m = current.getClass().getMethod(method, String.class);
                current = m.invoke(current, argsInside);
            } else {
                // treat as property method with no args
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
            char unit = expr.charAt(0); // T or t
            char sign = expr.charAt(1); // + or -
            int val = Integer.parseInt(expr.substring(2).trim());
            LocalDateTime now = LocalDateTime.now();
            if (unit == 'T' || unit == 't') {
                if (Character.isUpperCase(unit)) {
                    // days
                    now = (sign == '+') ? now.plusDays(val) : now.minusDays(val);
                } else {
                    // hours (we used lower-case 't' earlier; keep behavior)
                    now = (sign == '+') ? now.plusHours(val) : now.minusHours(val);
                }
                return now.format(dtf);
            }
        } catch (Exception e) {
            return "[TIME_ERR]";
        }
        return "[TIME_ERR]";
    }
}
