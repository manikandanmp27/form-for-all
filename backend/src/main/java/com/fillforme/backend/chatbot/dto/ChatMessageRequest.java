package com.fillforme.backend.chatbot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageRequest {

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;

    @Valid
    private List<ChatMessageDto> history = new ArrayList<>();

    public ChatMessageRequest() {}

    public ChatMessageRequest(String message, List<ChatMessageDto> history) {
        this.message = message;
        this.history = history != null ? history : new ArrayList<>();
    }

    public static ChatMessageRequestBuilder builder() {
        return new ChatMessageRequestBuilder();
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<ChatMessageDto> getHistory() { return history; }
    public void setHistory(List<ChatMessageDto> history) { this.history = history; }

    public static class ChatMessageRequestBuilder {
        private String message;
        private List<ChatMessageDto> history = new ArrayList<>();

        ChatMessageRequestBuilder() {}

        public ChatMessageRequestBuilder message(String message) { this.message = message; return this; }
        public ChatMessageRequestBuilder history(List<ChatMessageDto> history) { this.history = history; return this; }

        public ChatMessageRequest build() {
            return new ChatMessageRequest(message, history);
        }
    }
}
