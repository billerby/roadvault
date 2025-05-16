package com.billerby.roadvault.dto;

import com.billerby.roadvault.model.Billing;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Billing entities.
 */
public class BillingDTO {
    
    private Long id;
    private Integer year;
    private String description;
    private BigDecimal totalAmount;
    private BigDecimal extraCharge;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String type;
    private int invoiceCount;
    
    // Default constructor
    public BillingDTO() {
    }
    
    // Constructor with fields
    public BillingDTO(Long id, Integer year, String description, BigDecimal totalAmount, 
                     BigDecimal extraCharge, LocalDate issueDate, LocalDate dueDate, String type) {
        this.id = id;
        this.year = year;
        this.description = description;
        this.totalAmount = totalAmount;
        this.extraCharge = extraCharge;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.type = type;
    }
    
    // Factory method to create DTO from entity
    public static BillingDTO fromEntity(Billing billing) {
        BillingDTO dto = new BillingDTO();
        dto.setId(billing.getId());
        dto.setYear(billing.getYear());
        dto.setDescription(billing.getDescription());
        dto.setTotalAmount(billing.getTotalAmount());
        dto.setExtraCharge(billing.getExtraCharge());
        dto.setIssueDate(billing.getIssueDate());
        dto.setDueDate(billing.getDueDate());
        dto.setType(billing.getType() != null ? billing.getType().name() : null);
        
        if (billing.getInvoices() != null) {
            dto.setInvoiceCount(billing.getInvoices().size());
        }
        
        return dto;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getExtraCharge() {
        return extraCharge;
    }
    
    public void setExtraCharge(BigDecimal extraCharge) {
        this.extraCharge = extraCharge;
    }
    
    public LocalDate getIssueDate() {
        return issueDate;
    }
    
    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getInvoiceCount() {
        return invoiceCount;
    }
    
    public void setInvoiceCount(int invoiceCount) {
        this.invoiceCount = invoiceCount;
    }
}
