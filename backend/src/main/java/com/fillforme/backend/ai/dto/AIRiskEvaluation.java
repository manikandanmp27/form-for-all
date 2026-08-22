package com.fillforme.backend.ai.dto;

import com.fillforme.backend.risk.entity.RiskLevel;

public class AIRiskEvaluation {
    private RiskLevel riskLevel;
    private String warningTitle;
    private String warningReason;
    private String consequenceExplanation;

    public AIRiskEvaluation() {}

    public AIRiskEvaluation(RiskLevel riskLevel, String warningTitle, String warningReason, String consequenceExplanation) {
        this.riskLevel = riskLevel;
        this.warningTitle = warningTitle;
        this.warningReason = warningReason;
        this.consequenceExplanation = consequenceExplanation;
    }

    public static AIRiskEvaluationBuilder builder() {
        return new AIRiskEvaluationBuilder();
    }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getWarningTitle() { return warningTitle; }
    public void setWarningTitle(String warningTitle) { this.warningTitle = warningTitle; }

    public String getWarningReason() { return warningReason; }
    public void setWarningReason(String warningReason) { this.warningReason = warningReason; }

    public String getConsequenceExplanation() { return consequenceExplanation; }
    public void setConsequenceExplanation(String consequenceExplanation) { this.consequenceExplanation = consequenceExplanation; }

    public static class AIRiskEvaluationBuilder {
        private RiskLevel riskLevel;
        private String warningTitle;
        private String warningReason;
        private String consequenceExplanation;

        AIRiskEvaluationBuilder() {}

        public AIRiskEvaluationBuilder riskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; return this; }
        public AIRiskEvaluationBuilder warningTitle(String warningTitle) { this.warningTitle = warningTitle; return this; }
        public AIRiskEvaluationBuilder warningReason(String warningReason) { this.warningReason = warningReason; return this; }
        public AIRiskEvaluationBuilder consequenceExplanation(String consequenceExplanation) { this.consequenceExplanation = consequenceExplanation; return this; }

        public AIRiskEvaluation build() {
            return new AIRiskEvaluation(riskLevel, warningTitle, warningReason, consequenceExplanation);
        }
    }
}
