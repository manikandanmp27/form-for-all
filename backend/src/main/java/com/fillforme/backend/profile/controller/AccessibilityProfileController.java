package com.fillforme.backend.profile.controller;

import com.fillforme.backend.common.security.UserPrincipal;
import com.fillforme.backend.profile.dto.AccessibilityProfileDto;
import com.fillforme.backend.profile.service.AccessibilityProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class AccessibilityProfileController {

    private final AccessibilityProfileService profileService;

    public AccessibilityProfileController(AccessibilityProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<AccessibilityProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        AccessibilityProfileDto dto = profileService.getProfileByUserId(userPrincipal.getId());
        return ResponseEntity.ok(dto);
    }

    @PutMapping
    public ResponseEntity<AccessibilityProfileDto> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AccessibilityProfileDto dto) {
        AccessibilityProfileDto updated = profileService.updateProfile(userPrincipal.getId(), dto);
        return ResponseEntity.ok(updated);
    }
}
