package com.syntheticdata.engine;

import com.syntheticdata.expression.CustomPatternGenerator;
import com.syntheticdata.expression.ExpressionProcessor;
import com.syntheticdata.expression.plugins.AlphaPlugin;
import com.syntheticdata.expression.plugins.PluginRegistry;
import com.syntheticdata.expression.plugins.RandStrPlugin;
import com.syntheticdata.expression.plugins.UuidPlugin;
import com.syntheticdata.loader.ConfigLoader;
import com.syntheticdata.model.PayloadConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.syntheticdata.processor.JsonPathProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SyntheticDataEngine {

    private final ExpressionProcessor expressionProcessor;
    private final CustomPatternGenerator patternGenerator;
    private final JsonPathProcessor jsonPathProcessor;
    private final ConfigLoader configLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        // Mask REF placeholders
        Pattern refPattern = Pattern.compile("(\\{\\{REF:[^}]+}})");
        Matcher refMatcher = refPattern.matcher(inputTemplate);
        List<String> refs = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        while (refMatcher.find()) {
            refs.add(refMatcher.group(1));
            refMatcher.appendReplacement(sb, "__REF_" + (refs.size() - 1) + "__");
        }
        refMatcher.appendTail(sb);
        String maskedTemplate = sb.toString();

        String resolved = expressionProcessor.evaluate(maskedTemplate);
        resolved = patternGenerator.process(resolved);

        // Unmask REF placeholders
        for (int i = 0; i < refs.size(); i++) {
            resolved = resolved.replace("__REF_" + i + "__", refs.get(i));
        }

        try {
            JsonNode rootNode = objectMapper.readTree(resolved);
            processJsonNode(rootNode);
            resolved = objectMapper.writeValueAsString(rootNode);
            resolved = resolveReferences(resolved);
            resolved = resolved.replaceAll("\\\\/", "/");
        } catch (IOException e) {
            // Not a valid JSON, so return as is
        }

        return resolved;
    }

    private String resolveReferences(String jsonString) {
        String currentJson = jsonString;
        for (int i = 0; i < 10; i++) { // Max 10 iterations to prevent infinite loops
            String nextJson = com.jayway.jsonpath.JsonPath.parse(currentJson).jsonString();
            java.util.regex.Pattern refPattern = java.util.regex.Pattern.compile("\\{\\{REF:([^}]+)\\}\\}");
            java.util.regex.Matcher matcher = refPattern.matcher(nextJson);
            StringBuffer sb = new StringBuffer();
            boolean found = false;
            while (matcher.find()) {
                found = true;
                String jsonPath = matcher.group(1);
                Object value = com.jayway.jsonpath.JsonPath.read(currentJson, jsonPath);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(value)));
            }
            matcher.appendTail(sb);
            currentJson = sb.toString();
            if (!found) {
                break;
            }
        }
        return currentJson;
    }

    private void processJsonNode(JsonNode parent, JsonNode node, String fieldName, int index) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                processJsonNode(node, field.getValue(), field.getKey(), -1);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                processJsonNode(node, node.get(i), null, i);
            }
        } else if (node.isTextual()) {
            String textValue = node.asText();
            if (textValue.contains("{{")) {
                String generatedValue = generate(textValue);
                if (parent.isObject()) {
                    ((ObjectNode) parent).put(fieldName, generatedValue);
                } else if (parent.isArray()) {
                    ((com.fasterxml.jackson.databind.node.ArrayNode) parent).set(index, objectMapper.convertValue(generatedValue, JsonNode.class));
                }
            }
        }
    }

    private void processJsonNode(JsonNode node) {
        processJsonNode(null, node, null, -1);
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
