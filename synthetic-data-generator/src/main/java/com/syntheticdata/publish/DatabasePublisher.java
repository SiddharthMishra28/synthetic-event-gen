package com.syntheticdata.publish;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabasePublisher {
    private final HikariDataSource dataSource;
    private final ExecutorService executor;
    private final String SQL_INSERT;

    public DatabasePublisher(DatabaseConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUser());
        hikariConfig.setPassword(config.getPassword());
        this.dataSource = new HikariDataSource(hikariConfig);
        this.executor = Executors.newFixedThreadPool(10);
        this.SQL_INSERT = "INSERT INTO " + config.getTable() + " (" + config.getColumn() + ") VALUES (?)";
    }

    public CompletableFuture<Void> publish(String data) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
                stmt.setString(1, data);
                stmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public void close() {
        dataSource.close();
        executor.shutdown();
    }
}
