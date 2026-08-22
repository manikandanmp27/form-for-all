package com.fillforme.backend.conversation.dto;

import com.fillforme.backend.form.dto.FormFieldDto;
import com.fillforme.backend.risk.dto.RiskFlagDto;

import java.util.UUID;

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

    public ConversationStepResponse() {}

    public ConversationStepResponse(UUID sessionId, int currentStep, int totalSteps, boolean isCompleted, FormFieldDto currentField, FormFieldDto previousField, FormFieldDto nextField, boolean riskConfirmationRequired, RiskFlagDto pendingRiskFlag, String progressPercentage) {
        this.sessionId = sessionId;
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
        this.isCompleted = isCompleted;
        this.currentField = currentField;
        this.previousField = previousField;
        this.nextField = nextField;
        this.riskConfirmationRequired = riskConfirmationRequired;
        this.pendingRiskFlag = pendingRiskFlag;
        this.progressPercentage = progressPercentage;
    }

    public static ConversationStepResponseBuilder builder() {
        return new ConversationStepResponseBuilder();
    }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public int getCurrentStep() { return currentStep; }
    public void setCurrentStep(int currentStep) { this.currentStep = currentStep; }

    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean isCompleted) { this.isCompleted = isCompleted; }

    public FormFieldDto getCurrentField() { return currentField; }
    public void setCurrentField(FormFieldDto currentField) { this.currentField = currentField; }

    public FormFieldDto getPreviousField() { return previousField; }
    public void setPreviousField(FormFieldDto previousField) { this.previousField = previousField; }

    public FormFieldDto getNextField() { return nextField; }
    public void setNextField(FormFieldDto nextField) { this.nextField = nextField; }

    public boolean isRiskConfirmationRequired() { return riskConfirmationRequired; }
    public void setRiskConfirmationRequired(boolean riskConfirmationRequired) { this.riskConfirmationRequired = riskConfirmationRequired; }

    public RiskFlagDto getPendingRiskFlag() { return pendingRiskFlag; }
    public void setPendingRiskFlag(RiskFlagDto pendingRiskFlag) { this.pendingRiskFlag = pendingRiskFlag; }

    public String getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(String progressPercentage) { this.progressPercentage = progressPercentage; }

    public static class ConversationStepResponseBuilder {
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

        ConversationStepResponseBuilder() {}

        public ConversationStepResponseBuilder sessionId(UUID sessionId) { this.sessionId = sessionId; return this; }
        public ConversationStepResponseBuilder currentStep(int currentStep) { this.currentStep = currentStep; return this; }
        public ConversationStepResponseBuilder totalSteps(int totalSteps) { this.totalSteps = totalSteps; return this; }
        public ConversationStepResponseBuilder isCompleted(boolean isCompleted) { this.isCompleted = isCompleted; return this; }
        public ConversationStepResponseBuilder currentField(FormFieldDto currentField) { this.currentField = currentField; return this; }
        public ConversationStepResponseBuilder previousField(FormFieldDto previousField) { this.previousField = previousField; return this; }
        public ConversationStepResponseBuilder nextField(FormFieldDto nextField) { this.nextField = nextField; return this; }
        public ConversationStepResponseBuilder riskConfirmationRequired(boolean riskConfirmationRequired) { this.riskConfirmationRequired = riskConfirmationRequired; return this; }
        public ConversationStepResponseBuilder pendingRiskFlag(RiskFlagDto pendingRiskFlag) { this.pendingRiskFlag = pendingRiskFlag; return this; }
        public ConversationStepResponseBuilder progressPercentage(String progressPercentage) { this.progressPercentage = progressPercentage; return this; }

        public ConversationStepResponse build() {
            return new ConversationStepResponse(sessionId, currentStep, totalSteps, isCompleted, currentField, previousField, nextField, riskConfirmationRequired, pendingRiskFlag, progressPercentage);
        }
    }
}
