package com.fillforme.backend.conversation.dto;

import com.fillforme.backend.form.dto.FormFieldDto;
import com.fillforme.backend.risk.dto.RiskFlagDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationStepResponse {
    private UUID sessionId;
    private int currentStep;
    private int totalSteps;
    private boolean isCompleted;
    private FormFieldDto currentField;
    private FormFieldDto previousField;
    private FormFieldDto nextField;
    private boolean riskConfirmationRequired;
    private RiskFlagDto pendingRiskFlag;
    private String progressPercentage;
}
