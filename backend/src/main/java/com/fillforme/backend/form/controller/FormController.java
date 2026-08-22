package com.fillforme.backend.form.controller;

import com.fillforme.backend.common.security.UserPrincipal;
import com.fillforme.backend.form.dto.FormIngestionRequest;
import com.fillforme.backend.form.dto.FormSessionDto;
import com.fillforme.backend.form.service.FormService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/forms")
public class FormController {

    private final FormService formService;

    public FormController(FormService formService) {
        this.formService = formService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FormSessionDto> createFormFromFile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "formTitle", required = false) String formTitle) {
        UUID userId = userPrincipal != null ? userPrincipal.getId() : null;
        String effectiveTitle = title != null && !title.isBlank() ? title : formTitle;
        FormSessionDto dto = formService.createSessionFromFile(userId, file, effectiveTitle);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FormSessionDto> createFormFromUrl(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody FormIngestionRequest request) {
        UUID userId = userPrincipal != null ? userPrincipal.getId() : null;
        FormSessionDto dto = formService.createSessionFromUrl(userId, request.getFormUrl(), request.getFormTitle());
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FormSessionDto>> getUserSessions(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<FormSessionDto> sessions = formService.getUserSessions(userPrincipal.getId());
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<FormSessionDto> getSessionById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID sessionId) {
        FormSessionDto session = formService.getSessionById(sessionId, userPrincipal.getId());
        return ResponseEntity.ok(session);
    }
}
