package com.fillforme.backend.export.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResponseDto {
    private UUID sessionId;
    private String downloadUrl;
    private String filename;
    private String contentType;
    private long sizeBytes;
}
