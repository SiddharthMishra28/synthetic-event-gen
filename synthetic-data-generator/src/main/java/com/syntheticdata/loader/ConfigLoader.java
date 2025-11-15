package com.syntheticdata.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.syntheticdata.model.PayloadConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigLoader {

    public <T> T load(String resourceName, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IOException("Resource not found: " + resourceName);
        }
        return mapper.readValue(is, clazz);
    }

    public PayloadConfig loadConfig(File configFile) throws IOException {
        ObjectMapper mapper;
        if (configFile.getName().endsWith(".yaml") || configFile.getName().endsWith(".yml")) {
            mapper = new ObjectMapper(new YAMLFactory());
        } else {
            mapper = new ObjectMapper();
        }

        try {
            return mapper.readValue(configFile, PayloadConfig.class);
        } catch (UnrecognizedPropertyException e) {
            // Fallback for old config format
            Map<String, String> mappings;
            if (configFile.getName().endsWith(".yaml") || configFile.getName().endsWith(".yml")) {
                mappings = mapper.readValue(configFile, new TypeReference<Map<String, String>>() {});
            } else {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
                mappings = new HashMap<>();
                for (String key : props.stringPropertyNames()) {
                    mappings.put(key, props.getProperty(key));
                }
            }
            PayloadConfig config = new PayloadConfig();
            config.setFieldMappings(mappings);
            return config;
        }
    }
}
