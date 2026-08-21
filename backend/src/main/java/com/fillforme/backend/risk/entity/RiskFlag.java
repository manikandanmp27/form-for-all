package com.fillforme.backend.risk.entity;

import com.fillforme.backend.form.entity.FormField;
import com.fillforme.backend.form.entity.FormSession;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "risk_flags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
