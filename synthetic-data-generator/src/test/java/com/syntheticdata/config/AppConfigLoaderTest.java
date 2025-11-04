package com.syntheticdata.config;

import org.junit.Test;
import java.io.File;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AppConfigLoaderTest {

    @Test
    public void testLoadYamlConfig() throws Exception {
        AppConfigLoader loader = new AppConfigLoader();
        URL resource = getClass().getClassLoader().getResource("test-config.yaml");
        File configFile = new File(resource.getFile());

        Config config = loader.loadConfig(configFile);

        assertNotNull(config);
        assertEquals("database", config.getOutputMode());
        assertEquals(100, config.getCount());
        assertEquals("{\"testId\": \"{{UUID()}}\"}", config.getTemplate());
        assertNotNull(config.getDatabase());
        assertEquals("jdbc:h2:mem:testdb", config.getDatabase().getUrl());
        assertEquals("sa", config.getDatabase().getUser());
        assertEquals("", config.getDatabase().getPassword());
        assertEquals("TestTable", config.getDatabase().getTable());
        assertEquals("TestData", config.getDatabase().getColumn());
    }

    @Test
    public void testLoadPropertiesConfig() throws Exception {
        AppConfigLoader loader = new AppConfigLoader();
        URL resource = getClass().getClassLoader().getResource("test-config.properties");
        File configFile = new File(resource.getFile());

        Config config = loader.loadConfig(configFile);

        assertNotNull(config);
        assertEquals("database", config.getOutputMode());
        assertNotNull(config.getDatabase());
        assertEquals("jdbc:h2:mem:testdb", config.getDatabase().getUrl());
        assertEquals("sa", config.getDatabase().getUser());
        assertEquals("", config.getDatabase().getPassword());
        assertEquals("TestTable", config.getDatabase().getTable());
        assertEquals("TestData", config.getDatabase().getColumn());
    }
}
