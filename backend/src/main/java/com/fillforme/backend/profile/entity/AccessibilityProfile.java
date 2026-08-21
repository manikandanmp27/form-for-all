package com.fillforme.backend.profile.entity;

import com.fillforme.backend.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accessibility_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessibilityProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(name = "preferred_language", nullable = false)
    private String preferredLanguage = "en";

    @Builder.Default
    @Column(name = "voice_preference", nullable = false)
    private Boolean voicePreference = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "cognitive_load_preference", nullable = false)
    private CognitiveLoadPreference cognitiveLoadPreference = CognitiveLoadPreference.STANDARD;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "accessibility_need", nullable = false)
    private AccessibilityNeed accessibilityNeed = AccessibilityNeed.NONE;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
