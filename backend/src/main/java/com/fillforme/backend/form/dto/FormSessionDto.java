package com.fillforme.backend.form.dto;

import com.fillforme.backend.form.entity.FormSourceType;
import com.fillforme.backend.form.entity.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormSessionDto {
    private UUID id;
    private String formTitle;
    private FormSourceType formSourceType;
    private String sourceUrl;
    private SessionStatus sessionStatus;
    private Integer currentFieldIndex;
    private Integer totalFields;
    private List<FormFieldDto> fields;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
