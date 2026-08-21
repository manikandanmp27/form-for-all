package com.fillforme.backend.form.dto;

import com.fillforme.backend.form.entity.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormFieldDto {
    private UUID id;
    private Integer fieldOrder;
    private String fieldKey;
    private String label;
    private FieldType fieldType;
    private String plainLanguageExplanation;
    private String whyAsked;
    private String simplifiedQuestionText;
    private Boolean required;
    private String defaultHelpText;
    private String currentAnswer;
}
