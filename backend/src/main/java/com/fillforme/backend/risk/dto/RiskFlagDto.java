package com.fillforme.backend.risk.dto;

import com.fillforme.backend.risk.entity.ConfirmationStatus;
import com.fillforme.backend.risk.entity.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskFlagDto {
    private UUID id;
    private UUID sessionId;
    private UUID fieldId;
    private String fieldLabel;
    private RiskLevel riskLevel;
    private String warningTitle;
    private String warningReason;
    private String consequenceExplanation;
    private ConfirmationStatus confirmationStatus;
    private LocalDateTime confirmedAt;
}
