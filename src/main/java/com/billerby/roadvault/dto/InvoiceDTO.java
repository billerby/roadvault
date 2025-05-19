package com.billerby.roadvault.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Invoice entities.
 */
public class InvoiceDTO {
    
    private Long id;
    private Long billingId;
    private String billingDescription;
    private Long propertyId;
    private String propertyDesignation;
    private String ownerName;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String invoiceNumber;
    private String ocrNumber;
    private String status;
    
    // Default constructor
    public InvoiceDTO() {
    }
    
    // Constructor with fields
    public InvoiceDTO(Long id, Long billingId, String billingDescription, Long propertyId, 
                      String propertyDesignation, String ownerName, BigDecimal amount, 
                      LocalDate dueDate, String invoiceNumber, String ocrNumber, String status) {
        this.id = id;
        this.billingId = billingId;
        this.billingDescription = billingDescription;
        this.propertyId = propertyId;
        this.propertyDesignation = propertyDesignation;
        this.ownerName = ownerName;
        this.amount = amount;
        this.dueDate = dueDate;
        this.invoiceNumber = invoiceNumber;
        this.ocrNumber = ocrNumber;
        this.status = status;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getBillingId() {
        return billingId;
    }
    
    public void setBillingId(Long billingId) {
        this.billingId = billingId;
    }
    
    public String getBillingDescription() {
        return billingDescription;
    }
    
    public void setBillingDescription(String billingDescription) {
        this.billingDescription = billingDescription;
    }
    
    public Long getPropertyId() {
        return propertyId;
    }
    
    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }
    
    public String getPropertyDesignation() {
        return propertyDesignation;
    }
    
    public void setPropertyDesignation(String propertyDesignation) {
        this.propertyDesignation = propertyDesignation;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public String getOcrNumber() {
        return ocrNumber;
    }
    
    public void setOcrNumber(String ocrNumber) {
        this.ocrNumber = ocrNumber;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
