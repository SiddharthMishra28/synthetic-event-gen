package com.syntheticdata.model;

import com.fasterxml.jackson.databind.JsonNode;

public class PayloadTemplate {
    private String name;
    private JsonNode jsonContent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonNode getJsonContent() {
        return jsonContent;
    }

    public void setJsonContent(JsonNode jsonContent) {
        this.jsonContent = jsonContent;
    }
}
