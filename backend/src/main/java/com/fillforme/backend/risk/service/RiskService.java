package com.fillforme.backend.risk.service;

import com.fillforme.backend.common.exception.ResourceNotFoundException;
import com.fillforme.backend.common.exception.UnauthorizedAccessException;
import com.fillforme.backend.form.entity.FormSession;
import com.fillforme.backend.form.repository.FormSessionRepository;
import com.fillforme.backend.risk.dto.RiskFlagDto;
import com.fillforme.backend.risk.entity.ConfirmationStatus;
import com.fillforme.backend.risk.entity.RiskFlag;
import com.fillforme.backend.risk.repository.RiskFlagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RiskService {

    private final RiskFlagRepository riskFlagRepository;
    private final FormSessionRepository sessionRepository;

    public RiskService(RiskFlagRepository riskFlagRepository, FormSessionRepository sessionRepository) {
        this.riskFlagRepository = riskFlagRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public List<RiskFlagDto> getSessionRiskFlags(UUID sessionId, UUID userId) {
        verifySessionOwner(sessionId, userId);
        return riskFlagRepository.findBySessionId(sessionId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RiskFlagDto confirmRisk(UUID sessionId, UUID riskId, UUID userId, boolean confirmed) {
        verifySessionOwner(sessionId, userId);

        RiskFlag riskFlag = riskFlagRepository.findById(riskId)
                .orElseThrow(() -> new ResourceNotFoundException("Risk flag not found: " + riskId));

        if (!riskFlag.getSession().getId().equals(sessionId)) {
            throw new IllegalArgumentException("Risk flag does not belong to the specified session.");
        }

        if (confirmed) {
            riskFlag.setConfirmationStatus(ConfirmationStatus.CONFIRMED);
            riskFlag.setConfirmedAt(LocalDateTime.now());
        } else {
            riskFlag.setConfirmationStatus(ConfirmationStatus.REJECTED);
        }

        RiskFlag saved = riskFlagRepository.save(riskFlag);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public boolean hasPendingHighRiskFlags(UUID sessionId) {
        List<RiskFlag> pending = riskFlagRepository.findBySessionIdAndConfirmationStatus(sessionId, ConfirmationStatus.PENDING);
        return pending.stream().anyMatch(r -> r.getRiskLevel() == com.fillforme.backend.risk.entity.RiskLevel.HIGH);
    }

    private void verifySessionOwner(UUID sessionId, UUID userId) {
        FormSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Form session not found: " + sessionId));
        if (!session.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You do not have authorization to access this session's risk flags.");
        }
    }

    private RiskFlagDto mapToDto(RiskFlag flag) {
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
