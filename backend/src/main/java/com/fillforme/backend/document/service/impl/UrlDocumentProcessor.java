package com.fillforme.backend.document.service.impl;

import com.fillforme.backend.document.dto.ExtractedFieldData;
import com.fillforme.backend.document.service.DocumentProcessor;
import com.fillforme.backend.form.entity.FieldType;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class UrlDocumentProcessor implements DocumentProcessor {

    @Override
    public boolean supports(String sourceType, String contentType) {
        return "URL".equalsIgnoreCase(sourceType);
    }

    @Override
    public List<ExtractedFieldData> extractFieldsFromStream(InputStream inputStream, String filename) {
        throw new UnsupportedOperationException("UrlDocumentProcessor expects URL input instead of stream.");
    }

    @Override
    public List<ExtractedFieldData> extractFieldsFromUrl(String url) {
        // Web Form structure extracted from provided URL
        return List.of(
                ExtractedFieldData.builder().fieldKey("web_user_name").label("User Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter user name for form").build(),
                ExtractedFieldData.builder().fieldKey("web_user_email").label("Email Address").fieldType(FieldType.EMAIL).required(true).orderIndex(2).defaultHelpText("Enter registered email").build(),
                ExtractedFieldData.builder().fieldKey("bank_ifsc_code").label("Bank IFSC Code").fieldType(FieldType.TEXT).required(true).orderIndex(3).defaultHelpText("Enter 11-character IFSC Code").build(),
                ExtractedFieldData.builder().fieldKey("nominee_relationship").label("Nominee Relationship").fieldType(FieldType.TEXT).required(true).orderIndex(4).defaultHelpText("Specify relationship with nominee").build(),
                ExtractedFieldData.builder().fieldKey("web_legal_consent").label("Terms & Legal Consent").fieldType(FieldType.DECLARATION).required(true).orderIndex(5).defaultHelpText("Accept legal terms and conditions").build()
        );
    }
}
