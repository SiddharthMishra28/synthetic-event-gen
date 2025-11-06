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
        assertTrue(result.contains("\"stringifiedJson\":\"{\\\"name\\\":"));
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
}
