package com.fillforme.backend.form.repository;

import com.fillforme.backend.form.entity.FormSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormSessionRepository extends JpaRepository<FormSession, UUID> {
    List<FormSession> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<FormSession> findByIdAndUserId(UUID id, UUID userId);
}
