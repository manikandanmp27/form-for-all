package com.fillforme.backend.chatbot.dto;

import java.time.Instant;

public class ChatMessageResponse {

    private String reply;
    private String timestamp = Instant.now().toString();
    private String provider;

    public ChatMessageResponse() {}

    public ChatMessageResponse(String reply, String timestamp, String provider) {
        this.reply = reply;
        this.timestamp = timestamp != null ? timestamp : Instant.now().toString();
        this.provider = provider;
    }

    public static ChatMessageResponseBuilder builder() {
        return new ChatMessageResponseBuilder();
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public static class ChatMessageResponseBuilder {
        private String reply;
        private String timestamp = Instant.now().toString();
        private String provider;

        ChatMessageResponseBuilder() {}

        public ChatMessageResponseBuilder reply(String reply) { this.reply = reply; return this; }
        public ChatMessageResponseBuilder timestamp(String timestamp) { this.timestamp = timestamp; return this; }
        public ChatMessageResponseBuilder provider(String provider) { this.provider = provider; return this; }

        public ChatMessageResponse build() {
            return new ChatMessageResponse(reply, timestamp, provider);
        }
    }
}
