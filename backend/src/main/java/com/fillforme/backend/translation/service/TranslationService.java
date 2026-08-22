package com.fillforme.backend.translation.service;

import com.fillforme.backend.translation.dto.LanguageDto;

import java.util.List;

public interface TranslationService {
    String translate(String text, String sourceLanguage, String targetLanguage);
    String detectLanguage(String text);
    List<LanguageDto> getSupportedLanguages();
}
