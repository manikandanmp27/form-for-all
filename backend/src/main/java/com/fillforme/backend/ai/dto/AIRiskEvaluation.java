package com.fillforme.backend.ai.dto;

import com.fillforme.backend.risk.entity.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRiskEvaluation {
    private RiskLevel riskLevel;
    private String warningTitle;
    private String warningReason;
    private String consequenceExplanation;
}
