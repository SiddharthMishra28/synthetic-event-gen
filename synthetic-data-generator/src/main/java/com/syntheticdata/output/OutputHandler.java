package com.syntheticdata.output;

import com.syntheticdata.model.PayloadConfig;

public interface OutputHandler {
    void handle(String data, PayloadConfig payloadConfig);
    void shutdown();
}
