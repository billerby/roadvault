package com.billerby.roadvault.dto;

import com.billerby.roadvault.model.Invoice;

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
    
    // Factory method to create DTO from entity
    public static InvoiceDTO fromEntity(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        
        if (invoice.getBilling() != null) {
            dto.setBillingId(invoice.getBilling().getId());
            dto.setBillingDescription(invoice.getBilling().getDescription());
        }
        
        if (invoice.getProperty() != null) {
            dto.setPropertyId(invoice.getProperty().getId());
            dto.setPropertyDesignation(invoice.getProperty().getPropertyDesignation());
            
            if (invoice.getProperty().getMainContact() != null) {
                dto.setOwnerName(invoice.getProperty().getMainContact().getName());
            }
        }
        
        dto.setAmount(invoice.getAmount());
        dto.setDueDate(invoice.getDueDate());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setOcrNumber(invoice.getOcrNumber());
        dto.setStatus(invoice.getStatus() != null ? invoice.getStatus().name() : null);
        
        return dto;
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
