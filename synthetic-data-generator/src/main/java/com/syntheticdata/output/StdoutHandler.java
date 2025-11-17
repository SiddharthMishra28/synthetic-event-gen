package com.syntheticdata.output;

import com.syntheticdata.model.PayloadConfig;

public class StdoutHandler implements OutputHandler {

    @Override
    public void handle(String data, PayloadConfig payloadConfig) {
        System.out.println(data);
    }

    @Override
    public void shutdown() {
        // No resources to shut down
    }
}
