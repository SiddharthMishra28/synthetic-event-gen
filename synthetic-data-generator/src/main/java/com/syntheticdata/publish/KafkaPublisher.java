package com.syntheticdata.publish;

import com.syntheticdata.model.Config;
import com.syntheticdata.model.PayloadConfig;
import com.syntheticdata.output.OutputHandler;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaPublisher implements OutputHandler {
    private final ExecutorService executor;
    private final Producer<String, String> producer;
    private final Map<String, String> topicMap = new HashMap<>();

    public KafkaPublisher(Config config) {
        this(config, createProducer(config));
    }

    public KafkaPublisher(Config config, Producer<String, String> producer) {
        this.executor = Executors.newFixedThreadPool(config.getThreadCount());
        this.producer = producer;
        loadTopicMap();
    }

    private static Producer<String, String> createProducer(Config config) {
        Properties props = new Properties();
        props.put("bootstrap.servers", config.getKafka().getBootstrapServers());
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }

    private void loadTopicMap() {
        for (Field field : PublisherConstants.class.getDeclaredFields()) {
            try {
                String value = (String) field.get(null);
                String[] parts = value.split(":", 2);
                if (parts.length == 2) {
                    topicMap.put(parts[0], parts[1]);
                }
            } catch (IllegalAccessException e) {
                // Ignore
            }
        }
    }

    @Override
    public void handle(String data, PayloadConfig payloadConfig) {
        String topic = topicMap.get(payloadConfig.getFile());
        if (topic != null) {
            executor.submit(() -> producer.send(new ProducerRecord<>(topic, data)));
        }
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        producer.close();
    }
}
