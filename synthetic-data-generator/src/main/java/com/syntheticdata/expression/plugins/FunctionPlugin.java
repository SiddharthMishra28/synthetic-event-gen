package com.syntheticdata.expression.plugins;

public interface FunctionPlugin {
    /**
     * Returns the plugin name used in templates (case-insensitive).
     * Example: "UUID", "RANDSTR", "ALPHA".
     */
    String name();

    /**
     * Execute the plugin with the given args (already trimmed).
     * args may be empty array for zero-arg plugins.
     * Return the string result to substitute into template.
     */
    String execute(String[] args);
}
