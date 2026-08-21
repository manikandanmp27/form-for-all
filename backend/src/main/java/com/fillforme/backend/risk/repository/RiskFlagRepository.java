package com.fillforme.backend.risk.repository;

import com.fillforme.backend.risk.entity.ConfirmationStatus;
import com.fillforme.backend.risk.entity.RiskFlag;
import com.fillforme.backend.risk.entity.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiskFlagRepository extends JpaRepository<RiskFlag, UUID> {
    List<RiskFlag> findBySessionId(UUID sessionId);
    Optional<RiskFlag> findByFieldId(UUID fieldId);
    List<RiskFlag> findBySessionIdAndConfirmationStatus(UUID sessionId, ConfirmationStatus status);
    Optional<RiskFlag> findBySessionIdAndFieldIdAndRiskLevelAndConfirmationStatus(
            UUID sessionId, UUID fieldId, RiskLevel riskLevel, ConfirmationStatus confirmationStatus);
}
