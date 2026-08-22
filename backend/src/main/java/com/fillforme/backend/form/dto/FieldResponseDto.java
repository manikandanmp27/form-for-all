package com.fillforme.backend.form.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class FieldResponseDto {
    private UUID id;
    private UUID fieldId;
    private String answerValue;
    private LocalDateTime updatedAt;

    public FieldResponseDto() {}

    public FieldResponseDto(UUID id, UUID fieldId, String answerValue, LocalDateTime updatedAt) {
        this.id = id;
        this.fieldId = fieldId;
        this.answerValue = answerValue;
        this.updatedAt = updatedAt;
    }

    public static FieldResponseDtoBuilder builder() {
        return new FieldResponseDtoBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getFieldId() { return fieldId; }
    public void setFieldId(UUID fieldId) { this.fieldId = fieldId; }

    public String getAnswerValue() { return answerValue; }
    public void setAnswerValue(String answerValue) { this.answerValue = answerValue; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class FieldResponseDtoBuilder {
        private UUID id;
        private UUID fieldId;
        private String answerValue;
        private LocalDateTime updatedAt;

        FieldResponseDtoBuilder() {}

        public FieldResponseDtoBuilder id(UUID id) { this.id = id; return this; }
        public FieldResponseDtoBuilder fieldId(UUID fieldId) { this.fieldId = fieldId; return this; }
        public FieldResponseDtoBuilder answerValue(String answerValue) { this.answerValue = answerValue; return this; }
        public FieldResponseDtoBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public FieldResponseDto build() {
            return new FieldResponseDto(id, fieldId, answerValue, updatedAt);
        }
    }
}
