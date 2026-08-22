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
        String lowerName = filename != null ? filename.toLowerCase() : "";

        // Banking & Finance Image (Cheque / Passbook / Account Form)
        if (lowerName.contains("bank") || lowerName.contains("account") || lowerName.contains("cheque") ||
            lowerName.contains("passbook") || lowerName.contains("loan") || lowerName.contains("deposit")) {
            return List.of(
                    ExtractedFieldData.builder().fieldKey("accountHolderName").label("Account Holder Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter primary account holder name").build(),
                    ExtractedFieldData.builder().fieldKey("accountNumber").label("Bank Account Number").fieldType(FieldType.TEXT).required(true).orderIndex(2).defaultHelpText("Enter bank account number shown on document").build(),
                    ExtractedFieldData.builder().fieldKey("ifscCode").label("Branch IFSC Code").fieldType(FieldType.TEXT).required(true).orderIndex(3).defaultHelpText("Enter 11-character IFSC code").build(),
                    ExtractedFieldData.builder().fieldKey("branchName").label("Branch Name / Location").fieldType(FieldType.TEXT).required(false).orderIndex(4).defaultHelpText("Enter bank branch name").build(),
                    ExtractedFieldData.builder().fieldKey("bankConsent").label("Bank Processing Consent").fieldType(FieldType.DECLARATION).required(true).orderIndex(5).defaultHelpText("Authorize bank account details verification").build()
            );
        }

        // Tax / Revenue / PAN Image
        if (lowerName.contains("tax") || lowerName.contains("pan") || lowerName.contains("itr") ||
            lowerName.contains("gst") || lowerName.contains("income")) {
            return List.of(
                    ExtractedFieldData.builder().fieldKey("taxpayerName").label("Taxpayer Full Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter taxpayer name as on document").build(),
                    ExtractedFieldData.builder().fieldKey("panNumber").label("PAN Card Number").fieldType(FieldType.TEXT).required(true).orderIndex(2).defaultHelpText("Enter 10-character PAN number").build(),
                    ExtractedFieldData.builder().fieldKey("assessmentYear").label("Assessment Year").fieldType(FieldType.TEXT).required(true).orderIndex(3).defaultHelpText("Enter tax year").build(),
                    ExtractedFieldData.builder().fieldKey("incomeDeclaration").label("Tax Declaration").fieldType(FieldType.DECLARATION).required(true).orderIndex(4).defaultHelpText("Confirm tax document accuracy").build()
            );
        }

        // Job & Employment Application Image
        if (lowerName.contains("job") || lowerName.contains("career") || lowerName.contains("resume") ||
            lowerName.contains("application") || lowerName.contains("hiring") || lowerName.contains("employment")) {
            return List.of(
                    ExtractedFieldData.builder().fieldKey("applicantName").label("Applicant Full Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter applicant full legal name").build(),
                    ExtractedFieldData.builder().fieldKey("jobTitle").label("Desired Position / Role").fieldType(FieldType.TEXT).required(true).orderIndex(2).defaultHelpText("Enter position applied for").build(),
                    ExtractedFieldData.builder().fieldKey("contactEmail").label("Email Address").fieldType(FieldType.EMAIL).required(true).orderIndex(3).defaultHelpText("Enter contact email address").build(),
                    ExtractedFieldData.builder().fieldKey("contactPhone").label("Phone Number").fieldType(FieldType.PHONE).required(true).orderIndex(4).defaultHelpText("Enter mobile phone number").build(),
                    ExtractedFieldData.builder().fieldKey("employmentConsent").label("Application Consent").fieldType(FieldType.DECLARATION).required(true).orderIndex(5).defaultHelpText("Confirm job application details").build()
            );
        }

        // Medical / Hospital / Insurance Form Image
        if (lowerName.contains("medical") || lowerName.contains("health") || lowerName.contains("patient") ||
            lowerName.contains("hospital") || lowerName.contains("insurance")) {
            return List.of(
                    ExtractedFieldData.builder().fieldKey("patientName").label("Patient Full Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter patient full name").build(),
                    ExtractedFieldData.builder().fieldKey("patientAge").label("Patient Age / DOB").fieldType(FieldType.TEXT).required(true).orderIndex(2).defaultHelpText("Enter patient age or date of birth").build(),
                    ExtractedFieldData.builder().fieldKey("medicalPurpose").label("Reason for Medical Care").fieldType(FieldType.TEXT).required(true).orderIndex(3).defaultHelpText("Enter symptoms or medical purpose").build(),
                    ExtractedFieldData.builder().fieldKey("emergencyPhone").label("Emergency Contact Phone").fieldType(FieldType.PHONE).required(true).orderIndex(4).defaultHelpText("Enter emergency contact number").build(),
                    ExtractedFieldData.builder().fieldKey("healthConsent").label("Medical Treatment Consent").fieldType(FieldType.DECLARATION).required(true).orderIndex(5).defaultHelpText("Consent to medical record review").build()
            );
        }

        // Aadhaar / Passport / Identity Card Image
        if (lowerName.contains("aadhaar") || lowerName.contains("aadhar") || lowerName.contains("uidai") ||
            lowerName.contains("identity") || lowerName.contains("id") || lowerName.contains("voter") || lowerName.contains("passport")) {
            return List.of(
                    ExtractedFieldData.builder().fieldKey("applicantFullName").label("Full Legal Name").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Enter applicant full legal name").build(),
                    ExtractedFieldData.builder().fieldKey("aadhaarNumber").label("Aadhaar / ID Number").fieldType(FieldType.TEXT).required(true).orderIndex(2).defaultHelpText("Enter 12-digit Aadhaar number").build(),
                    ExtractedFieldData.builder().fieldKey("dateOfBirth").label("Date of Birth").fieldType(FieldType.DATE).required(true).orderIndex(3).defaultHelpText("Select date of birth").build(),
                    ExtractedFieldData.builder().fieldKey("gender").label("Gender").fieldType(FieldType.TEXT).required(true).orderIndex(4).defaultHelpText("Male / Female / Transgender").build(),
                    ExtractedFieldData.builder().fieldKey("contactNumber").label("Contact Phone Number").fieldType(FieldType.PHONE).required(true).orderIndex(5).defaultHelpText("Enter contact number").build(),
                    ExtractedFieldData.builder().fieldKey("residentialAddress").label("Permanent Residential Address").fieldType(FieldType.TEXT).required(true).orderIndex(6).defaultHelpText("Enter permanent address").build(),
                    ExtractedFieldData.builder().fieldKey("applicantDeclaration").label("Applicant Declaration").fieldType(FieldType.DECLARATION).required(true).orderIndex(7).defaultHelpText("I declare all details provided are accurate").build()
            );
        }

        // Standard General Image Form
        return List.of(
                ExtractedFieldData.builder().fieldKey("imageFormTitle").label("Form Image Title / Note").fieldType(FieldType.TEXT).required(true).orderIndex(1).defaultHelpText("Reference title for this image form").build(),
                ExtractedFieldData.builder().fieldKey("applicantFullName").label("Full Legal Name").fieldType(FieldType.TEXT).required(true).orderIndex(2).defaultHelpText("Enter applicant full legal name").build(),
                ExtractedFieldData.builder().fieldKey("dateOfBirth").label("Date of Birth").fieldType(FieldType.DATE).required(true).orderIndex(3).defaultHelpText("Select date of birth").build(),
                ExtractedFieldData.builder().fieldKey("contactNumber").label("Contact Phone Number").fieldType(FieldType.PHONE).required(true).orderIndex(4).defaultHelpText("Enter contact number").build(),
                ExtractedFieldData.builder().fieldKey("residentialAddress").label("Permanent Residential Address").fieldType(FieldType.TEXT).required(true).orderIndex(5).defaultHelpText("Enter permanent address").build(),
                ExtractedFieldData.builder().fieldKey("applicantDeclaration").label("Applicant Declaration").fieldType(FieldType.DECLARATION).required(true).orderIndex(6).defaultHelpText("I declare all details provided are accurate").build()
        );
    }

    @Override
    public List<ExtractedFieldData> extractFieldsFromUrl(String url) {
        throw new UnsupportedOperationException("ImageDocumentProcessor does not support direct URL parsing.");
    }
}
