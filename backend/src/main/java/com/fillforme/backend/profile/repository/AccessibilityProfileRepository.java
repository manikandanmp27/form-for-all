package com.fillforme.backend.profile.repository;

import com.fillforme.backend.profile.entity.AccessibilityProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessibilityProfileRepository extends JpaRepository<AccessibilityProfile, UUID> {
    Optional<AccessibilityProfile> findByUserId(UUID userId);
}
