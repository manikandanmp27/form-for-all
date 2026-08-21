package com.fillforme.backend.conversation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SubmitAnswerRequest {
    @NotNull(message = "Field ID is required")
    private UUID fieldId;

    private String answerValue;

    private String direction; // "NEXT" or "PREVIOUS"
}
