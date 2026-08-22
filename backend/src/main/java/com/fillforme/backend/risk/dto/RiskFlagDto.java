package com.fillforme.backend.risk.dto;

import com.fillforme.backend.risk.entity.ConfirmationStatus;
import com.fillforme.backend.risk.entity.RiskLevel;

import java.time.LocalDateTime;
import java.util.UUID;

public class RiskFlagDto {
    private UUID id;
    private UUID sessionId;
    private UUID fieldId;
    private String fieldLabel;
    private RiskLevel riskLevel;
    private String warningTitle;
    private String warningReason;
    private String consequenceExplanation;
    private ConfirmationStatus confirmationStatus;
    private LocalDateTime confirmedAt;

    public RiskFlagDto() {}

    public RiskFlagDto(UUID id, UUID sessionId, UUID fieldId, String fieldLabel, RiskLevel riskLevel, String warningTitle, String warningReason, String consequenceExplanation, ConfirmationStatus confirmationStatus, LocalDateTime confirmedAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.fieldId = fieldId;
        this.fieldLabel = fieldLabel;
        this.riskLevel = riskLevel;
        this.warningTitle = warningTitle;
        this.warningReason = warningReason;
        this.consequenceExplanation = consequenceExplanation;
        this.confirmationStatus = confirmationStatus;
        this.confirmedAt = confirmedAt;
    }

    public static RiskFlagDtoBuilder builder() {
        return new RiskFlagDtoBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public UUID getFieldId() { return fieldId; }
    public void setFieldId(UUID fieldId) { this.fieldId = fieldId; }

    public String getFieldLabel() { return fieldLabel; }
    public void setFieldLabel(String fieldLabel) { this.fieldLabel = fieldLabel; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getWarningTitle() { return warningTitle; }
    public void setWarningTitle(String warningTitle) { this.warningTitle = warningTitle; }

    public String getWarningReason() { return warningReason; }
    public void setWarningReason(String warningReason) { this.warningReason = warningReason; }

    public String getConsequenceExplanation() { return consequenceExplanation; }
    public void setConsequenceExplanation(String consequenceExplanation) { this.consequenceExplanation = consequenceExplanation; }

    public ConfirmationStatus getConfirmationStatus() { return confirmationStatus; }
    public void setConfirmationStatus(ConfirmationStatus confirmationStatus) { this.confirmationStatus = confirmationStatus; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public static class RiskFlagDtoBuilder {
        private UUID id;
        private UUID sessionId;
        private UUID fieldId;
        private String fieldLabel;
        private RiskLevel riskLevel;
        private String warningTitle;
        private String warningReason;
        private String consequenceExplanation;
        private ConfirmationStatus confirmationStatus;
        private LocalDateTime confirmedAt;

        RiskFlagDtoBuilder() {}

        public RiskFlagDtoBuilder id(UUID id) { this.id = id; return this; }
        public RiskFlagDtoBuilder sessionId(UUID sessionId) { this.sessionId = sessionId; return this; }
        public RiskFlagDtoBuilder fieldId(UUID fieldId) { this.fieldId = fieldId; return this; }
        public RiskFlagDtoBuilder fieldLabel(String fieldLabel) { this.fieldLabel = fieldLabel; return this; }
        public RiskFlagDtoBuilder riskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; return this; }
        public RiskFlagDtoBuilder warningTitle(String warningTitle) { this.warningTitle = warningTitle; return this; }
        public RiskFlagDtoBuilder warningReason(String warningReason) { this.warningReason = warningReason; return this; }
        public RiskFlagDtoBuilder consequenceExplanation(String consequenceExplanation) { this.consequenceExplanation = consequenceExplanation; return this; }
        public RiskFlagDtoBuilder confirmationStatus(ConfirmationStatus confirmationStatus) { this.confirmationStatus = confirmationStatus; return this; }
        public RiskFlagDtoBuilder confirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; return this; }

        public RiskFlagDto build() {
            return new RiskFlagDto(id, sessionId, fieldId, fieldLabel, riskLevel, warningTitle, warningReason, consequenceExplanation, confirmationStatus, confirmedAt);
        }
    }
}
