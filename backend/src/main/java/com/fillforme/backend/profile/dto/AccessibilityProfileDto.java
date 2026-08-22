package com.fillforme.backend.profile.dto;

import com.fillforme.backend.profile.entity.AccessibilityNeed;
import com.fillforme.backend.profile.entity.CognitiveLoadPreference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AccessibilityProfileDto {
    private UUID id;

    @NotBlank(message = "Preferred language is required")
    private String preferredLanguage;

    @NotNull(message = "Voice preference must be specified")
    private Boolean voicePreference;

    @NotNull(message = "Cognitive load preference must be specified")
    private CognitiveLoadPreference cognitiveLoadPreference;

    @NotNull(message = "Accessibility need must be specified")
    private AccessibilityNeed accessibilityNeed;

    public AccessibilityProfileDto() {}

    public AccessibilityProfileDto(UUID id, String preferredLanguage, Boolean voicePreference, CognitiveLoadPreference cognitiveLoadPreference, AccessibilityNeed accessibilityNeed) {
        this.id = id;
        this.preferredLanguage = preferredLanguage;
        this.voicePreference = voicePreference;
        this.cognitiveLoadPreference = cognitiveLoadPreference;
        this.accessibilityNeed = accessibilityNeed;
    }

    public static AccessibilityProfileDtoBuilder builder() {
        return new AccessibilityProfileDtoBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public Boolean getVoicePreference() { return voicePreference; }
    public void setVoicePreference(Boolean voicePreference) { this.voicePreference = voicePreference; }

    public CognitiveLoadPreference getCognitiveLoadPreference() { return cognitiveLoadPreference; }
    public void setCognitiveLoadPreference(CognitiveLoadPreference cognitiveLoadPreference) { this.cognitiveLoadPreference = cognitiveLoadPreference; }

    public AccessibilityNeed getAccessibilityNeed() { return accessibilityNeed; }
    public void setAccessibilityNeed(AccessibilityNeed accessibilityNeed) { this.accessibilityNeed = accessibilityNeed; }

    public static class AccessibilityProfileDtoBuilder {
        private UUID id;
        private String preferredLanguage;
        private Boolean voicePreference;
        private CognitiveLoadPreference cognitiveLoadPreference;
        private AccessibilityNeed accessibilityNeed;

        AccessibilityProfileDtoBuilder() {}

        public AccessibilityProfileDtoBuilder id(UUID id) { this.id = id; return this; }
        public AccessibilityProfileDtoBuilder preferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; return this; }
        public AccessibilityProfileDtoBuilder voicePreference(Boolean voicePreference) { this.voicePreference = voicePreference; return this; }
        public AccessibilityProfileDtoBuilder cognitiveLoadPreference(CognitiveLoadPreference cognitiveLoadPreference) { this.cognitiveLoadPreference = cognitiveLoadPreference; return this; }
        public AccessibilityProfileDtoBuilder accessibilityNeed(AccessibilityNeed accessibilityNeed) { this.accessibilityNeed = accessibilityNeed; return this; }

        public AccessibilityProfileDto build() {
            return new AccessibilityProfileDto(id, preferredLanguage, voicePreference, cognitiveLoadPreference, accessibilityNeed);
        }
    }
}
