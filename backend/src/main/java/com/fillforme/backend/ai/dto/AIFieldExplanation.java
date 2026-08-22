package com.fillforme.backend.ai.dto;

public class AIFieldExplanation {
    private String fieldKey;
    private String plainLanguageExplanation;
    private String whyAskedExplanation;
    private String simplifiedQuestionText;

    public AIFieldExplanation() {}

    public AIFieldExplanation(String fieldKey, String plainLanguageExplanation, String whyAskedExplanation, String simplifiedQuestionText) {
        this.fieldKey = fieldKey;
        this.plainLanguageExplanation = plainLanguageExplanation;
        this.whyAskedExplanation = whyAskedExplanation;
        this.simplifiedQuestionText = simplifiedQuestionText;
    }

    public static AIFieldExplanationBuilder builder() {
        return new AIFieldExplanationBuilder();
    }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getPlainLanguageExplanation() { return plainLanguageExplanation; }
    public void setPlainLanguageExplanation(String plainLanguageExplanation) { this.plainLanguageExplanation = plainLanguageExplanation; }

    public String getWhyAskedExplanation() { return whyAskedExplanation; }
    public void setWhyAskedExplanation(String whyAskedExplanation) { this.whyAskedExplanation = whyAskedExplanation; }

    public String getSimplifiedQuestionText() { return simplifiedQuestionText; }
    public void setSimplifiedQuestionText(String simplifiedQuestionText) { this.simplifiedQuestionText = simplifiedQuestionText; }

    public static class AIFieldExplanationBuilder {
        private String fieldKey;
        private String plainLanguageExplanation;
        private String whyAskedExplanation;
        private String simplifiedQuestionText;

        AIFieldExplanationBuilder() {}

        public AIFieldExplanationBuilder fieldKey(String fieldKey) { this.fieldKey = fieldKey; return this; }
        public AIFieldExplanationBuilder plainLanguageExplanation(String plainLanguageExplanation) { this.plainLanguageExplanation = plainLanguageExplanation; return this; }
        public AIFieldExplanationBuilder whyAskedExplanation(String whyAskedExplanation) { this.whyAskedExplanation = whyAskedExplanation; return this; }
        public AIFieldExplanationBuilder simplifiedQuestionText(String simplifiedQuestionText) { this.simplifiedQuestionText = simplifiedQuestionText; return this; }

        public AIFieldExplanation build() {
            return new AIFieldExplanation(fieldKey, plainLanguageExplanation, whyAskedExplanation, simplifiedQuestionText);
        }
    }
}
