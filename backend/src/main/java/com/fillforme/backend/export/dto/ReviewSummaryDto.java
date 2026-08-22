package com.fillforme.backend.export.dto;

import com.fillforme.backend.form.dto.FormFieldDto;
import com.fillforme.backend.form.entity.SessionStatus;
import com.fillforme.backend.risk.dto.RiskFlagDto;

import java.util.List;
import java.util.UUID;

public class ReviewSummaryDto {
    private UUID sessionId;
    private String formTitle;
    private SessionStatus sessionStatus;
    private int totalFields;
    private int answeredFields;
    private boolean isReadyForSubmission;
    private List<String> missingRequiredFields;
    private boolean hasUnconfirmedHighRiskFlags;
    private List<RiskFlagDto> unconfirmedRiskFlags;
    private List<FormFieldDto> fields;

    public ReviewSummaryDto() {}

    public ReviewSummaryDto(UUID sessionId, String formTitle, SessionStatus sessionStatus, int totalFields, int answeredFields, boolean isReadyForSubmission, List<String> missingRequiredFields, boolean hasUnconfirmedHighRiskFlags, List<RiskFlagDto> unconfirmedRiskFlags, List<FormFieldDto> fields) {
        this.sessionId = sessionId;
        this.formTitle = formTitle;
        this.sessionStatus = sessionStatus;
        this.totalFields = totalFields;
        this.answeredFields = answeredFields;
        this.isReadyForSubmission = isReadyForSubmission;
        this.missingRequiredFields = missingRequiredFields;
        this.hasUnconfirmedHighRiskFlags = hasUnconfirmedHighRiskFlags;
        this.unconfirmedRiskFlags = unconfirmedRiskFlags;
        this.fields = fields;
    }

    public static ReviewSummaryDtoBuilder builder() {
        return new ReviewSummaryDtoBuilder();
    }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public String getFormTitle() { return formTitle; }
    public void setFormTitle(String formTitle) { this.formTitle = formTitle; }

    public SessionStatus getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(SessionStatus sessionStatus) { this.sessionStatus = sessionStatus; }

    public int getTotalFields() { return totalFields; }
    public void setTotalFields(int totalFields) { this.totalFields = totalFields; }

    public int getAnsweredFields() { return answeredFields; }
    public void setAnsweredFields(int answeredFields) { this.answeredFields = answeredFields; }

    public boolean isReadyForSubmission() { return isReadyForSubmission; }
    public void setReadyForSubmission(boolean isReadyForSubmission) { this.isReadyForSubmission = isReadyForSubmission; }

    public List<String> getMissingRequiredFields() { return missingRequiredFields; }
    public void setMissingRequiredFields(List<String> missingRequiredFields) { this.missingRequiredFields = missingRequiredFields; }

    public boolean isHasUnconfirmedHighRiskFlags() { return hasUnconfirmedHighRiskFlags; }
    public void setHasUnconfirmedHighRiskFlags(boolean hasUnconfirmedHighRiskFlags) { this.hasUnconfirmedHighRiskFlags = hasUnconfirmedHighRiskFlags; }

    public List<RiskFlagDto> getUnconfirmedRiskFlags() { return unconfirmedRiskFlags; }
    public void setUnconfirmedRiskFlags(List<RiskFlagDto> unconfirmedRiskFlags) { this.unconfirmedRiskFlags = unconfirmedRiskFlags; }

    public List<FormFieldDto> getFields() { return fields; }
    public void setFields(List<FormFieldDto> fields) { this.fields = fields; }

    public static class ReviewSummaryDtoBuilder {
        private UUID sessionId;
        private String formTitle;
        private SessionStatus sessionStatus;
        private int totalFields;
        private int answeredFields;
        private boolean isReadyForSubmission;
        private List<String> missingRequiredFields;
        private boolean hasUnconfirmedHighRiskFlags;
        private List<RiskFlagDto> unconfirmedRiskFlags;
        private List<FormFieldDto> fields;

        ReviewSummaryDtoBuilder() {}

        public ReviewSummaryDtoBuilder sessionId(UUID sessionId) { this.sessionId = sessionId; return this; }
        public ReviewSummaryDtoBuilder formTitle(String formTitle) { this.formTitle = formTitle; return this; }
        public ReviewSummaryDtoBuilder sessionStatus(SessionStatus sessionStatus) { this.sessionStatus = sessionStatus; return this; }
        public ReviewSummaryDtoBuilder totalFields(int totalFields) { this.totalFields = totalFields; return this; }
        public ReviewSummaryDtoBuilder answeredFields(int answeredFields) { this.answeredFields = answeredFields; return this; }
        public ReviewSummaryDtoBuilder isReadyForSubmission(boolean isReadyForSubmission) { this.isReadyForSubmission = isReadyForSubmission; return this; }
        public ReviewSummaryDtoBuilder missingRequiredFields(List<String> missingRequiredFields) { this.missingRequiredFields = missingRequiredFields; return this; }
        public ReviewSummaryDtoBuilder hasUnconfirmedHighRiskFlags(boolean hasUnconfirmedHighRiskFlags) { this.hasUnconfirmedHighRiskFlags = hasUnconfirmedHighRiskFlags; return this; }
        public ReviewSummaryDtoBuilder unconfirmedRiskFlags(List<RiskFlagDto> unconfirmedRiskFlags) { this.unconfirmedRiskFlags = unconfirmedRiskFlags; return this; }
        public ReviewSummaryDtoBuilder fields(List<FormFieldDto> fields) { this.fields = fields; return this; }

        public ReviewSummaryDto build() {
            return new ReviewSummaryDto(sessionId, formTitle, sessionStatus, totalFields, answeredFields, isReadyForSubmission, missingRequiredFields, hasUnconfirmedHighRiskFlags, unconfirmedRiskFlags, fields);
        }
    }
}
