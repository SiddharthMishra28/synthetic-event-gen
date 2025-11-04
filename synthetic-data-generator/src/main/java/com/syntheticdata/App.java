package com.syntheticdata;

import com.syntheticdata.config.AppConfigLoader;
import com.syntheticdata.config.Config;
import com.syntheticdata.engine.SyntheticDataEngine;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        try {
            AppConfigLoader configLoader = new AppConfigLoader();
            File configFile = configLoader.findConfigFile();

            if (configFile == null) {
                System.err.println("Configuration file (config.yaml or config.properties) not found.");
                return;
            }

            Config config = configLoader.loadConfig(configFile);

            if ("database".equalsIgnoreCase(config.getOutputMode())) {
                System.out.println("Starting synthetic data generation and publishing...");
                SyntheticDataEngine engine = new SyntheticDataEngine(config.getDatabase());
                engine.generateAndPublish(config.getTemplate(), config.getCount()).join();
                engine.close();
                System.out.println("Synthetic data generation and publishing complete.");
            } else {
                generateDataAsFiles();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateDataAsFiles() throws Exception {
        System.out.println("Starting synthetic data generation...");
        URL payloadsUrl = App.class.getClassLoader().getResource("payloads");
        if (payloadsUrl == null) {
            System.err.println("Payloads directory not found in resources.");
            return;
        }
        File payloadsDir = new File(payloadsUrl.toURI());

        Path outputDir = Paths.get("src/main/resources/synthetic-data");
        Files.createDirectories(outputDir);

        SyntheticDataEngine engine = new SyntheticDataEngine();
        List<String> results = engine.generateFromDirectory(payloadsDir.getAbsolutePath());

        List<File> sourceFiles = Files.walk(payloadsDir.toPath())
            .filter(path -> path.toString().endsWith(".json"))
            .map(Path::toFile)
            .collect(Collectors.toList());

        for (int i = 0; i < results.size(); i++) {
            File sourceFile = sourceFiles.get(i);
            String result = results.get(i);
            Path outputPath = outputDir.resolve(sourceFile.getName());
            Files.write(outputPath, result.getBytes());
            System.out.println("Generated: " + outputPath);
        }

        System.out.println("Synthetic data generation complete.");
    }
}
