package com.fillforme.backend.profile.dto;

import com.fillforme.backend.profile.entity.AccessibilityNeed;
import com.fillforme.backend.profile.entity.CognitiveLoadPreference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
