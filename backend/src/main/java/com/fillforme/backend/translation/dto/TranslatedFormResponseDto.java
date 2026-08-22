package com.fillforme.backend.translation.dto;

import java.time.LocalDateTime;

public class TranslatedFormResponseDto {
    private boolean success;
    private String originalFilename;
    private String sourceLanguage;
    private String targetLanguage;
    private String imageUrl;
    private String pdfUrl;
    private int totalPages;
    private String message;
    private LocalDateTime timestamp;

    public TranslatedFormResponseDto() {}

    public TranslatedFormResponseDto(boolean success, String originalFilename, String sourceLanguage, String targetLanguage, String imageUrl, String pdfUrl, int totalPages, String message, LocalDateTime timestamp) {
        this.success = success;
        this.originalFilename = originalFilename;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.imageUrl = imageUrl;
        this.pdfUrl = pdfUrl;
        this.totalPages = totalPages;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public static class Builder {
        private boolean success;
        private String originalFilename;
        private String sourceLanguage;
        private String targetLanguage;
        private String imageUrl;
        private String pdfUrl;
        private int totalPages;
        private String message;
        private LocalDateTime timestamp;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder originalFilename(String originalFilename) {
            this.originalFilename = originalFilename;
            return this;
        }

        public Builder sourceLanguage(String sourceLanguage) {
            this.sourceLanguage = sourceLanguage;
            return this;
        }

        public Builder targetLanguage(String targetLanguage) {
            this.targetLanguage = targetLanguage;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder pdfUrl(String pdfUrl) {
            this.pdfUrl = pdfUrl;
            return this;
        }

        public Builder totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public TranslatedFormResponseDto build() {
            return new TranslatedFormResponseDto(success, originalFilename, sourceLanguage, targetLanguage, imageUrl, pdfUrl, totalPages, message, timestamp);
        }
    }
}
