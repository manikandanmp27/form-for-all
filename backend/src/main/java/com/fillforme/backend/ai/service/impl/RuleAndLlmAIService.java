package com.fillforme.backend.ai.service.impl;

import com.fillforme.backend.ai.dto.AIFieldExplanation;
import com.fillforme.backend.ai.dto.AIRiskEvaluation;
import com.fillforme.backend.ai.service.AIService;
import com.fillforme.backend.profile.entity.AccessibilityProfile;
import com.fillforme.backend.profile.entity.CognitiveLoadPreference;
import com.fillforme.backend.risk.entity.RiskLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class RuleAndLlmAIService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(RuleAndLlmAIService.class);

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Override
    public AIFieldExplanation generateFieldExplanation(String fieldKey, String label, String helpText, AccessibilityProfile profile) {
        String lang = profile != null && profile.getPreferredLanguage() != null ? profile.getPreferredLanguage().toLowerCase() : "en";
        boolean isLowCognitive = profile != null && profile.getCognitiveLoadPreference() == CognitiveLoadPreference.LOW;

        String plainExplanation;
        String whyAsked;
        String simplifiedQuestion;

        String lowerLabel = label.toLowerCase(Locale.ROOT);

        if (lowerLabel.contains("bank") || lowerLabel.contains("account") || lowerLabel.contains("ifsc") || lowerLabel.contains("iban")) {
            plainExplanation = "This is where your money or direct benefits will be sent.";
            whyAsked = "The system needs this account details to route direct deposit payments securely to your bank.";
            simplifiedQuestion = "What is your bank account number?";
        } else if (lowerLabel.contains("nominee") || lowerLabel.contains("beneficiary")) {
            plainExplanation = "This person will receive your benefit or account funds if something happens to you.";
            whyAsked = "Legal regulations require naming a trusted person as your registered nominee.";
            simplifiedQuestion = "Who is your nominated beneficiary?";
        } else if (lowerLabel.contains("address") || lowerLabel.contains("residence")) {
            plainExplanation = "This means the physical place where you live right now.";
            whyAsked = "The form uses this address to verify your identity and send physical notices if needed.";
            simplifiedQuestion = "What is your permanent home address?";
        } else if (lowerLabel.contains("declaration") || lowerLabel.contains("consent") || lowerLabel.contains("agree")) {
            plainExplanation = "This is your legal statement confirming that all information supplied is honest and true.";
            whyAsked = "Legal forms require explicit user agreement before official processing.";
            simplifiedQuestion = "Do you confirm all provided details are true?";
        } else if (lowerLabel.contains("phone") || lowerLabel.contains("mobile") || lowerLabel.contains("contact")) {
            plainExplanation = "The phone number where you can receive security codes or updates.";
            whyAsked = "Used for two-factor authentication and urgent status notifications.";
            simplifiedQuestion = "What is your mobile phone number?";
        } else if (lowerLabel.contains("email")) {
            plainExplanation = "Your electronic email address for digital correspondence.";
            whyAsked = "Used to send electronic receipts, confirmations, and session recovery links.";
            simplifiedQuestion = "What is your primary email address?";
        } else {
            plainExplanation = helpText != null && !helpText.isBlank() ? helpText : "Please provide the requested information for " + label + ".";
            whyAsked = "This information is required by the form authority to process your application.";
            simplifiedQuestion = "What is your " + label + "?";
        }

        if (isLowCognitive) {
            plainExplanation = "Simple guidance: " + plainExplanation;
        }

        // Handle language indicator adaptation
        if ("hi".equals(lang)) {
            plainExplanation = "[हिंदी] " + plainExplanation;
            whyAsked = "[हिंदी] " + whyAsked;
            simplifiedQuestion = "[हिंदी] " + simplifiedQuestion;
        } else if ("ta".equals(lang)) {
            plainExplanation = "[தமிழ்] " + plainExplanation;
            whyAsked = "[தமிழ்] " + whyAsked;
            simplifiedQuestion = "[தமிழ்] " + simplifiedQuestion;
        } else if ("es".equals(lang)) {
            plainExplanation = "[Español] " + plainExplanation;
            whyAsked = "[Español] " + whyAsked;
            simplifiedQuestion = "[Español] " + simplifiedQuestion;
        }

        return AIFieldExplanation.builder()
                .fieldKey(fieldKey)
                .plainLanguageExplanation(plainExplanation)
                .whyAskedExplanation(whyAsked)
                .simplifiedQuestionText(simplifiedQuestion)
                .build();
    }

    @Override
    public AIRiskEvaluation evaluateRisk(String fieldKey, String label, String answerValue) {
        String combined = (fieldKey + " " + label).toLowerCase(Locale.ROOT);

        if (combined.contains("bank") || combined.contains("account") || combined.contains("ifsc") || combined.contains("iban")) {
            return AIRiskEvaluation.builder()
                    .riskLevel(RiskLevel.HIGH)
                    .warningTitle("High Risk: Financial Destination Change")
                    .warningReason("You are updating financial bank account details.")
                    .consequenceExplanation("If you submit an incorrect account number or IFSC code, future payments and benefit distributions will be misrouted or delayed.")
                    .build();
        }

        if (combined.contains("nominee") || combined.contains("beneficiary")) {
            return AIRiskEvaluation.builder()
                    .riskLevel(RiskLevel.HIGH)
                    .warningTitle("High Risk: Nominee Rights Update")
                    .warningReason("You are modifying the legal beneficiary for this session.")
                    .consequenceExplanation("Changing your nominee will overwrite previous beneficiary allocations and affect legal distribution rights upon claim.")
                    .build();
        }

        if (combined.contains("declaration") || combined.contains("consent") || combined.contains("legal")) {
            return AIRiskEvaluation.builder()
                    .riskLevel(RiskLevel.HIGH)
                    .warningTitle("High Risk: Irreversible Legal Declaration")
                    .warningReason("This field contains a legally binding statement of truth.")
                    .consequenceExplanation("Providing false statements in a legal declaration may invalidate your application and lead to legal penalties.")
                    .build();
        }

        return AIRiskEvaluation.builder()
                .riskLevel(RiskLevel.STANDARD)
                .warningTitle("Standard Information")
                .warningReason("Standard field entry.")
                .consequenceExplanation("Please review for typo corrections.")
                .build();
    }
}
