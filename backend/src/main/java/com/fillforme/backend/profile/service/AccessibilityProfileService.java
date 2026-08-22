package com.fillforme.backend.profile.service;

import com.fillforme.backend.auth.entity.User;
import com.fillforme.backend.auth.repository.UserRepository;
import com.fillforme.backend.profile.dto.AccessibilityProfileDto;
import com.fillforme.backend.profile.entity.AccessibilityNeed;
import com.fillforme.backend.profile.entity.AccessibilityProfile;
import com.fillforme.backend.profile.entity.CognitiveLoadPreference;
import com.fillforme.backend.profile.repository.AccessibilityProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccessibilityProfileService {

    private final AccessibilityProfileRepository profileRepository;
    private final UserRepository userRepository;

    public AccessibilityProfileService(AccessibilityProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AccessibilityProfileDto getProfileByUserId(UUID userId) {
        User user = resolveUser(userId);
        AccessibilityProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultProfileForUser(user.getId()));

        return mapToDto(profile);
    }

    @Transactional
    public AccessibilityProfileDto updateProfile(UUID userId, AccessibilityProfileDto dto) {
        User user = resolveUser(userId);
        AccessibilityProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> AccessibilityProfile.builder().user(user).build());

        profile.setPreferredLanguage(dto.getPreferredLanguage() != null ? dto.getPreferredLanguage() : "en");
        profile.setVoicePreference(dto.getVoicePreference() != null ? dto.getVoicePreference() : false);
        profile.setCognitiveLoadPreference(dto.getCognitiveLoadPreference() != null ? dto.getCognitiveLoadPreference() : CognitiveLoadPreference.STANDARD);
        profile.setAccessibilityNeed(dto.getAccessibilityNeed() != null ? dto.getAccessibilityNeed() : AccessibilityNeed.NONE);

        AccessibilityProfile saved = profileRepository.save(profile);
        return mapToDto(saved);
    }

    @Transactional
    public AccessibilityProfile createDefaultProfileForUser(UUID userId) {
        User user = resolveUser(userId);

        AccessibilityProfile defaultProfile = AccessibilityProfile.builder()
                .user(user)
                .preferredLanguage("en")
                .voicePreference(false)
                .cognitiveLoadPreference(CognitiveLoadPreference.STANDARD)
                .accessibilityNeed(AccessibilityNeed.NONE)
                .build();

        return profileRepository.save(defaultProfile);
    }

    private User resolveUser(UUID userId) {
        if (userId != null) {
            return userRepository.findById(userId).orElseGet(this::getOrCreateDemoUser);
        }
        return getOrCreateDemoUser();
    }

    private User getOrCreateDemoUser() {
        return userRepository.findByEmail("guest@fillforme.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("guest@fillforme.com")
                        .fullName("Guest Accessibility User")
                        .password("$2a$10$UnusedPasswordHashForGuestUserInFillForMe")
                        .build()));
    }

    private AccessibilityProfileDto mapToDto(AccessibilityProfile profile) {
        return AccessibilityProfileDto.builder()
                .id(profile.getId())
                .preferredLanguage(profile.getPreferredLanguage())
                .voicePreference(profile.getVoicePreference())
                .cognitiveLoadPreference(profile.getCognitiveLoadPreference())
                .accessibilityNeed(profile.getAccessibilityNeed())
                .build();
    }
}
