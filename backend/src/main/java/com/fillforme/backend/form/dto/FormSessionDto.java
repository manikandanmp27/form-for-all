package com.fillforme.backend.form.dto;

import com.fillforme.backend.form.entity.FormSourceType;
import com.fillforme.backend.form.entity.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class FormSessionDto {
    private UUID id;
    private String formTitle;
    private FormSourceType formSourceType;
    private String sourceUrl;
    private SessionStatus sessionStatus;
    private Integer currentFieldIndex;
    private Integer totalFields;
    private List<FormFieldDto> fields;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FormSessionDto() {}

    public FormSessionDto(UUID id, String formTitle, FormSourceType formSourceType, String sourceUrl, SessionStatus sessionStatus, Integer currentFieldIndex, Integer totalFields, List<FormFieldDto> fields, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.formTitle = formTitle;
        this.formSourceType = formSourceType;
        this.sourceUrl = sourceUrl;
        this.sessionStatus = sessionStatus;
        this.currentFieldIndex = currentFieldIndex;
        this.totalFields = totalFields;
        this.fields = fields;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FormSessionDtoBuilder builder() {
        return new FormSessionDtoBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFormTitle() { return formTitle; }
    public void setFormTitle(String formTitle) { this.formTitle = formTitle; }

    public FormSourceType getFormSourceType() { return formSourceType; }
    public void setFormSourceType(FormSourceType formSourceType) { this.formSourceType = formSourceType; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public SessionStatus getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(SessionStatus sessionStatus) { this.sessionStatus = sessionStatus; }

    public Integer getCurrentFieldIndex() { return currentFieldIndex; }
    public void setCurrentFieldIndex(Integer currentFieldIndex) { this.currentFieldIndex = currentFieldIndex; }

    public Integer getTotalFields() { return totalFields; }
    public void setTotalFields(Integer totalFields) { this.totalFields = totalFields; }

    public List<FormFieldDto> getFields() { return fields; }
    public void setFields(List<FormFieldDto> fields) { this.fields = fields; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class FormSessionDtoBuilder {
        private UUID id;
        private String formTitle;
        private FormSourceType formSourceType;
        private String sourceUrl;
        private SessionStatus sessionStatus;
        private Integer currentFieldIndex;
        private Integer totalFields;
        private List<FormFieldDto> fields;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        FormSessionDtoBuilder() {}

        public FormSessionDtoBuilder id(UUID id) { this.id = id; return this; }
        public FormSessionDtoBuilder formTitle(String formTitle) { this.formTitle = formTitle; return this; }
        public FormSessionDtoBuilder formSourceType(FormSourceType formSourceType) { this.formSourceType = formSourceType; return this; }
        public FormSessionDtoBuilder sourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; return this; }
        public FormSessionDtoBuilder sessionStatus(SessionStatus sessionStatus) { this.sessionStatus = sessionStatus; return this; }
        public FormSessionDtoBuilder currentFieldIndex(Integer currentFieldIndex) { this.currentFieldIndex = currentFieldIndex; return this; }
        public FormSessionDtoBuilder totalFields(Integer totalFields) { this.totalFields = totalFields; return this; }
        public FormSessionDtoBuilder fields(List<FormFieldDto> fields) { this.fields = fields; return this; }
        public FormSessionDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public FormSessionDtoBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public FormSessionDto build() {
            return new FormSessionDto(id, formTitle, formSourceType, sourceUrl, sessionStatus, currentFieldIndex, totalFields, fields, createdAt, updatedAt);
        }
    }
}
