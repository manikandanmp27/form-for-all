package com.fillforme.backend.risk.service;

import com.fillforme.backend.risk.entity.RiskLevel;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class RiskRuleEngine {

    private static final Pattern BANK_ACCOUNT_PATTERN = Pattern.compile(".*(bank|account|iban|account_no|acc_no).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern IFSC_PATTERN = Pattern.compile(".*(ifsc|routing|swift).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern NOMINEE_PATTERN = Pattern.compile(".*(nominee|beneficiary|claimant).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern LEGAL_DECLARATION_PATTERN = Pattern.compile(".*(declaration|consent|legal|agree|terms|waiver).*", Pattern.CASE_INSENSITIVE);

    public boolean isHighRiskField(String fieldKey, String label) {
        String combined = (fieldKey + " " + label).toLowerCase(Locale.ROOT);
        return BANK_ACCOUNT_PATTERN.matcher(combined).matches() ||
               IFSC_PATTERN.matcher(combined).matches() ||
               NOMINEE_PATTERN.matcher(combined).matches() ||
               LEGAL_DECLARATION_PATTERN.matcher(combined).matches();
    }

    public RiskLevel evaluateRuleRisk(String fieldKey, String label) {
        if (isHighRiskField(fieldKey, label)) {
            return RiskLevel.HIGH;
        }
        return RiskLevel.STANDARD;
    }
}
