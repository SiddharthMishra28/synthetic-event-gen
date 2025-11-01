package com.syntheticdata.engine;

import com.syntheticdata.expression.CustomPatternGenerator;
import com.syntheticdata.expression.ExpressionProcessor;
import com.syntheticdata.expression.plugins.AlphaPlugin;
import com.syntheticdata.expression.plugins.PluginRegistry;
import com.syntheticdata.expression.plugins.RandStrPlugin;
import com.syntheticdata.expression.plugins.UuidPlugin;
import com.syntheticdata.loader.ConfigLoader;
import com.syntheticdata.model.PayloadConfig;
import com.syntheticdata.processor.JsonPathProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SyntheticDataEngine {

    private final ExpressionProcessor expressionProcessor;
    private final CustomPatternGenerator patternGenerator;
    private final JsonPathProcessor jsonPathProcessor;
    private final ConfigLoader configLoader;

    public SyntheticDataEngine() {
        PluginRegistry registry = new PluginRegistry();
        registry.register(new UuidPlugin());
        registry.register(new RandStrPlugin());
        registry.register(new AlphaPlugin());

        this.expressionProcessor = new ExpressionProcessor(registry);
        this.patternGenerator = new CustomPatternGenerator();
        this.jsonPathProcessor = new JsonPathProcessor(expressionProcessor);
        this.configLoader = new ConfigLoader();
    }

    public String generate(String inputTemplate) {
        if (inputTemplate == null || inputTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("Input template cannot be null or empty");
        }
        String resolved = expressionProcessor.evaluate(inputTemplate);
        resolved = patternGenerator.process(resolved);
        return resolved;
    }

    public String generateFromFile(String filePath) throws IOException {
        Path inputPath = Paths.get(filePath);
        if (!Files.exists(inputPath)) {
            throw new IOException("File not found: " + filePath);
        }

        String content = new String(Files.readAllBytes(inputPath));
        File configFile = findConfigFile(filePath);

        if (configFile != null) {
            // JSONPath-based processing
            PayloadConfig config = configLoader.loadConfig(configFile);
            return jsonPathProcessor.process(content, config.getFieldMappings());
        } else {
            // Direct processing
            return generate(content);
        }
    }

    public List<String> generateFromDirectory(String dirPath) throws IOException {
        List<String> results = new ArrayList<>();
        List<File> jsonFiles;
        try (Stream<Path> stream = Files.walk(Paths.get(dirPath))) {
            jsonFiles = stream
                .filter(file -> !Files.isDirectory(file))
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".json"))
                .collect(Collectors.toList());
        }

        for (File jsonFile : jsonFiles) {
            results.add(generateFromFile(jsonFile.getAbsolutePath()));
        }
        return results;
    }

    private File findConfigFile(String jsonFilePath) {
        String baseName = jsonFilePath.substring(0, jsonFilePath.lastIndexOf('.'));
        File yamlConfig = new File(baseName + "_config.yaml");
        if (yamlConfig.exists()) {
            return yamlConfig;
        }
        File propertiesConfig = new File(baseName + "_config.properties");
        if (propertiesConfig.exists()) {
            return propertiesConfig;
        }
        return null;
    }

    public static String generateData(String template) {
        return new SyntheticDataEngine().generate(template);
    }
}
