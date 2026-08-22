package com.fillforme.backend.export.dto;

import java.util.UUID;

public class ExportResponseDto {
    private UUID sessionId;
    private String downloadUrl;
    private String filename;
    private String contentType;
    private long sizeBytes;

    public ExportResponseDto() {}

    public ExportResponseDto(UUID sessionId, String downloadUrl, String filename, String contentType, long sizeBytes) {
        this.sessionId = sessionId;
        this.downloadUrl = downloadUrl;
        this.filename = filename;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    public static ExportResponseDtoBuilder builder() {
        return new ExportResponseDtoBuilder();
    }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public static class ExportResponseDtoBuilder {
        private UUID sessionId;
        private String downloadUrl;
        private String filename;
        private String contentType;
        private long sizeBytes;

        ExportResponseDtoBuilder() {}

        public ExportResponseDtoBuilder sessionId(UUID sessionId) { this.sessionId = sessionId; return this; }
        public ExportResponseDtoBuilder downloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; return this; }
        public ExportResponseDtoBuilder filename(String filename) { this.filename = filename; return this; }
        public ExportResponseDtoBuilder contentType(String contentType) { this.contentType = contentType; return this; }
        public ExportResponseDtoBuilder sizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; return this; }

        public ExportResponseDto build() {
            return new ExportResponseDto(sessionId, downloadUrl, filename, contentType, sizeBytes);
        }
    }
}
