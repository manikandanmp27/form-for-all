package com.fillforme.backend.common.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> details;

    public ErrorResponse() {}

    public ErrorResponse(int status, String error, String message, LocalDateTime timestamp, Map<String, String> details) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.details = details;
    }

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Map<String, String> getDetails() { return details; }
    public void setDetails(Map<String, String> details) { this.details = details; }

    public static class ErrorResponseBuilder {
        private int status;
        private String error;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, String> details;

        ErrorResponseBuilder() {}

        public ErrorResponseBuilder status(int status) { this.status = status; return this; }
        public ErrorResponseBuilder error(String error) { this.error = error; return this; }
        public ErrorResponseBuilder message(String message) { this.message = message; return this; }
        public ErrorResponseBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public ErrorResponseBuilder details(Map<String, String> details) { this.details = details; return this; }

        public ErrorResponse build() {
            return new ErrorResponse(status, error, message, timestamp, details);
        }
    }
}
