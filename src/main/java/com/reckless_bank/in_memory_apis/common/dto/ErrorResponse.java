package com.reckless_bank.in_memory_apis.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standard error response structure for API errors
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime timestamp,
    
    int status,
    String error,
    String message,
    String path,
    String traceId,
    Map<String, Object> details,
    List<ValidationError> validationErrors
) {
    
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null, null, null);
    }
    
    public ErrorResponse(int status, String error, String message, String path, String traceId) {
        this(LocalDateTime.now(), status, error, message, path, traceId, null, null);
    }
    
    public ErrorResponse(int status, String error, String message, String path, Map<String, Object> details) {
        this(LocalDateTime.now(), status, error, message, path, null, details, null);
    }
    
    public ErrorResponse(int status, String error, String message, String path, List<ValidationError> validationErrors) {
        this(LocalDateTime.now(), status, error, message, path, null, null, validationErrors);
    }
    
    public record ValidationError(
        String field,
        Object rejectedValue,
        String message
    ) {}
}