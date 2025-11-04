package com.syntheticdata;

import com.syntheticdata.cli.CmdLineParser;
import com.syntheticdata.engine.SyntheticDataEngine;
import com.syntheticdata.publish.DatabaseConfig;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        parser.parse(args);

        if (parser.hasArgument("dbUrl")) {
            handleDatabasePublishing(parser);
        } else {
            handleFileGeneration();
        }
    }

    private static void handleDatabasePublishing(CmdLineParser parser) {
        System.out.println("Starting synthetic data generation and publishing...");
        try {
            DatabaseConfig dbConfig = new DatabaseConfig(
                parser.getArgument("dbUrl"),
                parser.getArgument("dbUser"),
                parser.getArgument("dbPassword"),
                parser.getArgument("dbTable"),
                parser.getArgument("dbColumn")
            );

            int count = Integer.parseInt(parser.getArgument("count"));
            String templatePath = parser.getArgument("template");

            Path inputPath = Paths.get(templatePath);
            if (!Files.exists(inputPath)) {
                System.err.println("Template file not found: " + templatePath);
                return;
            }
            String template = new String(Files.readAllBytes(inputPath));

            SyntheticDataEngine engine = new SyntheticDataEngine(dbConfig);
            engine.generateAndPublish(template, count).join(); // Wait for completion
            engine.close();

            System.out.println("Synthetic data generation and publishing complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleFileGeneration() {
        System.out.println("Starting synthetic data generation...");
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
