package com.fillforme.backend.form.repository;

import com.fillforme.backend.form.entity.FormField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormFieldRepository extends JpaRepository<FormField, UUID> {
    List<FormField> findBySessionIdOrderByFieldOrderAsc(UUID sessionId);
    Optional<FormField> findBySessionIdAndFieldOrder(UUID sessionId, Integer fieldOrder);
}
