package com.billerby.roadvault.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Error response DTO.
 */
public class ErrorResponse {
    
    private int status;
    private String errorCode;
    private String message;
    private String path;
    private String timestamp;
    private String rootCause;
    private Map<String, String> validationErrors;
    
    // Default constructor
    public ErrorResponse() {
    }
    
    // Constructor with required fields
    public ErrorResponse(int status, String errorCode, String message, String path) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }
    
    // Constructor with all fields
    public ErrorResponse(int status, String errorCode, String message, String path, String rootCause) {
        this(status, errorCode, message, path);
        this.rootCause = rootCause;
    }
    
    // Constructor used for the legacy code (for backward compatibility)
    public ErrorResponse(int status, String message, String path, LocalDateTime timestamp) {
        this.status = status;
        this.errorCode = "Error" + status;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp.format(DateTimeFormatter.ISO_DATE_TIME);
    }
    
    // Getters and Setters
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getRootCause() {
        return rootCause;
    }
    
    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }
    
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
    
    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
