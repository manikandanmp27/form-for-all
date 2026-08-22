package com.fillforme.backend.document.dto;

import com.fillforme.backend.form.entity.FieldType;
import java.util.List;

public class ExtractedFieldData {
    private String fieldKey;
    private String label;
    private FieldType fieldType;
    private boolean required;
    private int orderIndex;
    private String defaultHelpText;
    private String extractedValue;
    private List<String> options;

    public ExtractedFieldData() {}

    public ExtractedFieldData(String fieldKey, String label, FieldType fieldType, boolean required, int orderIndex, String defaultHelpText, String extractedValue, List<String> options) {
        this.fieldKey = fieldKey;
        this.label = label;
        this.fieldType = fieldType;
        this.required = required;
        this.orderIndex = orderIndex;
        this.defaultHelpText = defaultHelpText;
        this.extractedValue = extractedValue;
        this.options = options;
    }

    public static ExtractedFieldDataBuilder builder() {
        return new ExtractedFieldDataBuilder();
    }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public FieldType getFieldType() { return fieldType; }
    public void setFieldType(FieldType fieldType) { this.fieldType = fieldType; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public String getDefaultHelpText() { return defaultHelpText; }
    public void setDefaultHelpText(String defaultHelpText) { this.defaultHelpText = defaultHelpText; }

    public String getExtractedValue() { return extractedValue; }
    public void setExtractedValue(String extractedValue) { this.extractedValue = extractedValue; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public static class ExtractedFieldDataBuilder {
        private String fieldKey;
        private String label;
        private FieldType fieldType;
        private boolean required;
        private int orderIndex;
        private String defaultHelpText;
        private String extractedValue;
        private List<String> options;

        ExtractedFieldDataBuilder() {}

        public ExtractedFieldDataBuilder fieldKey(String fieldKey) { this.fieldKey = fieldKey; return this; }
        public ExtractedFieldDataBuilder label(String label) { this.label = label; return this; }
        public ExtractedFieldDataBuilder fieldType(FieldType fieldType) { this.fieldType = fieldType; return this; }
        public ExtractedFieldDataBuilder required(boolean required) { this.required = required; return this; }
        public ExtractedFieldDataBuilder orderIndex(int orderIndex) { this.orderIndex = orderIndex; return this; }
        public ExtractedFieldDataBuilder defaultHelpText(String defaultHelpText) { this.defaultHelpText = defaultHelpText; return this; }
        public ExtractedFieldDataBuilder extractedValue(String extractedValue) { this.extractedValue = extractedValue; return this; }
        public ExtractedFieldDataBuilder options(List<String> options) { this.options = options; return this; }

        public ExtractedFieldData build() {
            return new ExtractedFieldData(fieldKey, label, fieldType, required, orderIndex, defaultHelpText, extractedValue, options);
        }
    }
}
