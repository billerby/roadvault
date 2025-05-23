package com.billerby.roadvault.dto;

/**
 * DTO for change password response.
 */
public class ChangePasswordResponse {
    
    private String message;
    private boolean success;
    
    public ChangePasswordResponse() {
    }
    
    public ChangePasswordResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
