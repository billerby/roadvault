package com.billerby.roadvault.dto;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO for Property entities.
 */
public class PropertyDTO {
    
    private Long id;
    private String propertyDesignation;
    private BigDecimal shareRatio;
    private String address;
    private Set<OwnerDTO> owners = new HashSet<>();
    private OwnerDTO mainContact;
    
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
    
    public Set<OwnerDTO> getOwners() {
        return owners;
    }
    
    public void setOwners(Set<OwnerDTO> owners) {
        this.owners = owners;
    }
    
    public OwnerDTO getMainContact() {
        return mainContact;
    }
    
    public void setMainContact(OwnerDTO mainContact) {
        this.mainContact = mainContact;
    }
}
