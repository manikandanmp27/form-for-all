package com.fillforme.backend.chatbot.controller;

import com.fillforme.backend.chatbot.dto.ChatMessageRequest;
import com.fillforme.backend.chatbot.dto.ChatMessageResponse;
import com.fillforme.backend.chatbot.service.ChatbotService;
import com.fillforme.backend.common.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    public ResponseEntity<ChatMessageResponse> chat(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChatMessageRequest request) {
        UUID userId = userPrincipal != null ? userPrincipal.getId() : null;
        ChatMessageResponse response = chatbotService.processChatMessage(request, userId);
        return ResponseEntity.ok(response);
    }
}
