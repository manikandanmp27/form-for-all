package com.fillforme.backend.profile.controller;

import com.fillforme.backend.common.security.UserPrincipal;
import com.fillforme.backend.profile.dto.AccessibilityProfileDto;
import com.fillforme.backend.profile.service.AccessibilityProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class AccessibilityProfileController {

    private final AccessibilityProfileService profileService;

    public AccessibilityProfileController(AccessibilityProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<AccessibilityProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UUID userId = userPrincipal != null ? userPrincipal.getId() : null;
        AccessibilityProfileDto dto = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping
    public ResponseEntity<AccessibilityProfileDto> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AccessibilityProfileDto dto) {
        UUID userId = userPrincipal != null ? userPrincipal.getId() : null;
        AccessibilityProfileDto updated = profileService.updateProfile(userId, dto);
        return ResponseEntity.ok(updated);
    }
}
