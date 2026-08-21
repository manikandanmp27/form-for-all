package com.fillforme.backend.form.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldResponseDto {
    private UUID id;
    private UUID fieldId;
    private String answerValue;
    private LocalDateTime updatedAt;
}
