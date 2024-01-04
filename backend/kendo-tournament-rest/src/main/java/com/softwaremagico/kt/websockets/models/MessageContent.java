package com.softwaremagico.kt.websockets.models;

public class MessageContent {

    private final String topic;
    private String payload;

    public MessageContent(String topic, String payload) {
        this.topic = topic;
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
