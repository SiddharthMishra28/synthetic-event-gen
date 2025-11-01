package com.syntheticdata.engine;

import org.junit.Test;
import java.io.File;
import java.net.URL;
import java.util.List;
import static org.junit.Assert.*;

public class FileProcessingTest {

    @Test
    public void testGenerateFromFile_Direct() throws Exception {
        URL resource = getClass().getClassLoader().getResource("test-data/direct_event.json");
        File file = new File(resource.toURI());
        SyntheticDataEngine engine = new SyntheticDataEngine();
        String result = engine.generateFromFile(file.getAbsolutePath());
        assertNotNull(result);
        assertFalse(result.contains("{{"));
        assertTrue(result.contains("Direct-Processing-Event"));
    }

    @Test
    public void testGenerateFromFile_JsonPath() throws Exception {
        URL resource = getClass().getClassLoader().getResource("test-data/jsonpath_event.json");
        File file = new File(resource.toURI());
        SyntheticDataEngine engine = new SyntheticDataEngine();
        String result = engine.generateFromFile(file.getAbsolutePath());
        assertNotNull(result);
        assertFalse(result.contains("{{"));
        assertTrue(result.contains("JSONPath-Event"));
        assertFalse(result.contains("\"name\": \"\""));
    }

    @Test
    public void testGenerateFromDirectory() throws Exception {
        URL resource = getClass().getClassLoader().getResource("test-data");
        File dir = new File(resource.toURI());
        SyntheticDataEngine engine = new SyntheticDataEngine();
        List<String> results = engine.generateFromDirectory(dir.getAbsolutePath());
        assertEquals(2, results.size());
        for (String result : results) {
            assertFalse(result.contains("{{"));
        }
    }
}
