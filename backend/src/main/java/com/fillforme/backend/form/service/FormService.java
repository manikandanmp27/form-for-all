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
import com.fillforme.backend.form.entity.FieldType;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public FormSessionDto createSessionFromFile(UUID userId, MultipartFile file, String customTitle, String customFieldsJson) {
        User user = resolveUser(userId);

        String storedFilename = storageService.store(file);
        FormSourceType sourceType = file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().endsWith(".pdf")
                ? FormSourceType.PDF : FormSourceType.IMAGE;

        String title = customTitle != null && !customTitle.isBlank()
                ? customTitle : file.getOriginalFilename();

        List<ExtractedFieldData> extracted = null;

        if (customFieldsJson != null && !customFieldsJson.isBlank()) {
            extracted = parseCustomFieldsJson(customFieldsJson);
        }

        if (extracted == null || extracted.isEmpty()) {
            try {
                byte[] fileBytes = file.getBytes();
                extracted = aiService.extractFieldsWithAI(fileBytes, file.getOriginalFilename(), file.getContentType());
            } catch (Exception e) {
                // Ignore AI failure and proceed to document processor
            }
        }

        if (extracted == null || extracted.isEmpty()) {
            try (InputStream is = file.getInputStream()) {
                extracted = documentProcessor.extractFieldsFromStream(is, file.getOriginalFilename());
            } catch (Exception ex) {
                throw new RuntimeException("Error processing document stream for file: " + file.getOriginalFilename(), ex);
            }
        }

        if (extracted == null || extracted.isEmpty()) {
            extracted = List.of(
                    ExtractedFieldData.builder().orderIndex(1).fieldKey("firstPersonName").label("First Person Full Name").fieldType(FieldType.TEXT).required(true).defaultHelpText("Enter the first person's full legal name.").build(),
                    ExtractedFieldData.builder().orderIndex(2).fieldKey("secondPersonName").label("Second Person Full Name").fieldType(FieldType.TEXT).required(true).defaultHelpText("Enter the second person's full legal name.").build(),
                    ExtractedFieldData.builder().orderIndex(3).fieldKey("thirdPersonName").label("Third Person Full Name").fieldType(FieldType.TEXT).required(true).defaultHelpText("Enter the third person's full legal name.").build()
            );
        }

        return createSessionWithExtractedFields(user, title, sourceType, null, storedFilename, extracted);
    }

    private List<ExtractedFieldData> parseCustomFieldsJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> list = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
            List<ExtractedFieldData> result = new ArrayList<>();
            int idx = 1;
            for (Map<String, Object> map : list) {
                String label = (String) map.getOrDefault("label", "Form Field " + idx);
                String key = (String) map.getOrDefault("fieldKey", "field_" + idx);
                String typeStr = (String) map.getOrDefault("fieldType", "TEXT");
                FieldType type = FieldType.TEXT;
                try {
                    type = FieldType.valueOf(typeStr.toUpperCase());
                } catch (Exception ignored) {}

                result.add(ExtractedFieldData.builder()
                        .orderIndex(idx++)
                        .fieldKey(key)
                        .label(label)
                        .fieldType(type)
                        .required(true)
                        .defaultHelpText("Please enter details for " + label)
                        .build());
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
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
                .orElseGet(() -> {
                    try {
                        return userRepository.save(User.builder()
                                .email("guest@fillforme.com")
                                .fullName("Guest Accessibility User")
                                .password("$2a$10$UnusedPasswordHashForGuestUserInFillForMe")
                                .role("ROLE_USER")
                                .build());
                    } catch (Exception e) {
                        return userRepository.findByEmail("guest@fillforme.com").orElse(null);
                    }
                });
    }

    @Transactional(readOnly = true)
    public List<FormSessionDto> getUserSessions(UUID userId) {
        if (userId == null) {
            User demoUser = getOrCreateDemoUser();
            userId = demoUser.getId();
        }
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FormSessionDto getSessionById(UUID sessionId, UUID userId) {
        FormSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Form session not found: " + sessionId));

        if (userId != null && !session.getUser().getId().equals(userId) && !session.getUser().getEmail().equals("guest@fillforme.com")) {
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
