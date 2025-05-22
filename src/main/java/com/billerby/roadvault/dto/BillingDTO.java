package com.billerby.roadvault.dto;

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
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String type;
    private int invoiceCount;
    
    // Default constructor
    public BillingDTO() {
    }
    
    // Constructor with fields
    public BillingDTO(Long id, Integer year, String description, BigDecimal totalAmount, 
                     LocalDate issueDate, LocalDate dueDate, String type) {
        this.id = id;
        this.year = year;
        this.description = description;
        this.totalAmount = totalAmount;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.type = type;
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
