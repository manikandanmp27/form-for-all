package com.fillforme.backend.conversation.service;

import com.fillforme.backend.ai.dto.AIRiskEvaluation;
import com.fillforme.backend.ai.service.AIService;
import com.fillforme.backend.common.exception.ResourceNotFoundException;
import com.fillforme.backend.common.exception.UnauthorizedAccessException;
import com.fillforme.backend.conversation.dto.ConversationStepResponse;
import com.fillforme.backend.conversation.dto.SubmitAnswerRequest;
import com.fillforme.backend.form.dto.FormFieldDto;
import com.fillforme.backend.form.entity.FieldResponse;
import com.fillforme.backend.form.entity.FormField;
import com.fillforme.backend.form.entity.FormSession;
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
    private final AIService aiService;

    public ConversationService(
            FormSessionRepository sessionRepository,
            FormFieldRepository fieldRepository,
            FieldResponseRepository responseRepository,
            RiskFlagRepository riskFlagRepository,
            AIService aiService) {
        this.sessionRepository = sessionRepository;
        this.fieldRepository = fieldRepository;
        this.responseRepository = responseRepository;
        this.riskFlagRepository = riskFlagRepository;
        this.aiService = aiService;
    }

    @Transactional(readOnly = true)
    public ConversationStepResponse getConversationStep(UUID sessionId, UUID userId) {
        FormSession session = getSessionAndVerifyUser(sessionId, userId);
        List<FormField> fields = fieldRepository.findBySessionIdOrderByFieldOrderAsc(sessionId);

        if (fields.isEmpty()) {
            throw new ResourceNotFoundException("No fields found for form session: " + sessionId);
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
                .orElseThrow(() -> new ResourceNotFoundException("Field not found: " + request.getFieldId()));

        if (!field.getSession().getId().equals(sessionId)) {
            throw new IllegalArgumentException("Field does not belong to session.");
        }

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
            // Check if current field risk blocks progression
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
        FormSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Form session not found: " + sessionId));

        if (userId != null && !session.getUser().getId().equals(userId) && !session.getUser().getEmail().equals("guest@fillforme.com")) {
            throw new UnauthorizedAccessException("You do not have permission to access this session.");
        }
        return session;
    }

    private boolean hasValidAnswer(FormField field) {
        return field.getResponse() != null && field.getResponse().getAnswerValue() != null && !field.getResponse().getAnswerValue().isBlank();
    }

    private FormFieldDto mapToFieldDto(FormField field) {
        return FormFieldDto.builder()
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
