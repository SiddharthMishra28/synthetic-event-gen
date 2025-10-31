package com.syntheticdata.expression.plugins;

import java.util.UUID;

public class UuidPlugin implements FunctionPlugin {
    @Override public String name() { return "UUID"; }

    @Override
    public String execute(String[] args) {
        return UUID.randomUUID().toString();
    }
}
