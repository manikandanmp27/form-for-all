package com.fillforme.backend.form.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "form_fields")
public class FormField {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private FormSession session;

    @Column(name = "field_order", nullable = false)
    private Integer fieldOrder;

    @Column(name = "field_key", nullable = false)
    private String fieldKey;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false)
    private FieldType fieldType;

    @Column(name = "plain_language_explanation", columnDefinition = "TEXT")
    private String plainLanguageExplanation;

    @Column(name = "why_asked", columnDefinition = "TEXT")
    private String whyAsked;

    @Column(name = "simplified_question_text", columnDefinition = "TEXT")
    private String simplifiedQuestionText;

    @Column(nullable = false)
    private Boolean required = true;

    @Column(columnDefinition = "TEXT")
    private String defaultHelpText;

    @OneToOne(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private FieldResponse response;

    public FormField() {}

    public FormField(UUID id, FormSession session, Integer fieldOrder, String fieldKey, String label, FieldType fieldType, String plainLanguageExplanation, String whyAsked, String simplifiedQuestionText, Boolean required, String defaultHelpText, FieldResponse response) {
        this.id = id;
        this.session = session;
        this.fieldOrder = fieldOrder;
        this.fieldKey = fieldKey;
        this.label = label;
        this.fieldType = fieldType;
        this.plainLanguageExplanation = plainLanguageExplanation;
        this.whyAsked = whyAsked;
        this.simplifiedQuestionText = simplifiedQuestionText;
        this.required = required != null ? required : true;
        this.defaultHelpText = defaultHelpText;
        this.response = response;
    }

    public static FormFieldBuilder builder() {
        return new FormFieldBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public FormSession getSession() { return session; }
    public void setSession(FormSession session) { this.session = session; }

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

    public FieldResponse getResponse() { return response; }
    public void setResponse(FieldResponse response) { this.response = response; }

    public static class FormFieldBuilder {
        private UUID id;
        private FormSession session;
        private Integer fieldOrder;
        private String fieldKey;
        private String label;
        private FieldType fieldType;
        private String plainLanguageExplanation;
        private String whyAsked;
        private String simplifiedQuestionText;
        private Boolean required = true;
        private String defaultHelpText;
        private FieldResponse response;

        FormFieldBuilder() {}

        public FormFieldBuilder id(UUID id) { this.id = id; return this; }
        public FormFieldBuilder session(FormSession session) { this.session = session; return this; }
        public FormFieldBuilder fieldOrder(Integer fieldOrder) { this.fieldOrder = fieldOrder; return this; }
        public FormFieldBuilder fieldKey(String fieldKey) { this.fieldKey = fieldKey; return this; }
        public FormFieldBuilder label(String label) { this.label = label; return this; }
        public FormFieldBuilder fieldType(FieldType fieldType) { this.fieldType = fieldType; return this; }
        public FormFieldBuilder plainLanguageExplanation(String plainLanguageExplanation) { this.plainLanguageExplanation = plainLanguageExplanation; return this; }
        public FormFieldBuilder whyAsked(String whyAsked) { this.whyAsked = whyAsked; return this; }
        public FormFieldBuilder simplifiedQuestionText(String simplifiedQuestionText) { this.simplifiedQuestionText = simplifiedQuestionText; return this; }
        public FormFieldBuilder required(Boolean required) { this.required = required; return this; }
        public FormFieldBuilder defaultHelpText(String defaultHelpText) { this.defaultHelpText = defaultHelpText; return this; }
        public FormFieldBuilder response(FieldResponse response) { this.response = response; return this; }

        public FormField build() {
            return new FormField(id, session, fieldOrder, fieldKey, label, fieldType, plainLanguageExplanation, whyAsked, simplifiedQuestionText, required, defaultHelpText, response);
        }
    }
}
