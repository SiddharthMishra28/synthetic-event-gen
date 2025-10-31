package com.syntheticdata.expression;

import org.junit.Test;
import static org.junit.Assert.*;

public class ExpressionProcessorTest {

    @Test
    public void testFakerExpressions() {
        ExpressionProcessor processor = new ExpressionProcessor();
        String result = processor.evaluate("{{faker.name().firstName()}}");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertFalse(result.contains("{{"));
    }

    @Test
    public void testRangeExpressions() {
        ExpressionProcessor processor = new ExpressionProcessor();
        String result = processor.evaluate("{{RANGE(10-20)}}");
        int value = Integer.parseInt(result);
        assertTrue(value >= 10 && value <= 20);
    }

    @Test
    public void testDateExpressions() {
        ExpressionProcessor processor = new ExpressionProcessor();
        String result = processor.evaluate("{{DATE(now)}}");
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void testCustomPatternGenerator() {
        ExpressionProcessor processor = new ExpressionProcessor();
        CustomPatternGenerator generator = new CustomPatternGenerator(processor);

        String result1 = generator.generate("TRAN###");
        assertTrue(result1.matches("TRAN\\d{3}"));

        String result2 = generator.generate("$$$");
        assertTrue(result2.matches("[A-Z]{3}"));

        String result3 = generator.generate("{{faker.name().lastName()}}-##");
        assertFalse(result3.contains("{{"));
        assertTrue(result3.matches("\\w+-\\d{2}"));

        String result4 = generator.generate("ID-{{RANGE(100-200)}}-$$");
        assertFalse(result4.contains("{{"));
        assertTrue(result4.matches("ID-\\d+-[A-Z]{2}"));
    }
}
