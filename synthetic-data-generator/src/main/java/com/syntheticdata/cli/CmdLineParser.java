package com.syntheticdata.cli;

import java.util.HashMap;
import java.util.Map;

public class CmdLineParser {
    private Map<String, String> argsMap = new HashMap<>();

    public void parse(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] parts = arg.substring(2).split("=", 2);
                if (parts.length == 2) {
                    argsMap.put(parts[0], parts[1]);
                }
            }
        }
    }

    public String getArgument(String name) {
        return argsMap.get(name);
    }

    public boolean hasArgument(String name) {
        return argsMap.containsKey(name);
    }
}
