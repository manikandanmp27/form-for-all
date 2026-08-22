package com.fillforme.backend.chatbot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fillforme.backend.chatbot.dto.ChatMessageDto;
import com.fillforme.backend.chatbot.dto.ChatMessageRequest;
import com.fillforme.backend.chatbot.dto.ChatMessageResponse;
import com.fillforme.backend.chatbot.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotServiceImpl.class);

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.provider:gemini}")
    private String aiProvider;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final List<String> GEMINI_MODELS = List.of(
            "gemini-2.5-flash",
            "gemini-2.0-flash",
            "gemini-1.5-flash-latest",
            "gemini-1.5-pro"
    );

    private static final String SYSTEM_PROMPT = """
            You are the official AI Assistant for Form-for-All.
            Form-for-All is an accessible, AI-powered platform designed to make filling out online and PDF forms effortless and transparent for everyone, including elderly individuals and users with cognitive, visual, or motor impairments.
            
            Key Platform Capabilities:
            1. Form Ingestion: Users can upload PDF documents or enter a website URL to automatically extract form fields.
            2. Gemini Vision Field Extraction: Uses AI vision models to detect fields, labels, input types, and required statuses.
            3. Plain-Language Guidance: Simplifies complex legalistic or medical questions into plain language and plain questions.
            4. Risk & Security Alerts: Analyzes field sensitivity (Aadhaar, PAN, SSN, financial accounts, nominee changes) and displays HIGH/STANDARD risk warnings before submission.
            5. Accessibility Profiles: Customizes cognitive load, contrast, and font size based on user preferences.
            6. Interactive Form Filling & Multi-modal Export: Guided step-by-step filling and exporting to filled PDF or structured JSON.
            
            Guidelines:
            - Answer user questions accurately, concisely, and warmly.
            - Keep responses under 4 short sentences or simple bullet points when explaining steps.
            - Never expose API keys, internal credentials, or code.
            - If asked about unsupported features, explain what Form-for-All currently provides.
            """;

    public ChatbotServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ChatMessageResponse processChatMessage(ChatMessageRequest request, UUID userId) {
        String userMessage = request.getMessage() != null ? request.getMessage().trim() : "";
        if (userMessage.isBlank()) {
            return ChatMessageResponse.builder()
                    .reply("Please type a message so I can assist you!")
                    .timestamp(Instant.now().toString())
                    .provider("system")
                    .build();
        }

        // Limit context history length to max 10 messages
        List<ChatMessageDto> safeHistory = new ArrayList<>();
        if (request.getHistory() != null) {
            int start = Math.max(0, request.getHistory().size() - 10);
            for (int i = start; i < request.getHistory().size(); i++) {
                ChatMessageDto msg = request.getHistory().get(i);
                if (msg != null && msg.getContent() != null && !msg.getContent().isBlank()) {
                    safeHistory.add(msg);
                }
            }
        }

        if (apiKey != null && !apiKey.isBlank()) {
            log.info("Sending chat request to Gemini AI for user message: '{}'", userMessage);
            String aiReply = callGeminiForChat(userMessage, safeHistory);
            if (aiReply != null && !aiReply.isBlank()) {
                return ChatMessageResponse.builder()
                        .reply(aiReply)
                        .timestamp(Instant.now().toString())
                        .provider("gemini")
                        .build();
            }
        }

        log.info("AI API key missing or external call failed. Using Form-for-All intelligent domain fallback.");
        String fallbackReply = generateDomainFallback(userMessage);
        return ChatMessageResponse.builder()
                .reply(fallbackReply)
                .timestamp(Instant.now().toString())
                .provider("fallback-engine")
                .build();
    }

    private String callGeminiForChat(String userMessage, List<ChatMessageDto> history) {
        List<Map<String, Object>> contents = new ArrayList<>();

        // Add system instruction prompt
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", "SYSTEM INSTRUCTION:\n" + SYSTEM_PROMPT))
        ));
        contents.add(Map.of(
                "role", "model",
                "parts", List.of(Map.of("text", "Understood! I am the official Form-for-All AI Assistant. How can I help you today?"))
        ));

        // Append historical conversation context
        for (ChatMessageDto msg : history) {
            String role = "assistant".equalsIgnoreCase(msg.getRole()) ? "model" : "user";
            contents.add(Map.of(
                    "role", role,
                    "parts", List.of(Map.of("text", msg.getContent()))
            ));
        }

        // Append active user message
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userMessage))
        ));

        Map<String, Object> body = Map.of("contents", contents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        for (String model : GEMINI_MODELS) {
            try {
                String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode candidates = root.path("candidates");
                    if (candidates.isArray() && candidates.size() > 0) {
                        String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
                        if (text != null && !text.isBlank()) {
                            return text.trim();
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Gemini model {} failed for chatbot query: {}", model, e.getMessage());
            }
        }

        return null;
    }

    private String generateDomainFallback(String userMessage) {
        String query = userMessage.toLowerCase(Locale.ROOT);

        if (query.contains("upload") || query.contains("pdf") || query.contains("file") || query.contains("url") || query.contains("new form")) {
            return "To process a form, navigate to 'Upload / Fill Form' from the top bar. You can upload a PDF document or paste a public web URL. Form-for-All will extract all fields automatically!";
        } else if (query.contains("risk") || query.contains("warning") || query.contains("security") || query.contains("alert")) {
            return "Form-for-All actively evaluates field risk. When you fill out sensitive inputs like Aadhaar, PAN, SSN, bank account numbers, or nominee designations, we display clear security risk alerts to protect your privacy.";
        } else if (query.contains("export") || query.contains("download") || query.contains("json")) {
            return "Once you review your form entries, you can export your completed form as a filled PDF document or as a structured JSON file on the Complete page.";
        } else if (query.contains("cognitive") || query.contains("profile") || query.contains("accessibility") || query.contains("font") || query.contains("theme")) {
            return "You can customize accessibility settings in your Profile! Options include High Contrast, Larger Fonts, Reduced Motion, and Low Cognitive Load (simplifying complex terminology).";
        } else if (query.contains("login") || query.contains("account") || query.contains("register") || query.contains("demo")) {
            return "You can log in or register using the buttons at the top right. We also support a 1-Click Demo Login on the login page for instant testing!";
        } else if (query.contains("hi") || query.contains("hello") || query.contains("hey")) {
            return "Hello! I am your Form-for-All Assistant. How can I help you upload, understand, or fill out forms today?";
        } else {
            return "Form-for-All makes online & PDF forms easy to understand and fill! You can upload forms, receive plain-language guidance, check real-time risk alerts, and export completed forms anytime.";
        }
    }
}
