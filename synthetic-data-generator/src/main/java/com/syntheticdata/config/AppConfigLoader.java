package com.syntheticdata.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class AppConfigLoader {
    private final ObjectMapper yamlMapper;
    private final ObjectMapper propertiesMapper;

    public AppConfigLoader() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.propertiesMapper = new JavaPropsMapper();
    }

    public Config loadConfig(File configFile) throws IOException {
        if (configFile.getName().endsWith(".yaml") || configFile.getName().endsWith(".yml")) {
            return yamlMapper.readValue(configFile, Config.class);
        } else if (configFile.getName().endsWith(".properties")) {
            return propertiesMapper.readValue(configFile, Config.class);
        } else {
            throw new IllegalArgumentException("Unsupported config file format: " + configFile.getName());
        }
    }

    public File findConfigFile() {
        URL yamlUrl = getClass().getClassLoader().getResource("config.yaml");
        if (yamlUrl != null) {
            return new File(yamlUrl.getFile());
        }
        URL propertiesUrl = getClass().getClassLoader().getResource("config.properties");
        if (propertiesUrl != null) {
            return new File(propertiesUrl.getFile());
        }
        return null;
    }
}
