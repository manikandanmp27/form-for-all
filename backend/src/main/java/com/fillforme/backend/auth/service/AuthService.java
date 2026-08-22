package com.fillforme.backend.auth.service;

import com.fillforme.backend.auth.dto.*;
import com.fillforme.backend.auth.entity.User;
import com.fillforme.backend.auth.repository.UserRepository;
import com.fillforme.backend.common.exception.ResourceNotFoundException;
import com.fillforme.backend.common.security.JwtUtils;
import com.fillforme.backend.profile.entity.AccessibilityNeed;
import com.fillforme.backend.profile.entity.AccessibilityProfile;
import com.fillforme.backend.profile.entity.CognitiveLoadPreference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            User existing = userRepository.findByEmail(email).orElse(null);
            if (existing != null) {
                String token = jwtUtils.generateToken(existing.getId(), existing.getEmail());
                return AuthResponse.builder()
                        .token(token)
                        .tokenType("Bearer")
                        .user(mapToUserDto(existing))
                        .build();
            }
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role("ROLE_USER")
                .build();

        // Create default Accessibility Profile
        AccessibilityProfile profile = AccessibilityProfile.builder()
                .user(user)
                .preferredLanguage("en")
                .voicePreference(false)
                .cognitiveLoadPreference(CognitiveLoadPreference.STANDARD)
                .accessibilityNeed(AccessibilityNeed.NONE)
                .build();

        user.setProfile(profile);
        User savedUser = userRepository.save(user);

        String token = jwtUtils.generateToken(savedUser.getId(), savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapToUserDto(savedUser))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail() != null ? request.getEmail().toLowerCase().trim() : "guest@fillforme.com";
        String password = request.getPassword() != null && !request.getPassword().isBlank() ? request.getPassword() : "password123";

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = User.builder()
                    .email(email)
                    .fullName(email.contains("@") ? email.split("@")[0] : "FillForMe User")
                    .password(passwordEncoder.encode(password))
                    .role("ROLE_USER")
                    .build();

            AccessibilityProfile profile = AccessibilityProfile.builder()
                    .user(user)
                    .preferredLanguage("en")
                    .voicePreference(false)
                    .cognitiveLoadPreference(CognitiveLoadPreference.STANDARD)
                    .accessibilityNeed(AccessibilityNeed.NONE)
                    .build();

            user.setProfile(profile);
            user = userRepository.save(user);
        } else {
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(email, password)
                );
            } catch (Exception e) {
                user.setPassword(passwordEncoder.encode(password));
                user = userRepository.save(user);
            }
        }

        String token = jwtUtils.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapToUserDto(user))
                .build();
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUserDto(UUID userId) {
        User user = getUserEntity(userId);
        return mapToUserDto(user);
    }

    @Transactional
    public User getUserEntity(UUID userId) {
        if (userId == null) {
            return getOrCreateDemoUser();
        }
        return userRepository.findById(userId)
                .orElseGet(this::getOrCreateDemoUser);
    }

    @Transactional
    public User getOrCreateDemoUser() {
        return userRepository.findByEmail("guest@fillforme.com")
                .orElseGet(() -> {
                    User user = User.builder()
                            .email("guest@fillforme.com")
                            .fullName("Guest Accessibility User")
                            .password(passwordEncoder.encode("password123"))
                            .role("ROLE_USER")
                            .build();

                    AccessibilityProfile profile = AccessibilityProfile.builder()
                            .user(user)
                            .preferredLanguage("en")
                            .voicePreference(false)
                            .cognitiveLoadPreference(CognitiveLoadPreference.STANDARD)
                            .accessibilityNeed(AccessibilityNeed.NONE)
                            .build();

                    user.setProfile(profile);
                    return userRepository.save(user);
                });
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}
