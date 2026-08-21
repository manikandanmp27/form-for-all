package com.fillforme.backend.form.repository;

import com.fillforme.backend.form.entity.FieldResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FieldResponseRepository extends JpaRepository<FieldResponse, UUID> {
    Optional<FieldResponse> findByFieldId(UUID fieldId);
    List<FieldResponse> findBySessionId(UUID sessionId);
}
