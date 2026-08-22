package com.fillforme.backend.profile.entity;

import com.fillforme.backend.auth.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accessibility_profiles")
public class AccessibilityProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "preferred_language", nullable = false)
    private String preferredLanguage = "en";

    @Column(name = "voice_preference", nullable = false)
    private Boolean voicePreference = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "cognitive_load_preference", nullable = false)
    private CognitiveLoadPreference cognitiveLoadPreference = CognitiveLoadPreference.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(name = "accessibility_need", nullable = false)
    private AccessibilityNeed accessibilityNeed = AccessibilityNeed.NONE;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AccessibilityProfile() {}

    public AccessibilityProfile(UUID id, User user, String preferredLanguage, Boolean voicePreference, CognitiveLoadPreference cognitiveLoadPreference, AccessibilityNeed accessibilityNeed, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.preferredLanguage = preferredLanguage != null ? preferredLanguage : "en";
        this.voicePreference = voicePreference != null ? voicePreference : false;
        this.cognitiveLoadPreference = cognitiveLoadPreference != null ? cognitiveLoadPreference : CognitiveLoadPreference.STANDARD;
        this.accessibilityNeed = accessibilityNeed != null ? accessibilityNeed : AccessibilityNeed.NONE;
        this.updatedAt = updatedAt;
    }

    public static AccessibilityProfileBuilder builder() {
        return new AccessibilityProfileBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public Boolean getVoicePreference() { return voicePreference; }
    public void setVoicePreference(Boolean voicePreference) { this.voicePreference = voicePreference; }

    public CognitiveLoadPreference getCognitiveLoadPreference() { return cognitiveLoadPreference; }
    public void setCognitiveLoadPreference(CognitiveLoadPreference cognitiveLoadPreference) { this.cognitiveLoadPreference = cognitiveLoadPreference; }

    public AccessibilityNeed getAccessibilityNeed() { return accessibilityNeed; }
    public void setAccessibilityNeed(AccessibilityNeed accessibilityNeed) { this.accessibilityNeed = accessibilityNeed; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class AccessibilityProfileBuilder {
        private UUID id;
        private User user;
        private String preferredLanguage = "en";
        private Boolean voicePreference = false;
        private CognitiveLoadPreference cognitiveLoadPreference = CognitiveLoadPreference.STANDARD;
        private AccessibilityNeed accessibilityNeed = AccessibilityNeed.NONE;
        private LocalDateTime updatedAt;

        AccessibilityProfileBuilder() {}

        public AccessibilityProfileBuilder id(UUID id) { this.id = id; return this; }
        public AccessibilityProfileBuilder user(User user) { this.user = user; return this; }
        public AccessibilityProfileBuilder preferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; return this; }
        public AccessibilityProfileBuilder voicePreference(Boolean voicePreference) { this.voicePreference = voicePreference; return this; }
        public AccessibilityProfileBuilder cognitiveLoadPreference(CognitiveLoadPreference cognitiveLoadPreference) { this.cognitiveLoadPreference = cognitiveLoadPreference; return this; }
        public AccessibilityProfileBuilder accessibilityNeed(AccessibilityNeed accessibilityNeed) { this.accessibilityNeed = accessibilityNeed; return this; }
        public AccessibilityProfileBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public AccessibilityProfile build() {
            return new AccessibilityProfile(id, user, preferredLanguage, voicePreference, cognitiveLoadPreference, accessibilityNeed, updatedAt);
        }
    }
}
