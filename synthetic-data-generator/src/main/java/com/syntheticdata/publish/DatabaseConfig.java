package com.syntheticdata.publish;

public class DatabaseConfig {
    private String url;
    private String user;
    private String password;
    private String table;
    private String column;

    public DatabaseConfig() {
        // No-arg constructor for deserialization
    }

    public DatabaseConfig(String url, String user, String password, String table, String column) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.table = table;
        this.column = column;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getTable() {
        return table;
    }

    public String getColumn() {
        return column;
    }
}
