package com.fillforme.backend.translation.controller;

import com.fillforme.backend.common.exception.DocumentProcessingException;
import com.fillforme.backend.common.security.UserPrincipal;
import com.fillforme.backend.translation.dto.LanguageDto;
import com.fillforme.backend.translation.dto.TranslatedFormResponseDto;
import com.fillforme.backend.translation.service.TranslationService;
import com.fillforme.backend.translation.service.impl.DocumentLayoutRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/translation")
public class TranslationController {

    private static final Logger log = LoggerFactory.getLogger(TranslationController.class);

    private final TranslationService translationService;
    private final DocumentLayoutRenderer documentLayoutRenderer;

    public TranslationController(TranslationService translationService, DocumentLayoutRenderer documentLayoutRenderer) {
        this.translationService = translationService;
        this.documentLayoutRenderer = documentLayoutRenderer;
    }

    @PostMapping("/translate-form")
    public ResponseEntity<TranslatedFormResponseDto> translateForm(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetLanguage") String targetLanguage,
            @RequestParam(value = "sourceLanguage", required = false, defaultValue = "auto") String sourceLanguage,
            @RequestParam(value = "textRegions", required = false) String textRegionsJson) {

        if (file == null || file.isEmpty()) {
            throw new DocumentProcessingException("Please upload a valid form document (PDF, PNG, or JPG).");
        }

        if (file.getSize() > 15 * 1024 * 1024) {
            throw new DocumentProcessingException("File size exceeds 15MB maximum limit.");
        }

        log.info("Receiving form translation request for '{}' -> target: {}", file.getOriginalFilename(), targetLanguage);

        try {
            byte[] bytes = file.getBytes();
            TranslatedFormResponseDto response = documentLayoutRenderer.renderTranslatedDocument(
                    bytes, file.getOriginalFilename(), file.getContentType(), sourceLanguage, targetLanguage, textRegionsJson);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Failed to translate form document: {}", file.getOriginalFilename(), e);
            TranslatedFormResponseDto errorResponse = TranslatedFormResponseDto.builder()
                    .success(false)
                    .originalFilename(file.getOriginalFilename())
                    .targetLanguage(targetLanguage)
                    .message("Form layout translation failed: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/languages")
    public ResponseEntity<List<LanguageDto>> getSupportedLanguages() {
        return ResponseEntity.ok(translationService.getSupportedLanguages());
    }
}
