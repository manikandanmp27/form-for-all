package com.fillforme.backend.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatMessageDto {

    @NotBlank
    private String role; // "user" or "assistant"

    @NotBlank
    @Size(max = 1000)
    private String content;

    public ChatMessageDto() {}

    public ChatMessageDto(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static ChatMessageDtoBuilder builder() {
        return new ChatMessageDtoBuilder();
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public static class ChatMessageDtoBuilder {
        private String role;
        private String content;

        ChatMessageDtoBuilder() {}

        public ChatMessageDtoBuilder role(String role) { this.role = role; return this; }
        public ChatMessageDtoBuilder content(String content) { this.content = content; return this; }

        public ChatMessageDto build() {
            return new ChatMessageDto(role, content);
        }
    }
}
