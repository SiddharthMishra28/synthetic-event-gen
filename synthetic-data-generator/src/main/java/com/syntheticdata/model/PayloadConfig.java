package com.syntheticdata.model;

import java.util.Map;

public class PayloadConfig {
    private Map<String, String> fieldMappings;

    public Map<String, String> getFieldMappings() {
        return fieldMappings;
    }

    public void setFieldMappings(Map<String, String> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    @Override
    public String toString() {
        return "PayloadConfig{" +
                "fieldMappings=" + fieldMappings +
                '}';
    }
}
