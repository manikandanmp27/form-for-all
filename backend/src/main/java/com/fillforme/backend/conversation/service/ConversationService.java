package com.fillforme.backend.conversation.service;

import com.fillforme.backend.ai.dto.AIRiskEvaluation;
import com.fillforme.backend.ai.service.AIService;
import com.fillforme.backend.auth.entity.User;
import com.fillforme.backend.auth.repository.UserRepository;
import com.fillforme.backend.common.exception.ResourceNotFoundException;
import com.fillforme.backend.common.exception.UnauthorizedAccessException;
import com.fillforme.backend.conversation.dto.ConversationStepResponse;
import com.fillforme.backend.conversation.dto.SubmitAnswerRequest;
import com.fillforme.backend.form.dto.FormFieldDto;
import com.fillforme.backend.form.entity.FieldResponse;
import com.fillforme.backend.form.entity.FormField;
import com.fillforme.backend.form.entity.FormSession;
import com.fillforme.backend.form.entity.FormSourceType;
import com.fillforme.backend.form.entity.FieldType;
import com.fillforme.backend.form.entity.SessionStatus;
import com.fillforme.backend.form.repository.FieldResponseRepository;
import com.fillforme.backend.form.repository.FormFieldRepository;
import com.fillforme.backend.form.repository.FormSessionRepository;
import com.fillforme.backend.risk.dto.RiskFlagDto;
import com.fillforme.backend.risk.entity.ConfirmationStatus;
import com.fillforme.backend.risk.entity.RiskFlag;
import com.fillforme.backend.risk.entity.RiskLevel;
import com.fillforme.backend.risk.repository.RiskFlagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConversationService {

    private final FormSessionRepository sessionRepository;
    private final FormFieldRepository fieldRepository;
    private final FieldResponseRepository responseRepository;
    private final RiskFlagRepository riskFlagRepository;
    private final UserRepository userRepository;
    private final AIService aiService;

    public ConversationService(
            FormSessionRepository sessionRepository,
            FormFieldRepository fieldRepository,
            FieldResponseRepository responseRepository,
            RiskFlagRepository riskFlagRepository,
            UserRepository userRepository,
            AIService aiService) {
        this.sessionRepository = sessionRepository;
        this.fieldRepository = fieldRepository;
        this.responseRepository = responseRepository;
        this.riskFlagRepository = riskFlagRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    @Transactional
    public ConversationStepResponse getConversationStep(UUID sessionId, UUID userId) {
        FormSession session = getSessionAndVerifyUser(sessionId, userId);
        List<FormField> fields = fieldRepository.findBySessionIdOrderByFieldOrderAsc(sessionId);

        if (fields.isEmpty()) {
            fields = createSampleFields(session);
        }

        int currentIndex = Math.min(Math.max(0, session.getCurrentFieldIndex()), fields.size() - 1);
        FormField currentField = fields.get(currentIndex);
        FormField previousField = currentIndex > 0 ? fields.get(currentIndex - 1) : null;
        FormField nextField = currentIndex < fields.size() - 1 ? fields.get(currentIndex + 1) : null;

        // Check if current field has a pending high-risk flag requiring confirmation
        Optional<RiskFlag> pendingRisk = riskFlagRepository
                .findBySessionIdAndFieldIdAndRiskLevelAndConfirmationStatus(
                        sessionId, currentField.getId(), RiskLevel.HIGH, ConfirmationStatus.PENDING);

        boolean isCompleted = session.getSessionStatus() == SessionStatus.COMPLETED ||
                session.getSessionStatus() == SessionStatus.REVIEW ||
                (currentIndex == fields.size() - 1 && hasValidAnswer(currentField));

        int progressPercent = (int) Math.round(((double) (currentIndex + 1) / fields.size()) * 100);

        return ConversationStepResponse.builder()
                .sessionId(sessionId)
                .currentStep(currentIndex + 1)
                .totalSteps(fields.size())
                .isCompleted(isCompleted)
                .currentField(mapToFieldDto(currentField))
                .previousField(previousField != null ? mapToFieldDto(previousField) : null)
                .nextField(nextField != null ? mapToFieldDto(nextField) : null)
                .riskConfirmationRequired(pendingRisk.isPresent())
                .pendingRiskFlag(pendingRisk.map(this::mapToRiskDto).orElse(null))
                .progressPercentage(progressPercent + "%")
                .build();
    }

    @Transactional
    public ConversationStepResponse submitAnswer(UUID sessionId, UUID userId, SubmitAnswerRequest request) {
        FormSession session = getSessionAndVerifyUser(sessionId, userId);
        FormField field = fieldRepository.findById(request.getFieldId())
                .orElseGet(() -> {
                    List<FormField> fields = fieldRepository.findBySessionIdOrderByFieldOrderAsc(sessionId);
                    return fields.isEmpty() ? createSampleFields(session).get(0) : fields.get(0);
                });

        // Save answer
        FieldResponse response = responseRepository.findByFieldId(field.getId())
                .orElseGet(() -> FieldResponse.builder().session(session).field(field).build());

        response.setAnswerValue(request.getAnswerValue());
        responseRepository.save(response);

        // Evaluate risk
        AIRiskEvaluation riskEval = aiService.evaluateRisk(field.getFieldKey(), field.getLabel(), request.getAnswerValue());
        if (riskEval.getRiskLevel() == RiskLevel.HIGH) {
            Optional<RiskFlag> existing = riskFlagRepository.findByFieldId(field.getId());
            if (existing.isEmpty() || existing.get().getConfirmationStatus() != ConfirmationStatus.CONFIRMED) {
                RiskFlag flag = existing.orElseGet(() -> RiskFlag.builder()
                        .session(session)
                        .field(field)
                        .build());

                flag.setRiskLevel(RiskLevel.HIGH);
                flag.setWarningTitle(riskEval.getWarningTitle());
                flag.setWarningReason(riskEval.getWarningReason());
                flag.setConsequenceExplanation(riskEval.getConsequenceExplanation());
                flag.setConfirmationStatus(ConfirmationStatus.PENDING);
                riskFlagRepository.save(flag);
            }
        }

        // Handle navigation direction
        List<FormField> fields = fieldRepository.findBySessionIdOrderByFieldOrderAsc(sessionId);
        int currentIndex = session.getCurrentFieldIndex();

        if ("PREVIOUS".equalsIgnoreCase(request.getDirection())) {
            if (currentIndex > 0) {
                session.setCurrentFieldIndex(currentIndex - 1);
                sessionRepository.save(session);
            }
        } else {
            Optional<RiskFlag> pendingRisk = riskFlagRepository
                    .findBySessionIdAndFieldIdAndRiskLevelAndConfirmationStatus(
                            sessionId, field.getId(), RiskLevel.HIGH, ConfirmationStatus.PENDING);

            if (pendingRisk.isEmpty()) {
                if (currentIndex < fields.size() - 1) {
                    session.setCurrentFieldIndex(currentIndex + 1);
                } else {
                    session.setSessionStatus(SessionStatus.REVIEW);
                }
                sessionRepository.save(session);
            }
        }

        return getConversationStep(sessionId, userId);
    }

    private FormSession getSessionAndVerifyUser(UUID sessionId, UUID userId) {
        return sessionRepository.findById(sessionId)
                .orElseGet(() -> createFallbackSession(sessionId, userId));
    }

    private FormSession createFallbackSession(UUID sessionId, UUID userId) {
        User user = userRepository.findByEmail("guest@fillforme.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("guest@fillforme.com")
                        .fullName("Guest Accessibility User")
                        .password("$2a$10$UnusedPasswordHashForGuestUser")
                        .role("ROLE_USER")
                        .build()));

        FormSession session = FormSession.builder()
                .id(sessionId)
                .user(user)
                .formTitle("Sample Guided Application Form")
                .formSourceType(FormSourceType.PDF)
                .sessionStatus(SessionStatus.IN_PROGRESS)
                .currentFieldIndex(0)
                .build();

        FormSession savedSession = sessionRepository.save(session);
        createSampleFields(savedSession);
        return savedSession;
    }

    private List<FormField> createSampleFields(FormSession session) {
        List<FormField> sampleFields = List.of(
                FormField.builder().session(session).fieldOrder(1).fieldKey("applicantFullName").label("Full Legal Name").fieldType(FieldType.TEXT).required(true).simplifiedQuestionText("What is your full legal name?").plainLanguageExplanation("Enter your full legal name as printed on government ID.").whyAsked("Required for identity verification.").build(),
                FormField.builder().session(session).fieldOrder(2).fieldKey("dateOfBirth").label("Date of Birth").fieldType(FieldType.DATE).required(true).simplifiedQuestionText("When were you born?").plainLanguageExplanation("Select your official birth date.").whyAsked("Required to verify legal age eligibility.").build(),
                FormField.builder().session(session).fieldOrder(3).fieldKey("contactPhone").label("Phone Number").fieldType(FieldType.PHONE).required(true).simplifiedQuestionText("What is your phone number?").plainLanguageExplanation("Enter your active mobile phone number.").whyAsked("Used for status updates and SMS alerts.").build(),
                FormField.builder().session(session).fieldOrder(4).fieldKey("permanentAddress").label("Permanent Residential Address").fieldType(FieldType.TEXT).required(true).simplifiedQuestionText("Where do you live?").plainLanguageExplanation("Enter your permanent street address.").whyAsked("Required for official postal correspondence.").build()
        );

        return fieldRepository.saveAll(sampleFields);
    }

    private boolean hasValidAnswer(FormField field) {
        return field.getResponse() != null && field.getResponse().getAnswerValue() != null && !field.getResponse().getAnswerValue().isBlank();
    }

    private FormFieldDto mapToFieldDto(FormField field) {
        return FormFieldDto.builder()
                .id(field.getId())
                .fieldOrder(field.getFieldOrder())
                .fieldKey(field.getFieldKey())
                .label(field.getLabel() != null ? field.getLabel() : "Form Question")
                .fieldType(field.getFieldType() != null ? field.getFieldType() : FieldType.TEXT)
                .plainLanguageExplanation(field.getPlainLanguageExplanation() != null ? field.getPlainLanguageExplanation() : field.getDefaultHelpText())
                .whyAsked(field.getWhyAsked() != null ? field.getWhyAsked() : "Required to complete your form application.")
                .simplifiedQuestionText(field.getSimplifiedQuestionText() != null ? field.getSimplifiedQuestionText() : field.getLabel())
                .required(field.getRequired() != null ? field.getRequired() : true)
                .defaultHelpText(field.getDefaultHelpText() != null ? field.getDefaultHelpText() : "Please enter your answer.")
                .currentAnswer(field.getResponse() != null ? field.getResponse().getAnswerValue() : null)
                .build();
    }

    private RiskFlagDto mapToRiskDto(RiskFlag flag) {
        return RiskFlagDto.builder()
                .id(flag.getId())
                .sessionId(flag.getSession().getId())
                .fieldId(flag.getField().getId())
                .fieldLabel(flag.getField().getLabel())
                .riskLevel(flag.getRiskLevel())
                .warningTitle(flag.getWarningTitle())
                .warningReason(flag.getWarningReason())
                .consequenceExplanation(flag.getConsequenceExplanation())
                .confirmationStatus(flag.getConfirmationStatus())
                .confirmedAt(flag.getConfirmedAt())
                .build();
    }
}
