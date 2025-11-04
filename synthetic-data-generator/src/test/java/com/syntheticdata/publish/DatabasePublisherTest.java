package com.syntheticdata.publish;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class DatabasePublisherTest {

    private Connection connection;
    private DatabasePublisher publisher;

    @Before
    public void setUp() throws Exception {
        // Setup H2 in-memory database
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE TestTable (TestData VARCHAR(255))");
        }

        DatabaseConfig config = new DatabaseConfig(
            "jdbc:h2:mem:testdb",
            "sa",
            "",
            "TestTable",
            "TestData"
        );
        publisher = new DatabasePublisher(config);
    }

    @After
    public void tearDown() throws Exception {
        if (publisher != null) {
            publisher.close();
        }
        if (connection != null) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE TestTable");
            }
            connection.close();
        }
    }

    @Test
    public void testPublishSingleRecord() throws Exception {
        String testData = "Hello, World!";

        CompletableFuture<Void> future = publisher.publish(testData);
        future.get(5, TimeUnit.SECONDS); // Wait for async operation to complete

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TestData FROM TestTable")) {
            rs.next();
            String result = rs.getString("TestData");
            assertEquals(testData, result);
        }
    }

    @Test
    public void testPublishMultipleRecords() throws Exception {
        List<String> testDataList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            testDataList.add("Record " + i);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String data : testDataList) {
            futures.add(publisher.publish(data));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM TestTable")) {
            rs.next();
            int count = rs.getInt(1);
            assertEquals(testDataList.size(), count);
        }
    }
}
