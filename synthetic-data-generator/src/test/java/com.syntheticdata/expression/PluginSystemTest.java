package com.syntheticdata.expression;

import com.syntheticdata.engine.SyntheticDataEngine;
import org.junit.Test;
import static org.junit.Assert.*;

public class PluginSystemTest {

    @Test
    public void testUuidPlugin() {
        String result = SyntheticDataEngine.generateData("{{UUID()}}");
        assertNotNull(result);
        assertTrue(result.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    public void testRandStrPlugin() {
        String result = SyntheticDataEngine.generateData("{{RANDSTR(10)}}");
        assertNotNull(result);
        assertEquals(10, result.length());
        assertTrue(result.matches("[a-zA-Z0-9]+"));
    }

    @Test
    public void testAlphaPlugin() {
        String result = SyntheticDataEngine.generateData("{{ALPHA(5)}}");
        assertNotNull(result);
        assertEquals(5, result.length());
        assertTrue(result.matches("[a-zA-Z]+"));
    }

    @Test
    public void testNestedExpressionsWithPlugins() {
        String result = SyntheticDataEngine.generateData("user_{{faker.name().lastName()}}_{{RANDSTR(4)}}");
        assertNotNull(result);
        assertFalse(result.contains("{{"));
        assertTrue(result.matches("user_\\w+_[a-zA-Z0-9]{4}"));
    }

    @Test
    public void testCustomPatternsWithPlugins() {
        String result = SyntheticDataEngine.generateData("ID-{{ALPHA(3)}}-####");
        assertNotNull(result);
        assertFalse(result.contains("{{"));
        assertTrue(result.matches("ID-[a-zA-Z]{3}-\\d{4}"));
    }
}
