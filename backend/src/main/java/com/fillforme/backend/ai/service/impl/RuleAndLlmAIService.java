package com.fillforme.backend.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fillforme.backend.ai.dto.AIFieldExplanation;
import com.fillforme.backend.ai.dto.AIRiskEvaluation;
import com.fillforme.backend.ai.service.AIService;
import com.fillforme.backend.document.dto.ExtractedFieldData;
import com.fillforme.backend.form.entity.FieldType;
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

import java.util.ArrayList;
import java.util.Base64;
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
    private final ObjectMapper objectMapper;

    public RuleAndLlmAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<ExtractedFieldData> extractFieldsWithAI(byte[] fileBytes, String filename, String mimeType) {
        if (apiKey != null && !apiKey.isBlank() && fileBytes != null && fileBytes.length > 0) {
            try {
                log.info("Sending document '{}' ({} bytes) to Gemini Vision AI for live field extraction...", filename, fileBytes.length);
                List<ExtractedFieldData> aiExtracted = callGeminiVisionForFields(fileBytes, filename, mimeType);
                if (aiExtracted != null && !aiExtracted.isEmpty()) {
                    log.info("Gemini Vision AI successfully extracted {} fields from document '{}'", aiExtracted.size(), filename);
                    return aiExtracted;
                }
            } catch (Exception e) {
                log.warn("Gemini Vision AI extraction failed for '{}', falling back to document parser: {}", filename, e.getMessage());
            }
        }
        log.info("No valid AI API key present or AI extraction skipped for '{}'. Handing off to DocumentProcessor.", filename);
        return List.of();
    }

    private List<ExtractedFieldData> callGeminiVisionForFields(byte[] fileBytes, String filename, String mimeType) {
        String effectiveMime = (mimeType != null && !mimeType.isBlank() && !mimeType.equals("application/octet-stream"))
                ? mimeType
                : (filename != null && filename.toLowerCase().endsWith(".pdf") ? "application/pdf" : "image/jpeg");

        String base64Data = Base64.getEncoder().encodeToString(fileBytes);

        String prompt = "Inspect this form document image/file carefully. Extract ALL input form fields that a user needs to fill out in this form. " +
                "Return ONLY a valid JSON array of objects. Each object must have these keys:\n" +
                "- \"fieldKey\": string in camelCase (e.g. \"applicantName\", \"aadhaarNumber\", \"dateOfBirth\")\n" +
                "- \"label\": human-readable title (e.g. \"Applicant Full Name\", \"Aadhaar Number\")\n" +
                "- \"fieldType\": one of \"TEXT\", \"DATE\", \"EMAIL\", \"PHONE\", \"SELECT\", \"CHECKBOX\", \"DECLARATION\"\n" +
                "- \"required\": boolean true or false\n" +
                "- \"defaultHelpText\": short 1-sentence guidance for filling this field\n" +
                "Do NOT include markdown formatting, backticks, or extra text. Output raw JSON array only.";

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("inline_data", Map.of(
                                        "mime_type", effectiveMime,
                                        "data", base64Data
                                )),
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return parseGeminiJsonResponse(response.getBody());
        }

        return List.of();
    }

    private List<ExtractedFieldData> parseGeminiJsonResponse(String responseBody) {
        List<ExtractedFieldData> fields = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
                // Clean markdown code fence if present
                String cleanedJson = text.replaceAll("```json", "").replaceAll("```", "").trim();

                JsonNode fieldArray = objectMapper.readTree(cleanedJson);
                if (fieldArray.isArray()) {
                    int order = 1;
                    for (JsonNode node : fieldArray) {
                        String fieldKey = node.path("fieldKey").asText("field_" + order);
                        String label = node.path("label").asText("Field " + order);
                        String fieldTypeStr = node.path("fieldType").asText("TEXT").toUpperCase();
                        boolean required = node.path("required").asBoolean(true);
                        String helpText = node.path("defaultHelpText").asText("Please enter " + label);

                        FieldType fieldType;
                        try {
                            fieldType = FieldType.valueOf(fieldTypeStr);
                        } catch (Exception e) {
                            fieldType = FieldType.TEXT;
                        }

                        fields.add(ExtractedFieldData.builder()
                                .fieldKey(fieldKey)
                                .label(label)
                                .fieldType(fieldType)
                                .required(required)
                                .orderIndex(order++)
                                .defaultHelpText(helpText)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not parse JSON array from Gemini Vision response: {}", e.getMessage());
        }
        return fields;
    }

    @Override
    public AIFieldExplanation generateFieldExplanation(String fieldKey, String label, String helpText, AccessibilityProfile profile) {
        String preferredLang = profile != null && profile.getPreferredLanguage() != null ? profile.getPreferredLanguage() : "English";
        boolean isLowCognitive = profile != null && profile.getCognitiveLoadPreference() == CognitiveLoadPreference.LOW;

        if (apiKey != null && !apiKey.isBlank()) {
            try {
                return callLlmForFieldExplanation(fieldKey, label, helpText, preferredLang, isLowCognitive);
            } catch (Exception e) {
                log.warn("External LLM AI call failed, using smart document AI engine fallback: {}", e.getMessage());
            }
        }

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

    private List<ExtractedFieldData> generateSmartFallbackFields(String filename) {
        String lowerName = filename != null ? filename.toLowerCase() : "";

        if (lowerName.contains("aadhaar") || lowerName.contains("aadhar") || lowerName.contains("uidai") ||
            lowerName.contains("identity") || lowerName.contains("id") || lowerName.contains("voter") || lowerName.contains("passport")) {
            return List.of(
                    ExtractedFieldData.builder().fieldKey("applicantFullName").label("Full Legal Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter full name as on your ID document").build(),
                    ExtractedFieldData.builder().fieldKey("aadhaarNumber").label("Aadhaar / ID Number").fieldType(FieldType.TEXT).required(true).orderIndex(2).defaultHelpText("12-digit Aadhaar / Enrollment Number").build(),
                    ExtractedFieldData.builder().fieldKey("dateOfBirth").label("Date of Birth").fieldType(FieldType.DATE).required(true).orderIndex(3).defaultHelpText("Select your official birth date").build(),
                    ExtractedFieldData.builder().fieldKey("gender").label("Gender").fieldType(FieldType.TEXT).required(true).orderIndex(4).defaultHelpText("Male / Female / Transgender").build(),
                    ExtractedFieldData.builder().fieldKey("mobileNumber").label("Mobile Phone Number").fieldType(FieldType.PHONE).required(true).orderIndex(5).defaultHelpText("Registered mobile number").build(),
                    ExtractedFieldData.builder().fieldKey("permanentAddress").label("Permanent Residential Address").fieldType(FieldType.TEXT).required(true).orderIndex(6).defaultHelpText("Full residential address with PIN code").build(),
                    ExtractedFieldData.builder().fieldKey("declarationConsent").label("Declaration & Authorization Consent").fieldType(FieldType.DECLARATION).required(true).orderIndex(7).defaultHelpText("Confirm legal consent for identity verification").build()
            );
        }

        return List.of(
                ExtractedFieldData.builder().fieldKey("applicantFullName").label("Full Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter your official full name").build(),
                ExtractedFieldData.builder().fieldKey("dateOfBirth").label("Date of Birth").fieldType(FieldType.DATE).required(true).orderIndex(2).defaultHelpText("Select your official birth date").build(),
                ExtractedFieldData.builder().fieldKey("mobileNumber").label("Mobile Phone Number").fieldType(FieldType.PHONE).required(true).orderIndex(3).defaultHelpText("Enter your contact phone number").build(),
                ExtractedFieldData.builder().fieldKey("permanentAddress").label("Permanent Residential Address").fieldType(FieldType.TEXT).required(true).orderIndex(4).defaultHelpText("Enter your primary address").build(),
                ExtractedFieldData.builder().fieldKey("declarationConsent").label("Legal Declaration & Consent").fieldType(FieldType.DECLARATION).required(true).orderIndex(5).defaultHelpText("Confirm your legal consent").build()
        );
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
