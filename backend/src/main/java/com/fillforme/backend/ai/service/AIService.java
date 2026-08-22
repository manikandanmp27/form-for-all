package com.fillforme.backend.ai.service;

import com.fillforme.backend.ai.dto.AIFieldExplanation;
import com.fillforme.backend.ai.dto.AIRiskEvaluation;
import com.fillforme.backend.document.dto.ExtractedFieldData;
import com.fillforme.backend.profile.entity.AccessibilityProfile;

import com.fillforme.backend.ai.dto.TextRegionData;

import java.util.List;

public interface AIService {
    AIFieldExplanation generateFieldExplanation(String fieldKey, String label, String helpText, AccessibilityProfile profile);
    AIRiskEvaluation evaluateRisk(String fieldKey, String label, String answerValue);
    List<ExtractedFieldData> extractFieldsWithAI(byte[] fileBytes, String filename, String mimeType);
    List<TextRegionData> translateDocumentWithVisionAI(byte[] fileBytes, String filename, String mimeType, String targetLanguage);
}

