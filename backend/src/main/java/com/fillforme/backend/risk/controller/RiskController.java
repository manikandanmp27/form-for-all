package com.fillforme.backend.risk.controller;

import com.fillforme.backend.common.security.UserPrincipal;
import com.fillforme.backend.risk.dto.RiskConfirmationRequest;
import com.fillforme.backend.risk.dto.RiskFlagDto;
import com.fillforme.backend.risk.service.RiskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions/{sessionId}/risk")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping
    public ResponseEntity<List<RiskFlagDto>> getSessionRiskFlags(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID sessionId) {
        List<RiskFlagDto> flags = riskService.getSessionRiskFlags(sessionId, userPrincipal.getId());
        return ResponseEntity.ok(flags);
    }

    @PostMapping("/{riskId}/confirm")
    public ResponseEntity<RiskFlagDto> confirmRisk(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID sessionId,
            @PathVariable UUID riskId,
            @RequestBody RiskConfirmationRequest request) {
        RiskFlagDto dto = riskService.confirmRisk(sessionId, riskId, userPrincipal.getId(), request.isConfirmed());
        return ResponseEntity.ok(dto);
    }
}
