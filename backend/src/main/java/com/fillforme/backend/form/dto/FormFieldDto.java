package com.fillforme.backend.form.dto;

import com.fillforme.backend.form.entity.FieldType;

import java.util.UUID;

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

    public FormFieldDto() {}

    public FormFieldDto(UUID id, Integer fieldOrder, String fieldKey, String label, FieldType fieldType, String plainLanguageExplanation, String whyAsked, String simplifiedQuestionText, Boolean required, String defaultHelpText, String currentAnswer) {
        this.id = id;
        this.fieldOrder = fieldOrder;
        this.fieldKey = fieldKey;
        this.label = label;
        this.fieldType = fieldType;
        this.plainLanguageExplanation = plainLanguageExplanation;
        this.whyAsked = whyAsked;
        this.simplifiedQuestionText = simplifiedQuestionText;
        this.required = required;
        this.defaultHelpText = defaultHelpText;
        this.currentAnswer = currentAnswer;
    }

    public static FormFieldDtoBuilder builder() {
        return new FormFieldDtoBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getFieldOrder() { return fieldOrder; }
    public void setFieldOrder(Integer fieldOrder) { this.fieldOrder = fieldOrder; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public FieldType getFieldType() { return fieldType; }
    public void setFieldType(FieldType fieldType) { this.fieldType = fieldType; }

    public String getPlainLanguageExplanation() { return plainLanguageExplanation; }
    public void setPlainLanguageExplanation(String plainLanguageExplanation) { this.plainLanguageExplanation = plainLanguageExplanation; }

    public String getWhyAsked() { return whyAsked; }
    public void setWhyAsked(String whyAsked) { this.whyAsked = whyAsked; }

    public String getSimplifiedQuestionText() { return simplifiedQuestionText; }
    public void setSimplifiedQuestionText(String simplifiedQuestionText) { this.simplifiedQuestionText = simplifiedQuestionText; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public String getDefaultHelpText() { return defaultHelpText; }
    public void setDefaultHelpText(String defaultHelpText) { this.defaultHelpText = defaultHelpText; }

    public String getCurrentAnswer() { return currentAnswer; }
    public void setCurrentAnswer(String currentAnswer) { this.currentAnswer = currentAnswer; }

    public static class FormFieldDtoBuilder {
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

        FormFieldDtoBuilder() {}

        public FormFieldDtoBuilder id(UUID id) { this.id = id; return this; }
        public FormFieldDtoBuilder fieldOrder(Integer fieldOrder) { this.fieldOrder = fieldOrder; return this; }
        public FormFieldDtoBuilder fieldKey(String fieldKey) { this.fieldKey = fieldKey; return this; }
        public FormFieldDtoBuilder label(String label) { this.label = label; return this; }
        public FormFieldDtoBuilder fieldType(FieldType fieldType) { this.fieldType = fieldType; return this; }
        public FormFieldDtoBuilder plainLanguageExplanation(String plainLanguageExplanation) { this.plainLanguageExplanation = plainLanguageExplanation; return this; }
        public FormFieldDtoBuilder whyAsked(String whyAsked) { this.whyAsked = whyAsked; return this; }
        public FormFieldDtoBuilder simplifiedQuestionText(String simplifiedQuestionText) { this.simplifiedQuestionText = simplifiedQuestionText; return this; }
        public FormFieldDtoBuilder required(Boolean required) { this.required = required; return this; }
        public FormFieldDtoBuilder defaultHelpText(String defaultHelpText) { this.defaultHelpText = defaultHelpText; return this; }
        public FormFieldDtoBuilder currentAnswer(String currentAnswer) { this.currentAnswer = currentAnswer; return this; }

        public FormFieldDto build() {
            return new FormFieldDto(id, fieldOrder, fieldKey, label, fieldType, plainLanguageExplanation, whyAsked, simplifiedQuestionText, required, defaultHelpText, currentAnswer);
        }
    }
}
