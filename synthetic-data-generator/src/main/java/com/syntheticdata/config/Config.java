package com.syntheticdata.config;

import com.syntheticdata.publish.DatabaseConfig;

public class Config {
    private String outputMode;
    private DatabaseConfig database;

    public String getOutputMode() {
        return outputMode;
    }

    public void setOutputMode(String outputMode) {
        this.outputMode = outputMode;
    }

    public DatabaseConfig getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseConfig database) {
        this.database = database;
    }

    private int count;
    private String template;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
