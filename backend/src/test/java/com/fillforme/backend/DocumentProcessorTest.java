package com.fillforme.backend;

import com.fillforme.backend.document.dto.ExtractedFieldData;
import com.fillforme.backend.document.service.impl.ImageDocumentProcessor;
import com.fillforme.backend.document.service.impl.PdfDocumentProcessor;
import com.fillforme.backend.document.service.impl.UrlDocumentProcessor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentProcessorTest {

    @Test
    public void testPdfProcessorFallbackExtractsTaxFields() {
        PdfDocumentProcessor processor = new PdfDocumentProcessor();

        // Testing fallback categorization when a scanned/empty PDF stream is passed
        List<ExtractedFieldData> fields = processor.extractFieldsFromStream(null, "tax_return_2025.pdf");

        assertNotNull(fields);
        assertFalse(fields.isEmpty(), "Extracted fields list should not be empty for tax PDF");
        assertTrue(fields.stream().anyMatch(f -> f.getFieldKey().equals("taxpayerName")), "Should extract taxpayerName for tax PDF");
        assertTrue(fields.stream().anyMatch(f -> f.getFieldKey().equals("panNumber")), "Should extract panNumber for tax PDF");
    }

    @Test
    public void testBankingImageFallbackExtractsBankingFields() {
        ImageDocumentProcessor processor = new ImageDocumentProcessor();

        List<ExtractedFieldData> fields = processor.extractFieldsFromStream(null, "bank_passbook_copy.png");

        assertNotNull(fields);
        assertFalse(fields.isEmpty());
        assertTrue(fields.stream().anyMatch(f -> f.getFieldKey().equals("accountHolderName")), "Bank image form should extract accountHolderName");
        assertTrue(fields.stream().anyMatch(f -> f.getFieldKey().equals("ifscCode")), "Bank image form should extract ifscCode");
    }

    @Test
    public void testJobApplicationImageFallbackExtractsJobFields() {
        ImageDocumentProcessor processor = new ImageDocumentProcessor();

        List<ExtractedFieldData> fields = processor.extractFieldsFromStream(null, "job_application_form.jpeg");

        assertNotNull(fields);
        assertFalse(fields.isEmpty());
        assertTrue(fields.stream().anyMatch(f -> f.getFieldKey().equals("jobTitle")), "Job form should extract jobTitle");
        assertTrue(fields.stream().anyMatch(f -> f.getFieldKey().equals("applicantName")), "Job form should extract applicantName");
    }

    @Test
    public void testUrlProcessorCategorizedFallback() {
        UrlDocumentProcessor processor = new UrlDocumentProcessor();

        List<ExtractedFieldData> fields = processor.extractFieldsFromUrl("https://example-bank-portal.com/open-account");

        assertNotNull(fields);
        assertFalse(fields.isEmpty());
        assertTrue(fields.stream().anyMatch(f -> f.getFieldKey().contains("account")), "Banking URL should extract banking fields");
    }
}
