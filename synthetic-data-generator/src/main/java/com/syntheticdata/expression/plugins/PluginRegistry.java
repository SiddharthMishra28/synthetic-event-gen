package com.syntheticdata.expression.plugins;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;

public class PluginRegistry {

    private final Map<String, FunctionPlugin> registry = new ConcurrentHashMap<>();

    public void register(FunctionPlugin plugin) {
        registry.put(plugin.name().toUpperCase(), plugin);
    }

    public boolean contains(String name) {
        return registry.containsKey(name.toUpperCase());
    }

    public FunctionPlugin get(String name) {
        return registry.get(name.toUpperCase());
    }

    public Collection<FunctionPlugin> all() {
        return registry.values();
    }
}
