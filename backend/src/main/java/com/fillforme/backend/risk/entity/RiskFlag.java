package com.fillforme.backend.risk.entity;

import com.fillforme.backend.form.entity.FormField;
import com.fillforme.backend.form.entity.FormSession;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "risk_flags")
public class RiskFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private FormSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "field_id", nullable = false)
    private FormField field;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "warning_title", nullable = false)
    private String warningTitle;

    @Column(name = "warning_reason", columnDefinition = "TEXT", nullable = false)
    private String warningReason;

    @Column(name = "consequence_explanation", columnDefinition = "TEXT")
    private String consequenceExplanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "confirmation_status", nullable = false)
    private ConfirmationStatus confirmationStatus;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    public RiskFlag() {}

    public RiskFlag(UUID id, FormSession session, FormField field, RiskLevel riskLevel, String warningTitle, String warningReason, String consequenceExplanation, ConfirmationStatus confirmationStatus, LocalDateTime confirmedAt) {
        this.id = id;
        this.session = session;
        this.field = field;
        this.riskLevel = riskLevel;
        this.warningTitle = warningTitle;
        this.warningReason = warningReason;
        this.consequenceExplanation = consequenceExplanation;
        this.confirmationStatus = confirmationStatus;
        this.confirmedAt = confirmedAt;
    }

    public static RiskFlagBuilder builder() {
        return new RiskFlagBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public FormSession getSession() { return session; }
    public void setSession(FormSession session) { this.session = session; }

    public FormField getField() { return field; }
    public void setField(FormField field) { this.field = field; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getWarningTitle() { return warningTitle; }
    public void setWarningTitle(String warningTitle) { this.warningTitle = warningTitle; }

    public String getWarningReason() { return warningReason; }
    public void setWarningReason(String warningReason) { this.warningReason = warningReason; }

    public String getConsequenceExplanation() { return consequenceExplanation; }
    public void setConsequenceExplanation(String consequenceExplanation) { this.consequenceExplanation = consequenceExplanation; }

    public ConfirmationStatus getConfirmationStatus() { return confirmationStatus; }
    public void setConfirmationStatus(ConfirmationStatus confirmationStatus) { this.confirmationStatus = confirmationStatus; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public static class RiskFlagBuilder {
        private UUID id;
        private FormSession session;
        private FormField field;
        private RiskLevel riskLevel;
        private String warningTitle;
        private String warningReason;
        private String consequenceExplanation;
        private ConfirmationStatus confirmationStatus;
        private LocalDateTime confirmedAt;

        RiskFlagBuilder() {}

        public RiskFlagBuilder id(UUID id) { this.id = id; return this; }
        public RiskFlagBuilder session(FormSession session) { this.session = session; return this; }
        public RiskFlagBuilder field(FormField field) { this.field = field; return this; }
        public RiskFlagBuilder riskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; return this; }
        public RiskFlagBuilder warningTitle(String warningTitle) { this.warningTitle = warningTitle; return this; }
        public RiskFlagBuilder warningReason(String warningReason) { this.warningReason = warningReason; return this; }
        public RiskFlagBuilder consequenceExplanation(String consequenceExplanation) { this.consequenceExplanation = consequenceExplanation; return this; }
        public RiskFlagBuilder confirmationStatus(ConfirmationStatus confirmationStatus) { this.confirmationStatus = confirmationStatus; return this; }
        public RiskFlagBuilder confirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; return this; }

        public RiskFlag build() {
            return new RiskFlag(id, session, field, riskLevel, warningTitle, warningReason, consequenceExplanation, confirmationStatus, confirmedAt);
        }
    }
}
