package com.syntheticdata.publish;

public class DatabaseConfig {
    private String url;
    private String user;
    private String password;
    private String table;
    private String column;

    public DatabaseConfig(String url, String user, String password, String table, String column) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.table = table;
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
