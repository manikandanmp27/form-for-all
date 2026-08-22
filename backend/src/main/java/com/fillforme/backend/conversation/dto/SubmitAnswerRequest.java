package com.fillforme.backend.conversation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class SubmitAnswerRequest {
    @NotNull(message = "Field ID is required")
    private UUID fieldId;

    private String answerValue;

    private String direction; // "NEXT" or "PREVIOUS"

    public SubmitAnswerRequest() {}

    public SubmitAnswerRequest(UUID fieldId, String answerValue, String direction) {
        this.fieldId = fieldId;
        this.answerValue = answerValue;
        this.direction = direction;
    }

    public UUID getFieldId() { return fieldId; }
    public void setFieldId(UUID fieldId) { this.fieldId = fieldId; }

    public String getAnswerValue() { return answerValue; }
    public void setAnswerValue(String answerValue) { this.answerValue = answerValue; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
}
