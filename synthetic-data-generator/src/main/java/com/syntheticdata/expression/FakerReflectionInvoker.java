package com.syntheticdata.expression;

import net.datafaker.Faker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

public class FakerReflectionInvoker {

    private final Faker faker;

    public FakerReflectionInvoker() {
        this.faker = new Faker(Locale.ENGLISH);
    }

    /**
     * Takes an expression like "faker.name().firstName()" and executes it dynamically.
     */
    public String invoke(String expression) {
        try {
            // Remove outer faker.
            String cleanExpr = expression.replaceFirst("^faker\\.", "");
            String[] parts = cleanExpr.split("\\.");

            Object current = faker;
            for (String part : parts) {
                if (part.endsWith("()")) {
                    // Simple no-arg method
                    String methodName = part.substring(0, part.length() - 2);
                    Method method = current.getClass().getMethod(methodName);
                    current = method.invoke(current);
                } else if (part.contains("(") && part.endsWith(")")) {
                    // Method with arguments (future extension)
                    String methodName = part.substring(0, part.indexOf("("));
                    String argsString = part.substring(part.indexOf("(") + 1, part.length() - 1);
                    Object result = invokeMethodWithArgs(current, methodName, argsString);
                    current = result;
                } else {
                    // Access nested class e.g., faker.name
                    Method method = current.getClass().getMethod(part);
                    current = method.invoke(current);
                }
            }

            return current.toString();
        } catch (Exception e) {
            return "[FAKER_ERR: " + e.getMessage() + "]";
        }
    }

    /**
     * Future-ready extension for argument-based faker methods.
     */
    private Object invokeMethodWithArgs(Object target, String methodName, String argsString)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        String[] args = argsString.split(",");
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].trim().replace("\"", "");
        }

        // Currently support single string argument for simplicity
        Method method = target.getClass().getMethod(methodName, String.class);
        return method.invoke(target, args[0]);
    }
}
