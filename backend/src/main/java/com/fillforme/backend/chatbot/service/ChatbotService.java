package com.fillforme.backend.chatbot.service;

import com.fillforme.backend.chatbot.dto.ChatMessageRequest;
import com.fillforme.backend.chatbot.dto.ChatMessageResponse;

import java.util.UUID;

public interface ChatbotService {
    ChatMessageResponse processChatMessage(ChatMessageRequest request, UUID userId);
}
