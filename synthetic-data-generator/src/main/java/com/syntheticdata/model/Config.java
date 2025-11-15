package com.syntheticdata.model;

import java.util.List;

public class Config {
    private String outputMode;
    private int threadCount;
    private KafkaConfig kafka;
    private List<PayloadConfig> payloads;

    public String getOutputMode() {
        return outputMode;
    }

    public void setOutputMode(String outputMode) {
        this.outputMode = outputMode;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public KafkaConfig getKafka() {
        return kafka;
    }

    public void setKafka(KafkaConfig kafka) {
        this.kafka = kafka;
    }

    public List<PayloadConfig> getPayloads() {
        return payloads;
    }

    public void setPayloads(List<PayloadConfig> payloads) {
        this.payloads = payloads;
    }
}
