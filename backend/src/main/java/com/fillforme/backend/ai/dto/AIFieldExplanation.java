package com.fillforme.backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIFieldExplanation {
    private String fieldKey;
    private String plainLanguageExplanation;
    private String whyAskedExplanation;
    private String simplifiedQuestionText;
}
