package com.fillforme.backend.risk.dto;

public class RiskConfirmationRequest {
    private boolean confirmed;

    public RiskConfirmationRequest() {}

    public RiskConfirmationRequest(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
}
