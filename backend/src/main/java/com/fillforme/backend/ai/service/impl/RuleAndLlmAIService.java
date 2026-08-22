
package com.fillforme.backend.ai.service.impl;

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

import com.fillforme.backend.ai.dto.TextRegionData;

@Service
public class RuleAndLlmAIService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(RuleAndLlmAIService.class);

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.provider:gemini}") // "gemini" or "openai"
    private String aiProvider;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Supported active Gemini models in order of priority
    private static final List<String> GEMINI_MODELS = List.of(
            "gemini-2.5-flash",
            "gemini-1.5-flash",
            "gemini-1.5-pro",
            "gemini-2.0-flash-exp"
    );

    public RuleAndLlmAIService() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60000);
        factory.setReadTimeout(120000);
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<TextRegionData> translateDocumentWithVisionAI(byte[] fileBytes, String filename, String mimeType, String targetLanguage) {
        if (apiKey != null && !apiKey.isBlank() && fileBytes != null && fileBytes.length > 0) {
            log.info("Sending document '{}' to Gemini Vision AI for line-by-line translation to {}...", filename, targetLanguage);
            return callGeminiVisionForTranslation(fileBytes, filename, mimeType, targetLanguage);
        }
        log.warn("No Gemini API key available for vision translation of '{}'", filename);
        return List.of();
    }

    private List<TextRegionData> callGeminiVisionForTranslation(byte[] fileBytes, String filename, String mimeType, String targetLanguage) {
        String effectiveMime = (mimeType != null && !mimeType.isBlank() && !mimeType.equals("application/octet-stream"))
                ? mimeType
                : (filename != null && filename.toLowerCase().endsWith(".pdf") ? "application/pdf" : "image/jpeg");

        String base64Data = Base64.getEncoder().encodeToString(fileBytes);

        String prompt = String.format(
                "You are a Google Lens image translator. Read ALL text printed or written on this document image (including regional Indian languages like Kannada, Hindi, Tamil, Telugu, Marathi, Bengali, Gujarati, Punjabi, English).\n" +
                "Translate every single text line, heading, instruction, and field label to target language: \"%s\".\n" +
                "Return ONLY a valid JSON array of objects. Do not include markdown code block formatting.\n" +
                "Each object MUST have these exact keys:\n" +
                "- \"originalText\": string (original text found on document)\n" +
                "- \"translatedText\": string (translated text in %s)\n" +
                "- \"xPercent\": number (left edge percentage from 0.0 to 100.0)\n" +
                "- \"yPercent\": number (top edge percentage from 0.0 to 100.0)\n" +
                "- \"widthPercent\": number (box width percentage from 1.0 to 100.0)\n" +
                "- \"heightPercent\": number (box height percentage from 1.0 to 100.0)",
                targetLanguage, targetLanguage
        );

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

        for (String model : GEMINI_MODELS) {
            try {
                String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<TextRegionData> regions = parseGeminiVisionTranslationResponse(response.getBody());
                    if (!regions.isEmpty()) {
                        log.info("Gemini Vision AI successfully translated {} text regions for document '{}'", regions.size(), filename);
                        return regions;
                    }
                }
            } catch (Exception e) {
                log.debug("Gemini model {} failed for vision translation: {}", model, e.getMessage());
            }
        }
        return List.of();
    }

    private List<TextRegionData> parseGeminiVisionTranslationResponse(String responseBody) {
        List<TextRegionData> list = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
                String cleanedJson = text.replaceAll("```json", "").replaceAll("```", "").trim();

                JsonNode array = objectMapper.readTree(cleanedJson);
                if (array.isArray()) {
                    for (JsonNode node : array) {
                        String orig = node.path("originalText").asText("");
                        String trans = node.path("translatedText").asText("");
                        double x = node.path("xPercent").asDouble(0);
                        double y = node.path("yPercent").asDouble(0);
                        double w = node.path("widthPercent").asDouble(20);
                        double h = node.path("heightPercent").asDouble(4);

                        if (!trans.isBlank()) {
                            list.add(new TextRegionData(orig, trans, x, y, w, h));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse Gemini Vision translation JSON: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<ExtractedFieldData> extractFieldsWithAI(byte[] fileBytes, String filename, String mimeType) {
        if (apiKey != null && !apiKey.isBlank() && fileBytes != null && fileBytes.length > 0) {
            log.info("Sending document '{}' ({} bytes) to Gemini Vision AI for live field extraction...", filename, fileBytes.length);
            List<ExtractedFieldData> aiExtracted = callGeminiVisionForFields(fileBytes, filename, mimeType);
            if (aiExtracted != null && !aiExtracted.isEmpty()) {
                log.info("Gemini Vision AI successfully extracted {} fields from document '{}'", aiExtracted.size(), filename);
                return aiExtracted;
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

        String prompt = "Inspect this document image/file carefully. Extract ALL field labels AND their actual values printed/written on the document.\n" +
                "Return ONLY a valid JSON array of objects. Each object MUST have these exact keys:\n" +
                "- \"fieldKey\": string in camelCase (e.g. \"applicantName\", \"fatherName\", \"dateOfBirth\", \"address\", \"aadhaarNumber\")\n" +
                "- \"label\": human-readable title (e.g. \"Full Name\", \"Father's Name\", \"Date of Birth\", \"Address\")\n" +
                "- \"value\": string (the ACTUAL text value printed/written on the document image, e.g. \"John Doe\", \"15/08/1995\", \"9876543210\")\n" +
                "- \"fieldType\": one of \"TEXT\", \"DATE\", \"EMAIL\", \"PHONE\", \"SELECT\", \"CHECKBOX\"\n" +
                "- \"required\": boolean true or false\n" +
                "- \"defaultHelpText\": short guidance text\n" +
                "Do NOT include markdown formatting, backticks, or extra text. Output raw JSON array only.";

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

        for (String model : GEMINI_MODELS) {
            try {
                String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<ExtractedFieldData> fields = parseGeminiJsonResponse(response.getBody());
                    if (!fields.isEmpty()) {
                        return fields;
                    }
                }
            } catch (Exception e) {
                log.debug("Gemini model {} failed for vision extraction: {}", model, e.getMessage());
            }
        }

        log.warn("Gemini Vision AI calls failed for '{}', using DocumentProcessor fallback.", filename);
        return List.of();
    }

    private List<ExtractedFieldData> parseGeminiJsonResponse(String responseBody) {
        List<ExtractedFieldData> fields = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
                String cleanedJson = text.replaceAll("```json", "").replaceAll("```", "").trim();

                JsonNode fieldArray = objectMapper.readTree(cleanedJson);
                if (fieldArray.isArray()) {
                    int order = 1;
                    for (JsonNode node : fieldArray) {
                        String fieldKey = node.path("fieldKey").asText("field_" + order);
                        String label = node.path("label").asText("Field " + order);
                        String extractedVal = node.path("value").asText("");
                        if (extractedVal.isBlank()) {
                            extractedVal = node.path("extractedValue").asText("");
                        }
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
                                .extractedValue(extractedVal)
                                .fieldType(fieldType)
                                .required(required)
                                .orderIndex(order++)
                                .defaultHelpText(helpText)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse Gemini Vision JSON response: {}", e.getMessage());
        }
        return fields;
    }

    @Override
    public AIFieldExplanation generateFieldExplanation(String fieldKey, String label, String helpText, AccessibilityProfile profile) {
        String preferredLang = profile != null && profile.getPreferredLanguage() != null ? profile.getPreferredLanguage() : "English";
        boolean isLowCognitive = profile != null && profile.getCognitiveLoadPreference() == CognitiveLoadPreference.LOW;

        return generateSmartFallbackExplanation(fieldKey, label, helpText, preferredLang, isLowCognitive);
    }

    private AIFieldExplanation generateSmartFallbackExplanation(String fieldKey, String label, String helpText, String lang, boolean isLowCognitive) {
        String lowerLabel = (label != null ? label : fieldKey).toLowerCase(Locale.ROOT);
        String simplified;
        String explanation;
        String whyAsked;

        if (lowerLabel.contains("name") || lowerLabel.contains("applicant") || lowerLabel.contains("taxpayer") || lowerLabel.contains("patient")) {
            simplified = isLowCognitive ? "What is your full legal name?" : "Enter your full legal name";
            explanation = "This is your official name as shown on government documents.";
            whyAsked = "Required to verify your legal identity and assign this form to you.";
        } else if (lowerLabel.contains("dob") || lowerLabel.contains("birth")) {
            simplified = isLowCognitive ? "When were you born?" : "Select your date of birth";
            explanation = "Your official date of birth.";
            whyAsked = "Required to verify legal age eligibility.";
        } else if (lowerLabel.contains("aadhaar") || lowerLabel.contains("id") || lowerLabel.contains("pan") || lowerLabel.contains("ssn")) {
            simplified = isLowCognitive ? "What is your ID number?" : "Enter your official ID number";
            explanation = "Your official identification card number.";
            whyAsked = "Required for official identification and background verification.";
        } else if (lowerLabel.contains("address") || lowerLabel.contains("residence") || lowerLabel.contains("branch")) {
            simplified = isLowCognitive ? "Where do you live?" : "Enter your permanent address";
            explanation = "Your current residential street address.";
            whyAsked = "Required for official physical correspondence and location verification.";
        } else if (lowerLabel.contains("email")) {
            simplified = isLowCognitive ? "What is your email?" : "Enter your email address";
            explanation = "Your primary electronic mail address.";
            whyAsked = "Used to send instant confirmation receipts and form updates.";
        } else if (lowerLabel.contains("phone") || lowerLabel.contains("mobile") || lowerLabel.contains("contact")) {
            simplified = isLowCognitive ? "What is your phone number?" : "Enter your mobile phone number";
            explanation = "Your active telephone or mobile number.";
            whyAsked = "Used for SMS alerts and urgent status updates.";
        } else if (lowerLabel.contains("account") || lowerLabel.contains("bank") || lowerLabel.contains("ifsc")) {
            simplified = isLowCognitive ? "What are your bank details?" : "Enter your bank account number";
            explanation = "Your official bank account and branch details.";
            whyAsked = "Required for direct financial processing or refunds.";
        } else if (lowerLabel.contains("consent") || lowerLabel.contains("declaration") || lowerLabel.contains("agree")) {
            simplified = isLowCognitive ? "Do you confirm these details?" : "Confirm legal declaration";
            explanation = "A legal statement confirming that all information provided is accurate.";
            whyAsked = "Required by law to authorize processing of your application.";
        } else {
            simplified = isLowCognitive ? "Please provide: " + label : "Enter details for " + label;
            explanation = helpText != null && !helpText.isBlank() ? helpText : "Provide the requested details for this field.";
            whyAsked = "Required by the form issuer to complete your application.";
        }

        return AIFieldExplanation.builder()
                .simplifiedQuestionText(simplified)
                .plainLanguageExplanation(explanation)
                .whyAskedExplanation(whyAsked)
                .build();
    }

    @Override
    public AIRiskEvaluation evaluateRisk(String fieldKey, String label, String answerValue) {
        String lowerLabel = (label != null ? label : fieldKey).toLowerCase(Locale.ROOT);

        if (lowerLabel.contains("nominee") || lowerLabel.contains("beneficiary") || lowerLabel.contains("transfer")) {
            return AIRiskEvaluation.builder()
                    .riskLevel(RiskLevel.HIGH)
                    .warningTitle("Beneficiary Change Alert")
                    .warningReason("You are designating a legal beneficiary or nominee.")
                    .consequenceExplanation("This choice legally entitles the person named to inherit or receive account assets upon claim.")
                    .build();
        }

        if (lowerLabel.contains("pan") || lowerLabel.contains("aadhaar") || lowerLabel.contains("bank") || lowerLabel.contains("account")) {
            return AIRiskEvaluation.builder()
                    .riskLevel(RiskLevel.STANDARD)
                    .warningTitle("Sensitive Financial/ID Field")
                    .warningReason("You are sharing sensitive personal identifier details.")
                    .consequenceExplanation("Ensure the issuer website is authentic before final submission.")
                    .build();
        }

        return AIRiskEvaluation.builder()
                .riskLevel(RiskLevel.INFORMATIONAL)
                .warningTitle("Standard Information")
                .warningReason("Standard low-risk form field.")
                .consequenceExplanation("No significant legal or financial risk detected.")
                .build();
    }
}
