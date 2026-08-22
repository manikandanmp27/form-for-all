package com.fillforme.backend.export.service;

import com.fillforme.backend.common.exception.ResourceNotFoundException;
import com.fillforme.backend.common.exception.UnauthorizedAccessException;
import com.fillforme.backend.export.dto.ReviewSummaryDto;
import com.fillforme.backend.form.dto.FormFieldDto;
import com.fillforme.backend.form.entity.FormField;
import com.fillforme.backend.form.entity.FormSession;
import com.fillforme.backend.form.entity.SessionStatus;
import com.fillforme.backend.form.repository.FormFieldRepository;
import com.fillforme.backend.form.repository.FormSessionRepository;
import com.fillforme.backend.risk.dto.RiskFlagDto;
import com.fillforme.backend.risk.entity.ConfirmationStatus;
import com.fillforme.backend.risk.entity.RiskFlag;
import com.fillforme.backend.risk.entity.RiskLevel;
import com.fillforme.backend.risk.repository.RiskFlagRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FormExportService {

    private final FormSessionRepository sessionRepository;
    private final FormFieldRepository fieldRepository;
    private final RiskFlagRepository riskFlagRepository;

    public FormExportService(
            FormSessionRepository sessionRepository,
            FormFieldRepository fieldRepository,
            RiskFlagRepository riskFlagRepository) {
        this.sessionRepository = sessionRepository;
        this.fieldRepository = fieldRepository;
        this.riskFlagRepository = riskFlagRepository;
    }

    @Transactional(readOnly = true)
    public ReviewSummaryDto getReviewSummary(UUID sessionId, UUID userId) {
        FormSession session = getSessionAndVerifyOwner(sessionId, userId);
        List<FormField> fields = fieldRepository.findBySessionIdOrderByFieldOrderAsc(sessionId);
        List<RiskFlag> riskFlags = riskFlagRepository.findBySessionId(sessionId);

        List<String> missingRequiredFields = new ArrayList<>();
        int answeredCount = 0;

        for (FormField field : fields) {
            boolean hasAnswer = field.getResponse() != null && field.getResponse().getAnswerValue() != null && !field.getResponse().getAnswerValue().isBlank();
            if (hasAnswer) {
                answeredCount++;
            } else if (field.getRequired()) {
                missingRequiredFields.add(field.getLabel());
            }
        }

        List<RiskFlag> unconfirmedHighRisks = riskFlags.stream()
                .filter(r -> r.getRiskLevel() == RiskLevel.HIGH && r.getConfirmationStatus() == ConfirmationStatus.PENDING)
                .collect(Collectors.toList());

        boolean isReady = missingRequiredFields.isEmpty() && unconfirmedHighRisks.isEmpty();

        List<FormFieldDto> fieldDtos = fields.stream().map(this::mapToFieldDto).collect(Collectors.toList());
        List<RiskFlagDto> unconfirmedDtos = unconfirmedHighRisks.stream().map(this::mapToRiskDto).collect(Collectors.toList());

        return ReviewSummaryDto.builder()
                .sessionId(sessionId)
                .formTitle(session.getFormTitle())
                .sessionStatus(session.getSessionStatus())
                .totalFields(fields.size())
                .answeredFields(answeredCount)
                .isReadyForSubmission(isReady)
                .missingRequiredFields(missingRequiredFields)
                .hasUnconfirmedHighRiskFlags(!unconfirmedHighRisks.isEmpty())
                .unconfirmedRiskFlags(unconfirmedDtos)
                .fields(fieldDtos)
                .build();
    }

    @Transactional
    public ReviewSummaryDto submitForm(UUID sessionId, UUID userId) {
        FormSession session = getSessionAndVerifyOwner(sessionId, userId);
        session.setSessionStatus(SessionStatus.COMPLETED);
        sessionRepository.save(session);

        ReviewSummaryDto summary = getReviewSummary(sessionId, userId);
        summary.setSessionStatus(SessionStatus.COMPLETED);
        summary.setReadyForSubmission(true);
        return summary;
    }

    public byte[] generatePdfDocument(UUID sessionId, UUID userId) {
        FormSession session = getSessionAndVerifyOwner(sessionId, userId);
        List<FormField> fields = fieldRepository.findBySessionIdOrderByFieldOrderAsc(sessionId);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                float yPosition = 750;

                // Header
                contentStream.beginText();
                contentStream.setFont(fontBold, 18);
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText("Fill-For-Me Completed Form Summary");
                contentStream.endText();

                yPosition -= 25;
                contentStream.beginText();
                contentStream.setFont(fontRegular, 12);
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText("Form Title: " + session.getFormTitle());
                contentStream.endText();

                yPosition -= 18;
                contentStream.beginText();
                contentStream.setFont(fontRegular, 10);
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                contentStream.endText();

                yPosition -= 30;

                // Form Fields & Answers
                for (FormField field : fields) {
                    if (yPosition < 80) {
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        // Open new content stream for page
                        break;
                    }

                    String answer = field.getResponse() != null && field.getResponse().getAnswerValue() != null
                            ? field.getResponse().getAnswerValue() : "[Not Answered]";

                    contentStream.beginText();
                    contentStream.setFont(fontBold, 11);
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText(field.getFieldOrder() + ". " + field.getLabel() + ": ");
                    contentStream.setFont(fontRegular, 11);
                    contentStream.showText(answer);
                    contentStream.endText();

                    yPosition -= 16;
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF document", e);
        }
    }

    private FormSession getSessionAndVerifyOwner(UUID sessionId, UUID userId) {
        FormSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Form session not found: " + sessionId));
        if (userId != null && !session.getUser().getId().equals(userId) && !session.getUser().getEmail().equals("guest@fillforme.com")) {
            throw new UnauthorizedAccessException("You do not have permission to access this form session.");
        }
        return session;
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
