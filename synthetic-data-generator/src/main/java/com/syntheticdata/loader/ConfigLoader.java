package com.syntheticdata.loader;

import com.syntheticdata.model.PayloadConfig;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.*;

public class ConfigLoader {

    public PayloadConfig loadConfig(File configFile) throws IOException {
        String fileName = configFile.getName();
        Map<String, String> mappings = new LinkedHashMap<>();

        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                Yaml yaml = new Yaml();
                Map<String, Object> yamlData = yaml.load(fis);
                yamlData.forEach((k, v) -> mappings.put(k, String.valueOf(v)));
            }
        } else if (fileName.endsWith(".properties")) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                Properties props = new Properties();
                props.load(fis);
                for (String key : props.stringPropertyNames()) {
                    mappings.put(key, props.getProperty(key));
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported config type: " + fileName);
        }

        PayloadConfig config = new PayloadConfig();
        config.setFieldMappings(mappings);
        return config;
    }
}
