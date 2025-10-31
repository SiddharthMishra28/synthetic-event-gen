package com.syntheticdata.engine;

import org.junit.Test;
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
}
