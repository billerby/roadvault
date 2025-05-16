package com.billerby.roadvault.dto;

import com.billerby.roadvault.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Payment entities.
 */
public class PaymentDTO {
    
    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String comment;
    private String paymentType;
    
    // Default constructor
    public PaymentDTO() {
    }
    
    // Constructor with fields
    public PaymentDTO(Long id, Long invoiceId, String invoiceNumber, BigDecimal amount, 
                      LocalDate paymentDate, String comment, String paymentType) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.comment = comment;
        this.paymentType = paymentType;
    }
    
    // Factory method to create DTO from entity
    public static PaymentDTO fromEntity(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        
        if (payment.getInvoice() != null) {
            dto.setInvoiceId(payment.getInvoice().getId());
            dto.setInvoiceNumber(payment.getInvoice().getInvoiceNumber());
        }
        
        dto.setAmount(payment.getAmount());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setComment(payment.getComment());
        dto.setPaymentType(payment.getPaymentType() != null ? payment.getPaymentType().name() : null);
        
        return dto;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getInvoiceId() {
        return invoiceId;
    }
    
    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDate getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
}
