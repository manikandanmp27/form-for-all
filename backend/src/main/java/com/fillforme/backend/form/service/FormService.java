package com.fillforme.backend.form.service;

import com.fillforme.backend.ai.dto.AIFieldExplanation;
import com.fillforme.backend.ai.service.AIService;
import com.fillforme.backend.auth.entity.User;
import com.fillforme.backend.auth.repository.UserRepository;
import com.fillforme.backend.common.exception.ResourceNotFoundException;
import com.fillforme.backend.common.exception.UnauthorizedAccessException;
import com.fillforme.backend.document.dto.ExtractedFieldData;
import com.fillforme.backend.document.service.DocumentProcessor;
import com.fillforme.backend.document.service.StorageService;
import com.fillforme.backend.form.dto.FormFieldDto;
import com.fillforme.backend.form.dto.FormSessionDto;
import com.fillforme.backend.form.entity.FormField;
import com.fillforme.backend.form.entity.FormSession;
import com.fillforme.backend.form.entity.FormSourceType;
import com.fillforme.backend.form.entity.SessionStatus;
import com.fillforme.backend.form.repository.FormFieldRepository;
import com.fillforme.backend.form.repository.FormSessionRepository;
import com.fillforme.backend.profile.entity.AccessibilityProfile;
import com.fillforme.backend.profile.repository.AccessibilityProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FormService {

    private final FormSessionRepository sessionRepository;
    private final FormFieldRepository fieldRepository;
    private final UserRepository userRepository;
    private final AccessibilityProfileRepository profileRepository;
    private final StorageService storageService;
    private final DocumentProcessor documentProcessor;
    private final AIService aiService;

    public FormService(
            FormSessionRepository sessionRepository,
            FormFieldRepository fieldRepository,
            UserRepository userRepository,
            AccessibilityProfileRepository profileRepository,
            StorageService storageService,
            DocumentProcessor documentProcessor,
            AIService aiService) {
        this.sessionRepository = sessionRepository;
        this.fieldRepository = fieldRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.storageService = storageService;
        this.documentProcessor = documentProcessor;
        this.aiService = aiService;
    }

    @Transactional
    public FormSessionDto createSessionFromFile(UUID userId, MultipartFile file, String customTitle) {
        User user = resolveUser(userId);

        String storedFilename = storageService.store(file);
        FormSourceType sourceType = file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().endsWith(".pdf")
                ? FormSourceType.PDF : FormSourceType.IMAGE;

        String title = customTitle != null && !customTitle.isBlank()
                ? customTitle : file.getOriginalFilename();

        List<ExtractedFieldData> extracted = null;
        try {
            byte[] fileBytes = file.getBytes();
            extracted = aiService.extractFieldsWithAI(fileBytes, file.getOriginalFilename(), file.getContentType());
        } catch (Exception e) {
            // Ignore AI failure and proceed to document processor
        }

        if (extracted == null || extracted.isEmpty()) {
            try (InputStream is = file.getInputStream()) {
                extracted = documentProcessor.extractFieldsFromStream(is, file.getOriginalFilename());
            } catch (Exception ex) {
                throw new RuntimeException("Error processing document stream for file: " + file.getOriginalFilename(), ex);
            }
        }

        if (extracted == null || extracted.isEmpty()) {
            extracted = aiService.generateFieldExplanation("fallback", title, null, null) != null
                    ? List.of()
                    : List.of();
        }

        return createSessionWithExtractedFields(user, title, sourceType, null, storedFilename, extracted);
    }

    @Transactional
    public FormSessionDto createSessionFromUrl(UUID userId, String url, String customTitle) {
        User user = resolveUser(userId);

        String title = customTitle != null && !customTitle.isBlank() ? customTitle : "Form from " + url;
        List<ExtractedFieldData> extracted = documentProcessor.extractFieldsFromUrl(url);

        return createSessionWithExtractedFields(user, title, FormSourceType.URL, url, null, extracted);
    }

    private User resolveUser(UUID userId) {
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseGet(this::getOrCreateDemoUser);
        }
        return getOrCreateDemoUser();
    }

    private User getOrCreateDemoUser() {
        return userRepository.findByEmail("guest@fillforme.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("guest@fillforme.com")
                        .fullName("Guest Accessibility User")
                        .password("$2a$10$UnusedPasswordHashForGuestUserInFillForMe")
                        .build()));
    }

    @Transactional(readOnly = true)
    public List<FormSessionDto> getUserSessions(UUID userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FormSessionDto getSessionById(UUID sessionId, UUID userId) {
        FormSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Form session not found: " + sessionId));

        if (!session.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You do not have permission to access this session.");
        }

        return mapToDto(session);
    }

    private FormSessionDto createSessionWithExtractedFields(
            User user, String title, FormSourceType sourceType, String sourceUrl, String storedFilename, List<ExtractedFieldData> extracted) {

        AccessibilityProfile profile = profileRepository.findByUserId(user.getId()).orElse(null);

        FormSession session = FormSession.builder()
                .user(user)
                .formTitle(title)
                .formSourceType(sourceType)
                .sourceUrl(sourceUrl)
                .storedFilename(storedFilename)
                .sessionStatus(SessionStatus.IN_PROGRESS)
                .currentFieldIndex(0)
                .totalFields(extracted.size())
                .build();

        FormSession savedSession = sessionRepository.save(session);

        List<FormField> fields = new ArrayList<>();
        for (ExtractedFieldData data : extracted) {
            AIFieldExplanation explanation = aiService.generateFieldExplanation(
                    data.getFieldKey(), data.getLabel(), data.getDefaultHelpText(), profile);

            FormField field = FormField.builder()
                    .session(savedSession)
                    .fieldOrder(data.getOrderIndex())
                    .fieldKey(data.getFieldKey())
                    .label(data.getLabel())
                    .fieldType(data.getFieldType())
                    .required(data.isRequired())
                    .defaultHelpText(data.getDefaultHelpText())
                    .plainLanguageExplanation(explanation.getPlainLanguageExplanation())
                    .whyAsked(explanation.getWhyAskedExplanation())
                    .simplifiedQuestionText(explanation.getSimplifiedQuestionText())
                    .build();

            fields.add(field);
        }

        fieldRepository.saveAll(fields);
        savedSession.setFields(fields);

        return mapToDto(savedSession);
    }

    public FormSessionDto mapToDto(FormSession session) {
        List<FormFieldDto> fieldDtos = session.getFields().stream().map(field -> FormFieldDto.builder()
                .id(field.getId())
                .fieldOrder(field.getFieldOrder())
                .fieldKey(field.getFieldKey())
                .label(field.getLabel())
                .fieldType(field.getFieldType())
                .plainLanguageExplanation(field.getPlainLanguageExplanation())
                .whyAsked(field.getWhyAsked())
                .simplifiedQuestionText(field.getSimplifiedQuestionText())
                .required(field.getRequired())
                .defaultHelpText(field.getDefaultHelpText())
                .currentAnswer(field.getResponse() != null ? field.getResponse().getAnswerValue() : null)
                .build()).collect(Collectors.toList());

        return FormSessionDto.builder()
                .id(session.getId())
                .formTitle(session.getFormTitle())
                .formSourceType(session.getFormSourceType())
                .sourceUrl(session.getSourceUrl())
                .sessionStatus(session.getSessionStatus())
                .currentFieldIndex(session.getCurrentFieldIndex())
                .totalFields(session.getTotalFields())
                .fields(fieldDtos)
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
