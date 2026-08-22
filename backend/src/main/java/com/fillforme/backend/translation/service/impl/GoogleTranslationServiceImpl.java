package com.fillforme.backend.translation.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fillforme.backend.translation.dto.LanguageDto;
import com.fillforme.backend.translation.service.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleTranslationServiceImpl implements TranslationService {

    private static final Logger log = LoggerFactory.getLogger(GoogleTranslationServiceImpl.class);

    @Value("${app.ai.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final List<LanguageDto> SUPPORTED_LANGUAGES = List.of(
            new LanguageDto("kn", "Kannada", "ಕನ್ನಡ"),
            new LanguageDto("hi", "Hindi", "हिन्दी"),
            new LanguageDto("ta", "Tamil", "தமிழ்"),
            new LanguageDto("te", "Telugu", "తెలుగు"),
            new LanguageDto("ml", "Malayalam", "മലയാളം"),
            new LanguageDto("mr", "Marathi", "मराठी"),
            new LanguageDto("bn", "Bengali", "বাংলা"),
            new LanguageDto("gu", "Gujarati", "ગુજરાતી"),
            new LanguageDto("pa", "Punjabi", "ਪੰਜਾਬੀ"),
            new LanguageDto("en", "English", "English")
    );

    // Fallback dictionary mappings for offline testing
    private static final Map<String, Map<String, String>> DICTIONARY = new HashMap<>();

    static {
        Map<String, String> toKannada = Map.of(
                "Full Name", "ಪೂರ್ಣ ಹೆಸರು",
                "Date of Birth", "ಹುಟ್ಟಿದ ದಿನಾಂಕ",
                "Gender", "ಲಿಂಗ",
                "Phone Number", "ದೂರವಾಣಿ ಸಂಖ್ಯೆ",
                "Address", "ವಿಳಾಸ",
                "Aadhaar Number", "ಆಧಾರ್ ಸಂಖ್ಯೆ",
                "Bank Account Number", "ಬ್ಯಾಂಕ್ ಖಾತೆ ಸಂಖ್ಯೆ",
                "Nominee Name", "ನಾಮಿನಿ ಹೆಸರು"
        );
        Map<String, String> toHindi = Map.of(
                "Full Name", "पूरा नाम",
                "Date of Birth", "जन्म तिथि",
                "Gender", "लिंग",
                "Phone Number", "फ़ोन नंबर",
                "Address", "पता",
                "Aadhaar Number", "आधार संख्या",
                "Bank Account Number", "बैंक खाता संख्या",
                "Nominee Name", "नॉमिनी का नाम"
        );
        Map<String, String> toTamil = Map.of(
                "Full Name", "முழு பெயர்",
                "Date of Birth", "பிறந்த தேதி",
                "Gender", "பாலினம்",
                "Phone Number", "தொலைபேசி எண்",
                "Address", "முகவரி",
                "Aadhaar Number", "ஆதார் எண்",
                "Bank Account Number", "வங்கி கணக்கு எண்",
                "Nominee Name", "நாமினி பெயர்"
        );
        DICTIONARY.put("kn", toKannada);
        DICTIONARY.put("hi", toHindi);
        DICTIONARY.put("ta", toTamil);
    }

    public GoogleTranslationServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        if (text == null || text.isBlank()) return text;
        if (targetLanguage == null || targetLanguage.isBlank()) targetLanguage = "en";
        if (targetLanguage.equalsIgnoreCase(sourceLanguage)) return text;

        // Try Google Cloud Translation REST API
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String url = "https://translation.googleapis.com/language/translate/v2?key=" + apiKey;
                Map<String, Object> body = new HashMap<>();
                body.put("q", text);
                body.put("target", targetLanguage);
                if (sourceLanguage != null && !sourceLanguage.isBlank() && !"auto".equalsIgnoreCase(sourceLanguage)) {
                    body.put("source", sourceLanguage);
                }

                ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode translatedNode = root.path("data").path("translations").get(0).path("translatedText");
                    if (!translatedNode.isMissingNode()) {
                        return translatedNode.asText();
                    }
                }
            } catch (Exception e) {
                log.warn("Google Translation API call failed, falling back to local translation dictionary: {}", e.getMessage());
            }
        }

        // Fallback dictionary translation
        Map<String, String> langDict = DICTIONARY.get(targetLanguage.toLowerCase());
        if (langDict != null) {
            for (Map.Entry<String, String> entry : langDict.entrySet()) {
                if (text.equalsIgnoreCase(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return text + " (" + targetLanguage.toUpperCase() + ")";
    }

    @Override
    public String detectLanguage(String text) {
        if (text == null || text.isBlank()) return "en";
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String url = "https://translation.googleapis.com/language/translate/v2/detect?key=" + apiKey;
                Map<String, Object> body = Map.of("q", text);
                ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode langNode = root.path("data").path("detections").get(0).get(0).path("language");
                    if (!langNode.isMissingNode()) {
                        return langNode.asText();
                    }
                }
            } catch (Exception e) {
                log.warn("Language detection failed: {}", e.getMessage());
            }
        }
        return "en";
    }

    @Override
    public List<LanguageDto> getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }
}
