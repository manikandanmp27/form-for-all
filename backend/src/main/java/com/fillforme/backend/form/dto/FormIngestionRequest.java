package com.fillforme.backend.form.dto;

import com.fillforme.backend.form.entity.FormSourceType;

public class FormIngestionRequest {
    private FormSourceType sourceType;
    private String formUrl;
    private String formTitle;

    public FormIngestionRequest() {}

    public FormIngestionRequest(FormSourceType sourceType, String formUrl, String formTitle) {
        this.sourceType = sourceType;
        this.formUrl = formUrl;
        this.formTitle = formTitle;
    }

    public FormSourceType getSourceType() { return sourceType; }
    public void setSourceType(FormSourceType sourceType) { this.sourceType = sourceType; }

    public String getFormUrl() { return formUrl; }
    public void setFormUrl(String formUrl) { this.formUrl = formUrl; }

    public String getFormTitle() { return formTitle; }
    public void setFormTitle(String formTitle) { this.formTitle = formTitle; }
}
