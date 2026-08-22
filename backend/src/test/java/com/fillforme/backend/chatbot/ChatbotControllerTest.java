package com.fillforme.backend.chatbot;

import com.fillforme.backend.chatbot.dto.ChatMessageDto;
import com.fillforme.backend.chatbot.dto.ChatMessageRequest;
import com.fillforme.backend.chatbot.dto.ChatMessageResponse;
import com.fillforme.backend.chatbot.service.ChatbotService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ChatbotControllerTest {

    @Autowired
    private ChatbotService chatbotService;

    @Test
    void testProcessChatMessageWithNormalQuery() {
        ChatMessageRequest request = ChatMessageRequest.builder()
                .message("How do I upload a form PDF?")
                .history(List.of(
                        ChatMessageDto.builder().role("user").content("Hello").build(),
                        ChatMessageDto.builder().role("assistant").content("Hi! How can I help you today?").build()
                ))
                .build();

        ChatMessageResponse response = chatbotService.processChatMessage(request, null);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getReply());
        Assertions.assertFalse(response.getReply().isBlank());
        Assertions.assertNotNull(response.getTimestamp());
        Assertions.assertNotNull(response.getProvider());
    }

    @Test
    void testProcessChatMessageWithBlankMessage() {
        ChatMessageRequest request = ChatMessageRequest.builder()
                .message("   ")
                .build();

        ChatMessageResponse response = chatbotService.processChatMessage(request, null);

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.getReply().contains("Please type a message"));
    }

    @Test
    void testProcessChatMessageWithRiskAlertQuery() {
        ChatMessageRequest request = ChatMessageRequest.builder()
                .message("What are risk alerts and warnings?")
                .build();

        ChatMessageResponse response = chatbotService.processChatMessage(request, null);

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.getReply().toLowerCase().contains("risk") || response.getReply().toLowerCase().contains("alert") || response.getReply().toLowerCase().contains("form"));
    }
}
