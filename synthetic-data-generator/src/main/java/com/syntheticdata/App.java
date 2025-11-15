package com.syntheticdata;

import com.syntheticdata.engine.SyntheticDataEngine;
import com.syntheticdata.loader.ConfigLoader;
import com.syntheticdata.model.Config;
import com.syntheticdata.model.PayloadConfig;
import com.syntheticdata.output.FileHandler;
import com.syntheticdata.output.OutputHandler;
import com.syntheticdata.output.StdoutHandler;
import com.syntheticdata.publish.KafkaPublisher;

public class App {
    public static void main(String[] args) {
        System.out.println("Starting synthetic data generation...");

        try {
            ConfigLoader loader = new ConfigLoader();
            Config config = loader.load("config.yaml", Config.class);

            OutputHandler handler;
            switch (config.getOutputMode().toLowerCase()) {
                case "stdout":
                    handler = new StdoutHandler();
                    break;
                case "file":
                    handler = new FileHandler();
                    break;
                case "publish":
                    handler = new KafkaPublisher(config);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid output mode: " + config.getOutputMode());
            }

            SyntheticDataEngine engine = new SyntheticDataEngine();

            for (PayloadConfig payloadConfig : config.getPayloads()) {
                for (int i = 0; i < payloadConfig.getCount(); i++) {
                    String data = engine.generateFromFile("src/main/resources/payloads/" + payloadConfig.getFile());
                    handler.handle(data, payloadConfig);
                }
            }

            handler.shutdown();

            System.out.println("Synthetic data generation complete.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
