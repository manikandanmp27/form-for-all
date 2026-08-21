package com.fillforme.backend.export.dto;

import com.fillforme.backend.form.dto.FormFieldDto;
import com.fillforme.backend.form.entity.SessionStatus;
import com.fillforme.backend.risk.dto.RiskFlagDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDto {
    private UUID sessionId;
    private String formTitle;
    private SessionStatus sessionStatus;
    private int totalFields;
    private int answeredFields;
    private boolean isReadyForSubmission;
    private List<String> missingRequiredFields;
    private boolean hasUnconfirmedHighRiskFlags;
    private List<RiskFlagDto> unconfirmedRiskFlags;
    private List<FormFieldDto> fields;
}
