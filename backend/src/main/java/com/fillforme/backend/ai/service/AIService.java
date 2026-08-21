package com.fillforme.backend.ai.service;

import com.fillforme.backend.ai.dto.AIFieldExplanation;
import com.fillforme.backend.ai.dto.AIRiskEvaluation;
import com.fillforme.backend.profile.entity.AccessibilityProfile;

public interface AIService {
    AIFieldExplanation generateFieldExplanation(String fieldKey, String label, String helpText, AccessibilityProfile profile);
    AIRiskEvaluation evaluateRisk(String fieldKey, String label, String answerValue);
}
