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

    public List<String> generate(String inputTemplate, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be a positive integer.");
        }
        List<String> results = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            results.add(generate(inputTemplate));
        }
        return results;
    }

    public String generateFromFile(String filePath) throws IOException {
        Path inputPath = Paths.get(filePath);
        if (!Files.exists(inputPath)) {
            throw new IOException("File not found: " + filePath);
        }

        String content = new String(Files.readAllBytes(inputPath));
        File configFile = findConfigFile(filePath);

        if (configFile != null) {
            PayloadConfig config = configLoader.loadConfig(configFile);
            return jsonPathProcessor.process(content, config.getFieldMappings());
        } else {
            return generate(content);
        }
    }

    public List<String> generateFromFile(String filePath, int count) throws IOException {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be a positive integer.");
        }
        List<String> results = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            results.add(generateFromFile(filePath));
        }
        return results;
    }

    public List<String> generateFromDirectory(String dirPath) throws IOException {
        return generateFromDirectory(dirPath, 1);
    }

    public List<String> generateFromDirectory(String dirPath, int count) throws IOException {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be a positive integer.");
        }
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
            results.addAll(generateFromFile(jsonFile.getAbsolutePath(), count));
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

    public static List<String> generateData(String template, int count) {
        return new SyntheticDataEngine().generate(template, count);
    }
}
