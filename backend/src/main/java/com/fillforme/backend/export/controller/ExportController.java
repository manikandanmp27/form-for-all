package com.fillforme.backend.export.controller;

import com.fillforme.backend.common.security.UserPrincipal;
import com.fillforme.backend.export.dto.ReviewSummaryDto;
import com.fillforme.backend.export.service.FormExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sessions/{sessionId}")
public class ExportController {

    private final FormExportService exportService;

    public ExportController(FormExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/review")
    public ResponseEntity<ReviewSummaryDto> getReviewSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID sessionId) {
        ReviewSummaryDto summary = exportService.getReviewSummary(sessionId, userPrincipal.getId());
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/submit")
    public ResponseEntity<ReviewSummaryDto> submitForm(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID sessionId) {
        ReviewSummaryDto summary = exportService.submitForm(sessionId, userPrincipal.getId());
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportPdf(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID sessionId) {
        byte[] pdfBytes = exportService.generatePdfDocument(sessionId, userPrincipal.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"form_" + sessionId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
