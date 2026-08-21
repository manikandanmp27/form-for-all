package com.fillforme.backend.conversation.controller;

import com.fillforme.backend.common.security.UserPrincipal;
import com.fillforme.backend.conversation.dto.ConversationStepResponse;
import com.fillforme.backend.conversation.dto.SubmitAnswerRequest;
import com.fillforme.backend.conversation.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sessions/{sessionId}/conversation")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public ResponseEntity<ConversationStepResponse> getConversationStep(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID sessionId) {
        ConversationStepResponse response = conversationService.getConversationStep(sessionId, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ConversationStepResponse> submitAnswer(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID sessionId,
            @Valid @RequestBody SubmitAnswerRequest request) {
        ConversationStepResponse response = conversationService.submitAnswer(sessionId, userPrincipal.getId(), request);
        return ResponseEntity.ok(response);
    }
}
