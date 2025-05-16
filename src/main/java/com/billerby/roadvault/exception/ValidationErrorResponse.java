package com.billerby.roadvault.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Validation error response DTO.
 */
public class ValidationErrorResponse extends ErrorResponse {
    
    private Map<String, String> errors;
    
    // Default constructor
    public ValidationErrorResponse() {
    }
    
    // Constructor with fields
    public ValidationErrorResponse(int status, String message, String path, LocalDateTime timestamp, Map<String, String> errors) {
        super(status, message, path, timestamp);
        this.errors = errors;
    }
    
    // Getters and Setters
    
    public Map<String, String> getErrors() {
        return errors;
    }
    
    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
