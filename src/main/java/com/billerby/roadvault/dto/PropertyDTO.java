package com.billerby.roadvault.dto;

import java.math.BigDecimal;

/**
 * DTO for Property entities.
 */
public class PropertyDTO {
    
    private Long id;
    private String propertyDesignation;
    private BigDecimal shareRatio;
    private String address;
    private OwnerDTO owner;
    
    // Default constructor
    public PropertyDTO() {
    }
    
    // Constructor with fields
    public PropertyDTO(Long id, String propertyDesignation, BigDecimal shareRatio, String address) {
        this.id = id;
        this.propertyDesignation = propertyDesignation;
        this.shareRatio = shareRatio;
        this.address = address;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPropertyDesignation() {
        return propertyDesignation;
    }
    
    public void setPropertyDesignation(String propertyDesignation) {
        this.propertyDesignation = propertyDesignation;
    }
    
    public BigDecimal getShareRatio() {
        return shareRatio;
    }
    
    public void setShareRatio(BigDecimal shareRatio) {
        this.shareRatio = shareRatio;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public OwnerDTO getOwner() {
        return owner;
    }
    
    public void setOwner(OwnerDTO owner) {
        this.owner = owner;
    }
}
