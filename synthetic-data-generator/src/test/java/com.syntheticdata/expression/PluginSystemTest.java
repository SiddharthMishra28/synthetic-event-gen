package com.syntheticdata.expression;

import com.syntheticdata.expression.plugins.AlphaPlugin;
import com.syntheticdata.expression.plugins.PluginRegistry;
import com.syntheticdata.expression.plugins.RandStrPlugin;
import com.syntheticdata.expression.plugins.UuidPlugin;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PluginSystemTest {

    private ExpressionProcessor processor;
    private CustomPatternGenerator generator;

    @Before
    public void setUp() {
        PluginRegistry registry = new PluginRegistry();
        registry.register(new UuidPlugin());
        registry.register(new RandStrPlugin());
        registry.register(new AlphaPlugin());

        processor = new ExpressionProcessor(registry);
        generator = new CustomPatternGenerator(processor);
    }

    @Test
    public void testUuidPlugin() {
        String result = generator.generate("{{UUID()}}");
        assertNotNull(result);
        assertTrue(result.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    public void testRandStrPlugin() {
        String result = generator.generate("{{RANDSTR(10)}}");
        assertNotNull(result);
        assertEquals(10, result.length());
        assertTrue(result.matches("[a-zA-Z0-9]+"));
    }

    @Test
    public void testAlphaPlugin() {
        String result = generator.generate("{{ALPHA(5)}}");
        assertNotNull(result);
        assertEquals(5, result.length());
        assertTrue(result.matches("[a-zA-Z]+"));
    }

    @Test
    public void testNestedExpressionsWithPlugins() {
        String result = generator.generate("user_{{faker.name().lastName()}}_{{RANDSTR(4)}}");
        assertNotNull(result);
        assertFalse(result.contains("{{"));
        assertTrue(result.matches("user_\\w+_[a-zA-Z0-9]{4}"));
    }

    @Test
    public void testCustomPatternsWithPlugins() {
        String result = generator.generate("ID-{{ALPHA(3)}}-####");
        assertNotNull(result);
        assertFalse(result.contains("{{"));
        assertTrue(result.matches("ID-[a-zA-Z]{3}-\\d{4}"));
    }
}
