package com.syntheticdata.expression;

import org.junit.Test;
import static org.junit.Assert.*;

public class ExpressionProcessorTest {

    @Test
    public void testSingleExpression() {
        ExpressionProcessor processor = new ExpressionProcessor();

        // Test a simple Faker expression
        String firstName = processor.evaluate("{{faker.name().firstName()}}");
        assertNotNull(firstName);
        assertFalse(firstName.startsWith("[FAKER_ERR"));
        assertFalse(firstName.contains("{{"));

        // Test another Faker expression
        String email = processor.evaluate("{{faker.internet().emailAddress()}}");
        assertNotNull(email);
        assertTrue(email.contains("@"));
        assertFalse(email.startsWith("[FAKER_ERR"));
        assertFalse(email.contains("{{"));
    }

    @Test
    public void testChainedExpressions() {
        ExpressionProcessor processor = new ExpressionProcessor();

        String chained = processor.evaluate("{{faker.name().lastName()}}_{{UUID}}");
        assertNotNull(chained);
        assertFalse(chained.contains("{{"));
        assertTrue(chained.contains("_"));

        // Verify that the UUID part is a valid UUID
        String[] parts = chained.split("_");
        assertEquals(2, parts.length);
        assertNotNull(parts[0]);
        assertNotNull(parts[1]);
        assertTrue(parts[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    public void testUnimplementedExpressions() {
        ExpressionProcessor processor = new ExpressionProcessor();

        String result = processor.evaluate("{{RANGE(1-10)}}");
        assertEquals("RANGE(1-10)", result);
    }

    @Test
    public void testDateExpressions() {
        ExpressionProcessor processor = new ExpressionProcessor();

        String date = processor.evaluate("{{T+3}}");
        assertNotNull(date);
        assertFalse(date.startsWith("[DATE_ERR"));
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}"));

        String time = processor.evaluate("{{t-5}}");
        assertNotNull(time);
        assertFalse(time.startsWith("[DATE_ERR"));
        assertTrue(time.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}"));
    }
}
