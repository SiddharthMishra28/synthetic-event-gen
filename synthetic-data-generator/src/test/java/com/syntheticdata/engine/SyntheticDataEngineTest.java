package com.syntheticdata.engine;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SyntheticDataEngineTest {

    @Test
    public void testGenerateData() {
        String template = "{\"name\":\"{{faker.name.fullName}}\", \"age\":\"{{RANGE(20-30)}}\"}";
        String generated = SyntheticDataEngine.generateData(template);
        assertNotNull(generated);
        assertTrue(generated.contains("\"name\":"));
        assertTrue(generated.contains("\"age\":"));
    }

    @Test
    public void testMultiRecordGeneration() {
        String template = "{\"id\":\"{{UUID()}}\"}";
        List<String> records = SyntheticDataEngine.generateData(template, 5);
        assertEquals(5, records.size());
        for (String record : records) {
            assertTrue(record.contains("\"id\":"));
        }
    }

    @Test
    public void testStringifiedJson() {
        SyntheticDataEngine engine = new SyntheticDataEngine();
        String input = "{\\\"name\\\": \\\"{{faker.name.fullName}}\\\", \\\"company\\\": \\\"{{faker.company.name}}\\\"}";
        String template = String.format("{\"stringifiedJson\": \"%s\"}", input);
        String result = engine.generate(template);

        com.jayway.jsonpath.JsonPath path = com.jayway.jsonpath.JsonPath.compile("$.stringifiedJson");
        String stringifiedJson = path.read(result);

        com.jayway.jsonpath.JsonPath namePath = com.jayway.jsonpath.JsonPath.compile("$.name");
        String name = namePath.read(stringifiedJson);
        assertNotNull(name);

        com.jayway.jsonpath.JsonPath companyPath = com.jayway.jsonpath.JsonPath.compile("$.company");
        String company = companyPath.read(stringifiedJson);
        assertNotNull(company);
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
    public void testValueReferencing() {
        SyntheticDataEngine engine = new SyntheticDataEngine();
        String template = "{\"universalAccountNumber\": \"########\", \"transaction\": {\"universalAccountNumber\": \"{{REF:$.universalAccountNumber}}\"}}";
        String result = engine.generate(template);

        com.jayway.jsonpath.JsonPath path = com.jayway.jsonpath.JsonPath.compile("$.universalAccountNumber");
        String uan = path.read(result);
        path = com.jayway.jsonpath.JsonPath.compile("$.transaction.universalAccountNumber");
        String tranUan = path.read(result);

        assertNotNull(uan);
        assertEquals(uan, tranUan);
    }

    @Test
    public void testEnumSelection() {
        SyntheticDataEngine engine = new SyntheticDataEngine();
        String template = "{\"productType\": \"{{ABC | DEF | GHI | PQR}}\"}";
        String result = engine.generate(template);

        com.jayway.jsonpath.JsonPath path = com.jayway.jsonpath.JsonPath.compile("$.productType");
        String productType = path.read(result);

        assertTrue(java.util.Arrays.asList("ABC", "DEF", "GHI", "PQR").contains(productType));
    }
}
