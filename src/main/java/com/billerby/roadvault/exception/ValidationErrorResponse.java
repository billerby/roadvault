package com.billerby.roadvault.exception;

import java.util.Map;

/**
 * Error response for validation errors.
 */
public class ValidationErrorResponse extends ErrorResponse {
    
    private Map<String, String> validationErrors;
    
    // Default constructor
    public ValidationErrorResponse() {
    }
    
    // Constructor with validation errors
    public ValidationErrorResponse(int status, String errorCode, String message, String path, Map<String, String> validationErrors) {
        super(status, errorCode, message, path);
        this.validationErrors = validationErrors;
    }
    
    // Legacy constructor for backward compatibility
    public ValidationErrorResponse(int status, String message, String path, java.time.LocalDateTime timestamp, Map<String, String> validationErrors) {
        super(status, message, path, timestamp);
        this.validationErrors = validationErrors;
    }
    
    // Getters and Setters
    
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
    
    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
