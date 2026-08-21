package com.fillforme.backend;

import com.fillforme.backend.auth.dto.AuthResponse;
import com.fillforme.backend.auth.dto.LoginRequest;
import com.fillforme.backend.auth.dto.RegisterRequest;
import com.fillforme.backend.auth.service.AuthService;
import com.fillforme.backend.conversation.dto.ConversationStepResponse;
import com.fillforme.backend.conversation.dto.SubmitAnswerRequest;
import com.fillforme.backend.conversation.service.ConversationService;
import com.fillforme.backend.export.dto.ReviewSummaryDto;
import com.fillforme.backend.export.service.FormExportService;
import com.fillforme.backend.form.dto.FormSessionDto;
import com.fillforme.backend.form.service.FormService;
import com.fillforme.backend.profile.dto.AccessibilityProfileDto;
import com.fillforme.backend.profile.entity.AccessibilityNeed;
import com.fillforme.backend.profile.entity.CognitiveLoadPreference;
import com.fillforme.backend.profile.service.AccessibilityProfileService;
import com.fillforme.backend.risk.dto.RiskFlagDto;
import com.fillforme.backend.risk.service.RiskService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

@SpringBootTest
class FormSessionIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AccessibilityProfileService profileService;

    @Autowired
    private FormService formService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private RiskService riskService;

    @Autowired
    private FormExportService exportService;

    @Test
    void testCompleteUserJourney() {
        // 1. Register user
        String email = "testuser_" + UUID.randomUUID() + "@example.com";
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword("securePassword123");
        registerRequest.setFullName("Test User");

        AuthResponse authResponse = authService.register(registerRequest);
        Assertions.assertNotNull(authResponse.getToken());
        UUID userId = authResponse.getUser().getId();

        // 2. Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("securePassword123");

        AuthResponse loginResponse = authService.login(loginRequest);
        Assertions.assertEquals(userId, loginResponse.getUser().getId());

        // 3. Update Accessibility Profile
        AccessibilityProfileDto profileDto = AccessibilityProfileDto.builder()
                .preferredLanguage("en")
                .voicePreference(true)
                .cognitiveLoadPreference(CognitiveLoadPreference.LOW)
                .accessibilityNeed(AccessibilityNeed.VISUAL)
                .build();

        AccessibilityProfileDto updatedProfile = profileService.updateProfile(userId, profileDto);
        Assertions.assertEquals(CognitiveLoadPreference.LOW, updatedProfile.getCognitiveLoadPreference());

        // 4. Create Form Session from URL
        FormSessionDto session = formService.createSessionFromUrl(userId, "https://example.com/forms/sample", "Sample Financial Form");
        Assertions.assertNotNull(session.getId());
        Assertions.assertTrue(session.getTotalFields() > 0);

        // 5. Retrieve Conversation Step
        ConversationStepResponse step1 = conversationService.getConversationStep(session.getId(), userId);
        Assertions.assertEquals(1, step1.getCurrentStep());
        Assertions.assertNotNull(step1.getCurrentField());

        // 6. Submit answer for standard field
        SubmitAnswerRequest ans1 = new SubmitAnswerRequest();
        ans1.setFieldId(step1.getCurrentField().getId());
        ans1.setAnswerValue("John Doe");
        ans1.setDirection("NEXT");

        ConversationStepResponse step2 = conversationService.submitAnswer(session.getId(), userId, ans1);
        Assertions.assertEquals(2, step2.getCurrentStep());

        // 7. Test Risk Flagging and Server Confirmation Enforcement
        List<RiskFlagDto> riskFlags = riskService.getSessionRiskFlags(session.getId(), userId);
        if (!riskFlags.isEmpty()) {
            RiskFlagDto risk = riskFlags.get(0);
            RiskFlagDto confirmedRisk = riskService.confirmRisk(session.getId(), risk.getId(), userId, true);
            Assertions.assertEquals(com.fillforme.backend.risk.entity.ConfirmationStatus.CONFIRMED, confirmedRisk.getConfirmationStatus());
        }

        // 8. Final Review Summary
        ReviewSummaryDto review = exportService.getReviewSummary(session.getId(), userId);
        Assertions.assertNotNull(review);
        Assertions.assertEquals(session.getId(), review.getSessionId());

        // 9. Generate PDF Export
        byte[] pdf = exportService.generatePdfDocument(session.getId(), userId);
        Assertions.assertTrue(pdf.length > 0);
    }
}
