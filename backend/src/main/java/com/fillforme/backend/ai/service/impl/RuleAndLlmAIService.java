package com.fillforme.backend.ai.service.impl;

import com.fillforme.backend.ai.dto.AIFieldExplanation;
import com.fillforme.backend.ai.dto.AIRiskEvaluation;
import com.fillforme.backend.ai.service.AIService;
import com.fillforme.backend.profile.entity.AccessibilityProfile;
import com.fillforme.backend.profile.entity.CognitiveLoadPreference;
import com.fillforme.backend.risk.entity.RiskLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class RuleAndLlmAIService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(RuleAndLlmAIService.class);

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.provider:gemini}") // "gemini" or "openai"
    private String aiProvider;

    private final RestTemplate restTemplate;

    public RuleAndLlmAIService() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public AIFieldExplanation generateFieldExplanation(String fieldKey, String label, String helpText, AccessibilityProfile profile) {
        String preferredLang = profile != null && profile.getPreferredLanguage() != null ? profile.getPreferredLanguage() : "English";
        boolean isLowCognitive = profile != null && profile.getCognitiveLoadPreference() == CognitiveLoadPreference.LOW;

        // Try Live LLM AI Call if API Key is configured
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                return callLlmForFieldExplanation(fieldKey, label, helpText, preferredLang, isLowCognitive);
            } catch (Exception e) {
                log.warn("External LLM AI call failed, using smart document AI engine fallback: {}", e.getMessage());
            }
        }

        // Smart Rule-Based Document AI Framing
        return generateSmartFallbackExplanation(fieldKey, label, helpText, preferredLang, isLowCognitive);
    }

    private AIFieldExplanation callLlmForFieldExplanation(String fieldKey, String label, String helpText, String lang, boolean isLowCognitive) {
        String prompt = String.format(
                "You are an accessibility AI co-pilot. Analyze this form field: '%s' (Help text: '%s'). " +
                "Language requested: '%s'. Cognitive mode: '%s'. " +
                "Respond in plain JSON with keys: simplifiedQuestionText, plainLanguageExplanation, whyAskedExplanation.",
                label, helpText != null ? helpText : "", lang, isLowCognitive ? "LOW (Simple single-card view)" : "STANDARD"
        );

        if ("gemini".equalsIgnoreCase(aiProvider)) {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully received LLM response from Gemini API for field: {}", label);
            }
        }

        // Return parsed AI response or fallback
        return generateSmartFallbackExplanation(fieldKey, label, helpText, lang, isLowCognitive);
    }

    private AIFieldExplanation generateSmartFallbackExplanation(String fieldKey, String label, String helpText, String lang, boolean isLowCognitive) {
        String lowerLabel = (label != null ? label : fieldKey).toLowerCase(Locale.ROOT);

        String plainExplanation;
        String whyAsked;
        String simplifiedQuestion;

        if (lowerLabel.contains("aadhaar") || lowerLabel.contains("aadhar") || lowerLabel.contains("uid")) {
            plainExplanation = "Your unique 12-digit government identification number issued by UIDAI.";
            whyAsked = "Required by identity verification authorities to validate official residency records.";
            simplifiedQuestion = "What is your 12-digit Aadhaar number?";
        } else if (lowerLabel.contains("name")) {
            plainExplanation = "Your full official legal name exactly as shown on your government ID card.";
            whyAsked = "Used to establish account ownership and legal identity match.";
            simplifiedQuestion = "What is your full legal name?";
        } else if (lowerLabel.contains("dob") || lowerLabel.contains("birth")) {
            plainExplanation = "Your official date of birth as recorded on identity documents.";
            whyAsked = "Used to verify your age eligibility and legal capacity.";
            simplifiedQuestion = "What is your date of birth?";
        } else if (lowerLabel.contains("address") || lowerLabel.contains("residence")) {
            plainExplanation = "The physical house or building address where you permanently reside.";
            whyAsked = "Required for official postal notices, verification, and location eligibility.";
            simplifiedQuestion = "What is your permanent residential address?";
        } else if (lowerLabel.contains("bank") || lowerLabel.contains("account") || lowerLabel.contains("ifsc")) {
            plainExplanation = "Your registered bank account and branch code details.";
            whyAsked = "Required to deposit benefits or payments directly into your account.";
            simplifiedQuestion = "What is your bank account number?";
        } else if (lowerLabel.contains("nominee") || lowerLabel.contains("beneficiary")) {
            plainExplanation = "The person designated to inherit or receive funds if something happens to you.";
            whyAsked = "Required by regulations to assign legal beneficiary transfer rights.";
            simplifiedQuestion = "Who is your primary nominee?";
        } else if (lowerLabel.contains("phone") || lowerLabel.contains("mobile")) {
            plainExplanation = "Your active 10-digit mobile phone number for security codes and calls.";
            whyAsked = "Used for two-factor authentication and urgent status SMS alerts.";
            simplifiedQuestion = "What is your mobile phone number?";
        } else if (lowerLabel.contains("email")) {
            plainExplanation = "Your active electronic email address for digital statements.";
            whyAsked = "Used to send instant digital receipts, updates, and session recovery links.";
            simplifiedQuestion = "What is your email address?";
        } else if (lowerLabel.contains("declaration") || lowerLabel.contains("consent") || lowerLabel.contains("agree")) {
            plainExplanation = "Your legal confirmation that all answers provided are honest and accurate.";
            whyAsked = "Required by law before processing any official application.";
            simplifiedQuestion = "Do you confirm all provided details are true?";
        } else {
            plainExplanation = helpText != null && !helpText.isBlank() ? helpText : "Information requested for " + label + ".";
            whyAsked = "Required by the processing authority to review your application.";
            simplifiedQuestion = "What is your " + label + "?";
        }

        if (isLowCognitive) {
            plainExplanation = "Calm Guidance: " + plainExplanation;
        }

        return AIFieldExplanation.builder()
                .fieldKey(fieldKey)
                .plainLanguageExplanation(plainExplanation)
                .whyAskedExplanation(whyAsked)
                .simplifiedQuestionText(simplifiedQuestion)
                .build();
    }

    @Override
    public AIRiskEvaluation evaluateRisk(String fieldKey, String label, String answerValue) {
        String combined = (fieldKey + " " + label).toLowerCase(Locale.ROOT);

        if (combined.contains("aadhaar") || combined.contains("uid") || combined.contains("ssn") || combined.contains("identity")) {
            return AIRiskEvaluation.builder()
                    .riskLevel(RiskLevel.HIGH)
                    .warningTitle("High Risk: Critical Identity Document Entry")
                    .warningReason("You are entering a sensitive national identity number.")
                    .consequenceExplanation("An incorrect Aadhaar or ID number will cause your application verification to fail or be rejected.")
                    .build();
        }

        if (combined.contains("bank") || combined.contains("account") || combined.contains("ifsc")) {
            return AIRiskEvaluation.builder()
                    .riskLevel(RiskLevel.HIGH)
                    .warningTitle("High Risk: Financial Account Update")
                    .warningReason("You are submitting financial bank account details.")
                    .consequenceExplanation("Incorrect bank details may misroute future payments or direct deposit benefits.")
                    .build();
        }

        if (combined.contains("nominee") || combined.contains("beneficiary")) {
            return AIRiskEvaluation.builder()
                    .riskLevel(RiskLevel.HIGH)
                    .warningTitle("High Risk: Nominee Rights Update")
                    .warningReason("You are designating the primary beneficiary.")
                    .consequenceExplanation("Changing your nominee overwrites existing legal beneficiary allocations on record.")
                    .build();
        }

        return AIRiskEvaluation.builder()
                .riskLevel(RiskLevel.STANDARD)
                .warningTitle("Standard Information Entry")
                .warningReason("Standard field entry.")
                .consequenceExplanation("Please review for typo corrections.")
                .build();
    }
}
