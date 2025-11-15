package com.syntheticdata.output;

import com.syntheticdata.model.PayloadConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler implements OutputHandler {
    private final Path outputDir;

    public FileHandler() {
        this("synthetic-data");
    }

    public FileHandler(String outputDir) {
        this.outputDir = Paths.get(outputDir);
        try {
            Files.createDirectories(this.outputDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create output directory", e);
        }
    }

    @Override
    public void handle(String data, PayloadConfig payloadConfig) {
        try {
            String fileName = payloadConfig.getName() + "_" + System.currentTimeMillis() + ".json";
            Files.write(outputDir.resolve(fileName), data.getBytes());
        } catch (IOException e) {
            System.err.println("Failed to write to file: " + e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        // No resources to shut down
    }
}
