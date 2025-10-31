package com.syntheticdata.engine;

import com.syntheticdata.expression.CustomPatternGenerator;
import com.syntheticdata.expression.ExpressionProcessor;
import com.syntheticdata.expression.plugins.AlphaPlugin;
import com.syntheticdata.expression.plugins.PluginRegistry;
import com.syntheticdata.expression.plugins.RandStrPlugin;
import com.syntheticdata.expression.plugins.UuidPlugin;

public class SyntheticDataEngine {

    private final ExpressionProcessor expressionProcessor;
    private final CustomPatternGenerator patternGenerator;

    public SyntheticDataEngine() {
        PluginRegistry registry = new PluginRegistry();
        registry.register(new UuidPlugin());
        registry.register(new RandStrPlugin());
        registry.register(new AlphaPlugin());

        this.expressionProcessor = new ExpressionProcessor(registry);
        this.patternGenerator = new CustomPatternGenerator();
    }

    public String generate(String inputTemplate) {
        if (inputTemplate == null || inputTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("Input template cannot be null or empty");
        }

        String resolved = expressionProcessor.evaluate(inputTemplate);
        resolved = patternGenerator.process(resolved);

        return resolved;
    }

    public static String generateData(String template) {
        return new SyntheticDataEngine().generate(template);
    }
}
