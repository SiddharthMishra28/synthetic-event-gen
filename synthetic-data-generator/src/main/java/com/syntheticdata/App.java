package com.syntheticdata;

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
        System.out.println("Starting synthetic data generation...");

        try {
            // Locate the payloads directory from classpath resources
            URL payloadsUrl = App.class.getClassLoader().getResource("payloads");
            if (payloadsUrl == null) {
                System.err.println("Payloads directory not found in resources.");
                return;
            }
            File payloadsDir = new File(payloadsUrl.toURI());

            // Create the output directory if it doesn't exist
            Path outputDir = Paths.get("src/main/resources/synthetic-data");
            Files.createDirectories(outputDir);

            SyntheticDataEngine engine = new SyntheticDataEngine();

            // Process all files in the directory
            List<String> results = engine.generateFromDirectory(payloadsDir.getAbsolutePath());

            // Write the results to the output directory
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
