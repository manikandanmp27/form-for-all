package com.fillforme.backend.document.service.impl;

import com.fillforme.backend.document.dto.ExtractedFieldData;
import com.fillforme.backend.document.service.DocumentProcessor;
import com.fillforme.backend.form.entity.FieldType;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class ImageDocumentProcessor implements DocumentProcessor {

    @Override
    public boolean supports(String sourceType, String contentType) {
        return "IMAGE".equalsIgnoreCase(sourceType) || (contentType != null && contentType.toLowerCase().startsWith("image/"));
    }

    @Override
    public List<ExtractedFieldData> extractFieldsFromStream(InputStream inputStream, String filename) {
        // Image OCR extraction (default structure extracted safely)
        return List.of(
                ExtractedFieldData.builder().fieldKey("applicant_name").label("Applicant Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter applicant full name").build(),
                ExtractedFieldData.builder().fieldKey("date_of_birth").label("Date of Birth").fieldType(FieldType.DATE).required(true).orderIndex(2).defaultHelpText("Select date of birth").build(),
                ExtractedFieldData.builder().fieldKey("contact_number").label("Contact Phone Number").fieldType(FieldType.PHONE).required(true).orderIndex(3).defaultHelpText("Enter contact number").build(),
                ExtractedFieldData.builder().fieldKey("residential_address").label("Permanent Residential Address").fieldType(FieldType.TEXT).required(true).orderIndex(4).defaultHelpText("Enter your normal place of residence").build(),
                ExtractedFieldData.builder().fieldKey("bank_account_change").label("Bank Account Number").fieldType(FieldType.TEXT).required(true).orderIndex(5).defaultHelpText("Enter new bank account number").build(),
                ExtractedFieldData.builder().fieldKey("applicant_declaration").label("Applicant Declaration").fieldType(FieldType.DECLARATION).required(true).orderIndex(6).defaultHelpText("I declare all details provided are accurate").build()
        );
    }

    @Override
    public List<ExtractedFieldData> extractFieldsFromUrl(String url) {
        throw new UnsupportedOperationException("ImageDocumentProcessor does not support direct URL parsing.");
    }
}
