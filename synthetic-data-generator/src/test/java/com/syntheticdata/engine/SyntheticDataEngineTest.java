package com.syntheticdata.engine;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class SyntheticDataEngineTest {

    @Test
    public void testGenerateData() {
        String input = "{\\n" +
                "  \"user\": \"{{faker.name().fullName()}}\",\\n" +
                "  \"email\": \"{{faker.internet().emailAddress()}}\",\\n" +
                "  \"amount\": \"{{RANGE(10-1000)}}\",\\n" +
                "  \"transactionId\": \"TRAN{{RANGE(10000-99999)}}\",\\n" +
                "  \"customRef\": \"{{faker.name().lastName()}}_{{RANGE(1-10)}}\",\\n" +
                "  \"apiKey\": \"{{RANDSTR(16)}}\"\\n" +
                "}";

        String result = SyntheticDataEngine.generateData(input);

        assertNotNull(result);
        assertFalse(result.contains("{{"));
        assertTrue(result.contains("TRAN"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateDataWithNullInput() {
        SyntheticDataEngine.generateData(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateDataWithEmptyInput() {
        SyntheticDataEngine.generateData("");
    }

    @Test
    public void testMultiRecordGeneration() {
        String template = "{\"id\": \"{{UUID()}}\"}";
        List<String> results = SyntheticDataEngine.generateData(template, 5);
        assertEquals(5, results.size());
        // Check for uniqueness
        long distinctCount = results.stream().distinct().count();
        assertTrue(distinctCount > 1);
    }
}
