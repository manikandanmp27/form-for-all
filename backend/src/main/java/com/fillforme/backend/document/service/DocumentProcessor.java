package com.fillforme.backend.document.service;

import com.fillforme.backend.document.dto.ExtractedFieldData;

import java.io.InputStream;
import java.util.List;

public interface DocumentProcessor {
    boolean supports(String sourceType, String contentType);
    List<ExtractedFieldData> extractFieldsFromStream(InputStream inputStream, String filename);
    List<ExtractedFieldData> extractFieldsFromUrl(String url);
}
