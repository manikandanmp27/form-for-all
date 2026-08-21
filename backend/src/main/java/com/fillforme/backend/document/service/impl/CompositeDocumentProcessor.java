package com.fillforme.backend.document.service.impl;

import com.fillforme.backend.common.exception.DocumentProcessingException;
import com.fillforme.backend.document.dto.ExtractedFieldData;
import com.fillforme.backend.document.service.DocumentProcessor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
@Primary
public class CompositeDocumentProcessor implements DocumentProcessor {

    private final List<DocumentProcessor> processors;

    public CompositeDocumentProcessor(List<DocumentProcessor> processors) {
        this.processors = processors;
    }

    @Override
    public boolean supports(String sourceType, String contentType) {
        return processors.stream().anyMatch(p -> p != this && p.supports(sourceType, contentType));
    }

    @Override
    public List<ExtractedFieldData> extractFieldsFromStream(InputStream inputStream, String filename) {
        String contentType = filename != null && filename.toLowerCase().endsWith(".pdf") ? "application/pdf" : "image/jpeg";
        String sourceType = filename != null && filename.toLowerCase().endsWith(".pdf") ? "PDF" : "IMAGE";

        for (DocumentProcessor processor : processors) {
            if (processor != this && processor.supports(sourceType, contentType)) {
                return processor.extractFieldsFromStream(inputStream, filename);
            }
        }
        throw new DocumentProcessingException("No suitable document processor found for file: " + filename);
    }

    @Override
    public List<ExtractedFieldData> extractFieldsFromUrl(String url) {
        for (DocumentProcessor processor : processors) {
            if (processor != this && processor.supports("URL", null)) {
                return processor.extractFieldsFromUrl(url);
            }
        }
        throw new DocumentProcessingException("No suitable document processor found for URL: " + url);
    }
}
