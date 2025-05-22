package com.billerby.roadvault.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Composite DTO for generating invoice PDFs using FreeMarker templates.
 * This combines data from InvoiceDTO, BillingDTO, AssociationDTO, OwnerDTO, etc.
 */
public class InvoicePdfDTO {
    
    // Association information
    private String associationName;
    private String associationAddress;
    private String associationPostalCode;
    private String associationCity;
    private String associationPhone;
    private String associationEmail;
    private String orgNumber;
    private String bankgiro;
    private String plusgiro;
    private String website;
    
    // Recipient information
    private String recipientName;
    private String recipientStreet;
    private String recipientPostalCode;
    private String recipientCity;
    private String recipientEmail;
    
    // Invoice details
    private String invoiceNumber;
    private String invoiceDate;
    private String dueDate;
    private String reference;
    private String propertyDesignation;
    private String paymentReference; // OCR number
    
    // Financial information
    private String subtotal;
    private String vat;
    private String total;
    private String additionalInfo; // Custom text from Association's invoiceText
    
    // Invoice items
    private List<InvoiceItemPdfDTO> items = new ArrayList<>();
    
    // Static factory method to create from existing DTOs
    public static InvoicePdfDTO from(InvoiceDTO invoice, BillingDTO billing, AssociationDTO association, OwnerDTO owner) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        InvoicePdfDTO pdfDTO = new InvoicePdfDTO();
        
        // Association information
        pdfDTO.setAssociationName(association.getName());
        pdfDTO.setAssociationAddress(association.getAddress());
        pdfDTO.setAssociationPostalCode(association.getPostalCode());
        pdfDTO.setAssociationCity(association.getCity());
        pdfDTO.setAssociationPhone(association.getPhone());
        pdfDTO.setAssociationEmail(association.getEmail());
        pdfDTO.setOrgNumber(association.getOrganizationNumber());
        pdfDTO.setBankgiro(association.getBankgiroNumber());
        pdfDTO.setPlusgiro(association.getPlusgiroNumber());
        pdfDTO.setWebsite(association.getWebsite());
        pdfDTO.setAdditionalInfo(association.getInvoiceText());
        
        // Recipient information
        pdfDTO.setRecipientName(owner.getName());
        pdfDTO.setRecipientStreet(owner.getAddress());
        pdfDTO.setRecipientPostalCode(owner.getPostalCode());
        pdfDTO.setRecipientCity(owner.getCity());
        pdfDTO.setRecipientEmail(owner.getEmail());
        
        // Invoice details
        pdfDTO.setInvoiceNumber(invoice.getInvoiceNumber());
        pdfDTO.setInvoiceDate(LocalDate.now().format(formatter)); // Current date as invoice date
        pdfDTO.setDueDate(invoice.getDueDate().format(formatter));
        pdfDTO.setReference(owner.getName()); // Default to owner name as reference
        pdfDTO.setPropertyDesignation(invoice.getPropertyDesignation());
        pdfDTO.setPaymentReference(invoice.getOcrNumber());
        
        // Financial information (in this simple example, no VAT calculation)
        pdfDTO.setSubtotal(formatCurrency(invoice.getAmount()));
        pdfDTO.setVat("0,00 kr"); // Default to 0
        pdfDTO.setTotal(formatCurrency(invoice.getAmount()));
        
        // Create a single invoice item from the billing description
        InvoiceItemPdfDTO item = new InvoiceItemPdfDTO();
        item.setDescription(billing.getDescription());
        item.setQuantity("1");
        item.setUnitPrice(formatCurrency(invoice.getAmount()));
        item.setVatRate("0%");
        item.setAmount(formatCurrency(invoice.getAmount()));
        pdfDTO.getItems().add(item);
        
        return pdfDTO;
    }
    
    // Helper method to format currency
    private static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0,00 kr";
        }
        // Format with Swedish locale (comma as decimal separator)
        return amount.toString().replace(".", ",") + " kr";
    }
    
    // Nested class for invoice line items
    public static class InvoiceItemPdfDTO {
        private String description;
        private String quantity;
        private String unitPrice;
        private String vatRate;
        private String amount;
        
        // Getters and setters
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getQuantity() {
            return quantity;
        }
        
        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }
        
        public String getUnitPrice() {
            return unitPrice;
        }
        
        public void setUnitPrice(String unitPrice) {
            this.unitPrice = unitPrice;
        }
        
        public String getVatRate() {
            return vatRate;
        }
        
        public void setVatRate(String vatRate) {
            this.vatRate = vatRate;
        }
        
        public String getAmount() {
            return amount;
        }
        
        public void setAmount(String amount) {
            this.amount = amount;
        }
    }
    
    // Getters and setters
    public String getAssociationName() {
        return associationName;
    }
    
    public void setAssociationName(String associationName) {
        this.associationName = associationName;
    }
    
    public String getAssociationAddress() {
        return associationAddress;
    }
    
    public void setAssociationAddress(String associationAddress) {
        this.associationAddress = associationAddress;
    }
    
    public String getAssociationPostalCode() {
        return associationPostalCode;
    }
    
    public void setAssociationPostalCode(String associationPostalCode) {
        this.associationPostalCode = associationPostalCode;
    }
    
    public String getAssociationCity() {
        return associationCity;
    }
    
    public void setAssociationCity(String associationCity) {
        this.associationCity = associationCity;
    }
    
    public String getAssociationPhone() {
        return associationPhone;
    }
    
    public void setAssociationPhone(String associationPhone) {
        this.associationPhone = associationPhone;
    }
    
    public String getAssociationEmail() {
        return associationEmail;
    }
    
    public void setAssociationEmail(String associationEmail) {
        this.associationEmail = associationEmail;
    }
    
    public String getOrgNumber() {
        return orgNumber;
    }
    
    public void setOrgNumber(String orgNumber) {
        this.orgNumber = orgNumber;
    }
    
    public String getBankgiro() {
        return bankgiro;
    }
    
    public void setBankgiro(String bankgiro) {
        this.bankgiro = bankgiro;
    }
    
    public String getPlusgiro() {
        return plusgiro;
    }
    
    public void setPlusgiro(String plusgiro) {
        this.plusgiro = plusgiro;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    
    public String getRecipientName() {
        return recipientName;
    }
    
    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
    
    public String getRecipientStreet() {
        return recipientStreet;
    }
    
    public void setRecipientStreet(String recipientStreet) {
        this.recipientStreet = recipientStreet;
    }
    
    public String getRecipientPostalCode() {
        return recipientPostalCode;
    }
    
    public void setRecipientPostalCode(String recipientPostalCode) {
        this.recipientPostalCode = recipientPostalCode;
    }
    
    public String getRecipientCity() {
        return recipientCity;
    }
    
    public void setRecipientCity(String recipientCity) {
        this.recipientCity = recipientCity;
    }
    
    public String getRecipientEmail() {
        return recipientEmail;
    }
    
    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public String getInvoiceDate() {
        return invoiceDate;
    }
    
    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
    
    public String getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public String getPropertyDesignation() {
        return propertyDesignation;
    }
    
    public void setPropertyDesignation(String propertyDesignation) {
        this.propertyDesignation = propertyDesignation;
    }
    
    public String getPaymentReference() {
        return paymentReference;
    }
    
    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
    
    public String getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }
    
    public String getVat() {
        return vat;
    }
    
    public void setVat(String vat) {
        this.vat = vat;
    }
    
    public String getTotal() {
        return total;
    }
    
    public void setTotal(String total) {
        this.total = total;
    }
    
    public String getAdditionalInfo() {
        return additionalInfo;
    }
    
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    
    public List<InvoiceItemPdfDTO> getItems() {
        return items;
    }
    
    public void setItems(List<InvoiceItemPdfDTO> items) {
        this.items = items;
    }
}