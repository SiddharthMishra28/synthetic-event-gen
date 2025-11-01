package com.syntheticdata.processor;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.syntheticdata.expression.ExpressionProcessor;
import java.util.Map;

public class JsonPathProcessor {

    private final ExpressionProcessor expressionProcessor;

    public JsonPathProcessor(ExpressionProcessor expressionProcessor) {
        this.expressionProcessor = expressionProcessor;
    }

    public String process(String json, Map<String, String> mappings) {
        DocumentContext doc = JsonPath.parse(json);
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            String path = entry.getKey();
            String expression = entry.getValue();
            String value = expressionProcessor.evaluate(expression);
            doc.set(path, value);
        }
        return doc.jsonString();
    }
}
