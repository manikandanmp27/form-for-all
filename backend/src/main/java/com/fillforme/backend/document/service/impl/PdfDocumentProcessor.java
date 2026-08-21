package com.fillforme.backend.document.service.impl;

import com.fillforme.backend.common.exception.DocumentProcessingException;
import com.fillforme.backend.document.dto.ExtractedFieldData;
import com.fillforme.backend.document.service.DocumentProcessor;
import com.fillforme.backend.form.entity.FieldType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfDocumentProcessor implements DocumentProcessor {

    private static final Logger log = LoggerFactory.getLogger(PdfDocumentProcessor.class);

    @Override
    public boolean supports(String sourceType, String contentType) {
        return "PDF".equalsIgnoreCase(sourceType) || (contentType != null && contentType.toLowerCase().contains("pdf"));
    }

    @Override
    public List<ExtractedFieldData> extractFieldsFromStream(InputStream inputStream, String filename) {
        List<ExtractedFieldData> fields = new ArrayList<>();
        try {
            byte[] bytes = inputStream.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

                if (acroForm != null && acroForm.getFields() != null && !acroForm.getFields().isEmpty()) {
                    int order = 1;
                    for (PDField field : acroForm.getFields()) {
                        String fieldName = field.getFullyQualifiedName();
                        if (fieldName == null || fieldName.isBlank()) {
                            fieldName = "field_" + order;
                        }
                        FieldType fieldType = determineFieldType(field.getFieldType(), fieldName);
                        fields.add(ExtractedFieldData.builder()
                                .fieldKey(fieldName)
                                .label(cleanLabel(fieldName))
                                .fieldType(fieldType)
                                .required(true)
                                .orderIndex(order++)
                                .defaultHelpText("Please enter your " + cleanLabel(fieldName))
                                .build());
                    }
                }

                // If AcroForm extracted no interactive fields, parse text content to detect fields
                if (fields.isEmpty()) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    String text = stripper.getText(document);
                    fields.addAll(extractFieldsFromRawText(text));
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse PDF document: {}", filename, e);
            throw new DocumentProcessingException("Could not extract fields from PDF document: " + e.getMessage(), e);
        }

        // Fallback default fields if document has minimal readable text
        if (fields.isEmpty()) {
            fields.addAll(getDefaultFallbackFields(filename));
        }

        return fields;
    }

    @Override
    public List<ExtractedFieldData> extractFieldsFromUrl(String url) {
        throw new UnsupportedOperationException("PdfDocumentProcessor does not support direct URL parsing without download.");
    }

    private FieldType determineFieldType(String pdfFieldType, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("date") || lowerName.contains("dob")) return FieldType.DATE;
        if (lowerName.contains("email")) return FieldType.EMAIL;
        if (lowerName.contains("phone") || lowerName.contains("mobile")) return FieldType.PHONE;
        if (lowerName.contains("agree") || lowerName.contains("declaration")) return FieldType.DECLARATION;
        if (pdfFieldType != null && pdfFieldType.contains("Btn")) return FieldType.CHECKBOX;
        if (pdfFieldType != null && pdfFieldType.contains("Ch")) return FieldType.SELECT;
        return FieldType.TEXT;
    }

    private String cleanLabel(String raw) {
        String cleaned = raw.replaceAll("[_.]", " ").trim();
        if (cleaned.length() > 1) {
            return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
        }
        return cleaned;
    }

    private List<ExtractedFieldData> extractFieldsFromRawText(String text) {
        List<ExtractedFieldData> fields = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        int index = 1;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() < 3 || trimmed.length() > 80) continue;

            String lower = trimmed.toLowerCase();
            if (lower.contains("name") || lower.contains("address") || lower.contains("aadhaar") ||
                lower.contains("uid") || lower.contains("enrollment") || lower.contains("gender") ||
                lower.contains("dob") || lower.contains("phone") || lower.contains("mobile") ||
                lower.contains("email") || lower.contains("date") || lower.contains("declaration") ||
                lower.contains("guardian") || lower.contains("father") || lower.contains("mother")) {

                FieldType type = FieldType.TEXT;
                if (lower.contains("date") || lower.contains("dob")) type = FieldType.DATE;
                else if (lower.contains("email")) type = FieldType.EMAIL;
                else if (lower.contains("phone") || lower.contains("mobile")) type = FieldType.PHONE;
                else if (lower.contains("declaration") || lower.contains("agree") || lower.contains("consent")) type = FieldType.DECLARATION;

                fields.add(ExtractedFieldData.builder()
                        .fieldKey("field_" + index)
                        .label(trimmed.replaceAll("[:_?]", "").trim())
                        .fieldType(type)
                        .required(true)
                        .orderIndex(index++)
                        .defaultHelpText("Please specify " + trimmed)
                        .build());
            }

            if (fields.size() >= 15) break; // cap extracted fields per document
        }
        return fields;
    }

    private List<ExtractedFieldData> getDefaultFallbackFields(String filename) {
        String lowerName = filename != null ? filename.toLowerCase() : "";

        // If Aadhaar / Identity document
        if (lowerName.contains("aadhaar") || lowerName.contains("aadhar") || lowerName.contains("uidai") ||
            lowerName.contains("identity") || lowerName.contains("id") || lowerName.contains("voter") || lowerName.contains("passport")) {
            return List.of(
                    ExtractedFieldData.builder().fieldKey("applicantFullName").label("Full Legal Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter full name as on your ID document").build(),
                    ExtractedFieldData.builder().fieldKey("aadhaarNumber").label("Aadhaar / ID Number").fieldType(FieldType.TEXT).required(true).orderIndex(2).defaultHelpText("12-digit Aadhaar / Enrollment Number").build(),
                    ExtractedFieldData.builder().fieldKey("dateOfBirth").label("Date of Birth").fieldType(FieldType.DATE).required(true).orderIndex(3).defaultHelpText("Select your official birth date").build(),
                    ExtractedFieldData.builder().fieldKey("gender").label("Gender").fieldType(FieldType.TEXT).required(true).orderIndex(4).defaultHelpText("Male / Female / Transgender").build(),
                    ExtractedFieldData.builder().fieldKey("mobileNumber").label("Mobile Phone Number").fieldType(FieldType.PHONE).required(true).orderIndex(5).defaultHelpText("Registered mobile number").build(),
                    ExtractedFieldData.builder().fieldKey("permanentAddress").label("Permanent Residential Address").fieldType(FieldType.TEXT).required(true).orderIndex(6).defaultHelpText("Full residential address with PIN code").build(),
                    ExtractedFieldData.builder().fieldKey("declarationConsent").label("Declaration & Authorization Consent").fieldType(FieldType.DECLARATION).required(true).orderIndex(7).defaultHelpText("Confirm legal consent for identity verification").build()
            );
        }

        // Default General Form
        return List.of(
                ExtractedFieldData.builder().fieldKey("applicantFullName").label("Full Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter your official full name").build(),
                ExtractedFieldData.builder().fieldKey("dateOfBirth").label("Date of Birth").fieldType(FieldType.DATE).required(true).orderIndex(2).defaultHelpText("Select your official birth date").build(),
                ExtractedFieldData.builder().fieldKey("mobileNumber").label("Mobile Phone Number").fieldType(FieldType.PHONE).required(true).orderIndex(3).defaultHelpText("Enter your contact phone number").build(),
                ExtractedFieldData.builder().fieldKey("permanentAddress").label("Permanent Residential Address").fieldType(FieldType.TEXT).required(true).orderIndex(4).defaultHelpText("Enter your primary address").build(),
                ExtractedFieldData.builder().fieldKey("declarationConsent").label("Legal Declaration & Consent").fieldType(FieldType.DECLARATION).required(true).orderIndex(5).defaultHelpText("Confirm your legal consent").build()
        );
    }
}

