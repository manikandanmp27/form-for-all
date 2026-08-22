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
        if (inputStream == null) {
            return getDefaultFallbackFields(filename);
        }
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
        if (text == null || text.isBlank()) return fields;

        String[] lines = text.split("\\r?\\n");
        int index = 1;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() < 3 || trimmed.length() > 90) continue;

            String lower = trimmed.toLowerCase();
            boolean isFormPrompt = trimmed.contains(":") || trimmed.contains("___") || trimmed.contains("....") ||
                    trimmed.matches("(?i)^(\\d+[.)]|\\([a-z0-9]+\\)).*") ||
                    lower.contains("name") || lower.contains("address") || lower.contains("number") ||
                    lower.contains("date") || lower.contains("code") || lower.contains("details") ||
                    lower.contains("title") || lower.contains("type") || lower.contains("status") ||
                    lower.contains("amount") || lower.contains("declaration") || lower.contains("signature");

            if (isFormPrompt && !lower.startsWith("http") && !lower.contains("copyright")) {
                String cleanLabel = trimmed.replaceAll("(?i)^(\\d+[.)]|\\([a-z0-9]+\\))", "")
                        .replaceAll("[:_?.*]", "")
                        .trim();

                if (cleanLabel.length() >= 3 && cleanLabel.length() <= 60) {
                    FieldType type = FieldType.TEXT;
                    if (lower.contains("date") || lower.contains("dob")) type = FieldType.DATE;
                    else if (lower.contains("email")) type = FieldType.EMAIL;
                    else if (lower.contains("phone") || lower.contains("mobile") || lower.contains("fax") || lower.contains("contact")) type = FieldType.PHONE;
                    else if (lower.contains("declaration") || lower.contains("agree") || lower.contains("consent") || lower.contains("undertaking")) type = FieldType.DECLARATION;
                    else if (lower.contains("select") || lower.contains("gender") || lower.contains("category")) type = FieldType.SELECT;

                    String fieldKey = cleanLabel.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
                    if (fieldKey.length() > 30) fieldKey = fieldKey.substring(0, 30);
                    fieldKey = fieldKey + "_" + index;

                    fields.add(ExtractedFieldData.builder()
                            .fieldKey(fieldKey)
                            .label(cleanLabel)
                            .fieldType(type)
                            .required(true)
                            .orderIndex(index++)
                            .defaultHelpText("Please specify " + cleanLabel)
                            .build());
                }
            }

            if (fields.size() >= 20) break; // cap extracted fields per document
        }
        return fields;
    }

    private List<ExtractedFieldData> getDefaultFallbackFields(String filename) {
        String lowerName = filename != null ? filename.toLowerCase() : "";

        // Banking & Finance
        if (lowerName.contains("bank") || lowerName.contains("account") || lowerName.contains("passbook") ||
            lowerName.contains("cheque") || lowerName.contains("loan") || lowerName.contains("deposit") || lowerName.contains("finance")) {
            return List.of(
                    ExtractedFieldData.builder().fieldKey("accountHolderName").label("Account Holder Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter primary account holder's full legal name").build(),
                    ExtractedFieldData.builder().fieldKey("bankAccountNumber").label("Bank Account Number").fieldType(FieldType.TEXT).required(true).orderIndex(2).defaultHelpText("Enter 9-18 digit registered bank account number").build(),
                    ExtractedFieldData.builder().fieldKey("ifscCode").label("Bank Branch IFSC Code").fieldType(FieldType.TEXT).required(true).orderIndex(3).defaultHelpText("11-character IFSC code printed on cheque/passbook").build(),
                    ExtractedFieldData.builder().fieldKey("branchName").label("Bank Branch Name").fieldType(FieldType.TEXT).required(false).orderIndex(4).defaultHelpText("Enter branch city or location name").build(),
                    ExtractedFieldData.builder().fieldKey("nomineeName").label("Nominee Full Name").fieldType(FieldType.TEXT).required(true).orderIndex(5).defaultHelpText("Designate primary beneficiary for this account").build(),
                    ExtractedFieldData.builder().fieldKey("financialConsent").label("Financial Processing Consent").fieldType(FieldType.DECLARATION).required(true).orderIndex(6).defaultHelpText("Authorize bank to process transactions on this account").build()
            );
        }

        // Tax & Income (ITR / PAN / GST)
        if (lowerName.contains("tax") || lowerName.contains("itr") || lowerName.contains("pan") ||
            lowerName.contains("gst") || lowerName.contains("income") || lowerName.contains("revenue")) {
            return List.of(
                    ExtractedFieldData.builder().fieldKey("taxpayerName").label("Taxpayer Full Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter taxpayer name as registered with revenue authority").build(),
                    ExtractedFieldData.builder().fieldKey("panNumber").label("Permanent Account Number (PAN)").fieldType(FieldType.TEXT).required(true).orderIndex(2).defaultHelpText("Enter 10-character alphanumeric PAN number").build(),
                    ExtractedFieldData.builder().fieldKey("assessmentYear").label("Assessment Year").fieldType(FieldType.TEXT).required(true).orderIndex(3).defaultHelpText("Specify tax assessment period e.g. 2025-2026").build(),
                    ExtractedFieldData.builder().fieldKey("totalGrossIncome").label("Total Annual Gross Income").fieldType(FieldType.TEXT).required(true).orderIndex(4).defaultHelpText("Enter total taxable income amount").build(),
                    ExtractedFieldData.builder().fieldKey("taxDeclaration").label("Tax Declaration Verification").fieldType(FieldType.DECLARATION).required(true).orderIndex(5).defaultHelpText("I certify that income declarations are accurate and complete").build()
            );
        }

        // Job & Employment Application
        if (lowerName.contains("job") || lowerName.contains("career") || lowerName.contains("application") ||
            lowerName.contains("resume") || lowerName.contains("employment") || lowerName.contains("hiring")) {
            return List.of(
                    ExtractedFieldData.builder().fieldKey("candidateFullName").label("Candidate Full Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter candidate full legal name").build(),
                    ExtractedFieldData.builder().fieldKey("appliedPosition").label("Position / Role Applied For").fieldType(FieldType.TEXT).required(true).orderIndex(2).defaultHelpText("Specify job position or code").build(),
                    ExtractedFieldData.builder().fieldKey("yearsExperience").label("Total Total Experience (Years)").fieldType(FieldType.TEXT).required(true).orderIndex(3).defaultHelpText("Enter total relevant work experience").build(),
                    ExtractedFieldData.builder().fieldKey("contactEmail").label("Candidate Email Address").fieldType(FieldType.EMAIL).required(true).orderIndex(4).defaultHelpText("Active email address for recruitment notices").build(),
                    ExtractedFieldData.builder().fieldKey("contactPhone").label("Contact Phone Number").fieldType(FieldType.PHONE).required(true).orderIndex(5).defaultHelpText("Primary phone number").build(),
                    ExtractedFieldData.builder().fieldKey("backgroundConsent").label("Background Check Consent").fieldType(FieldType.DECLARATION).required(true).orderIndex(6).defaultHelpText("Authorize employer to conduct background verification").build()
            );
        }

        // Medical & Health
        if (lowerName.contains("medical") || lowerName.contains("health") || lowerName.contains("hospital") ||
            lowerName.contains("patient") || lowerName.contains("doctor") || lowerName.contains("insurance")) {
            return List.of(
                    ExtractedFieldData.builder().fieldKey("patientFullName").label("Patient Full Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter patient official name").build(),
                    ExtractedFieldData.builder().fieldKey("patientAge").label("Patient Age / DOB").fieldType(FieldType.TEXT).required(true).orderIndex(2).defaultHelpText("Enter age or date of birth").build(),
                    ExtractedFieldData.builder().fieldKey("medicalCondition").label("Primary Medical Symptoms / Reason").fieldType(FieldType.TEXT).required(true).orderIndex(3).defaultHelpText("Brief description of medical purpose").build(),
                    ExtractedFieldData.builder().fieldKey("emergencyContact").label("Emergency Contact Phone").fieldType(FieldType.PHONE).required(true).orderIndex(4).defaultHelpText("Number of emergency contact person").build(),
                    ExtractedFieldData.builder().fieldKey("medicalConsent").label("Medical Treatment & Privacy Consent").fieldType(FieldType.DECLARATION).required(true).orderIndex(5).defaultHelpText("Consent to medical assessment and record sharing").build()
            );
        }

        // Identity / Aadhaar / Passport
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

        // Default General Document Form
        return List.of(
                ExtractedFieldData.builder().fieldKey("documentTitle").label("Document Title / Reference").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Name or reference number of this form").build(),
                ExtractedFieldData.builder().fieldKey("applicantFullName").label("Full Name").fieldType(FieldType.TEXT).required(true).orderIndex(2).defaultHelpText("Enter your official full name").build(),
                ExtractedFieldData.builder().fieldKey("contactEmail").label("Email Address").fieldType(FieldType.EMAIL).required(true).orderIndex(3).defaultHelpText("Enter your active email address").build(),
                ExtractedFieldData.builder().fieldKey("mobileNumber").label("Mobile Phone Number").fieldType(FieldType.PHONE).required(true).orderIndex(4).defaultHelpText("Enter your contact phone number").build(),
                ExtractedFieldData.builder().fieldKey("permanentAddress").label("Residential Address").fieldType(FieldType.TEXT).required(true).orderIndex(5).defaultHelpText("Enter your primary address").build(),
                ExtractedFieldData.builder().fieldKey("declarationConsent").label("Legal Declaration & Submission Consent").fieldType(FieldType.DECLARATION).required(true).orderIndex(6).defaultHelpText("Confirm accuracy of submitted document details").build()
        );
    }
}

