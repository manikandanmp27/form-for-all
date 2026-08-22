package com.fillforme.backend.common.exception;

public class RiskConfirmationRequiredException extends RuntimeException {
    private final String riskId;
    private final String warningReason;

    public RiskConfirmationRequiredException(String message, String riskId, String warningReason) {
        super(message);
        this.riskId = riskId;
        this.warningReason = warningReason;
    }

    public String getRiskId() {
        return riskId;
    }

    public String getWarningReason() {
        return warningReason;
    }
}
