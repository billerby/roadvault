package com.billerby.roadvault.dto;

import java.math.BigDecimal;

/**
 * DTO for Association entities.
 */
public class AssociationDTO {
    
    private Long id;
    private String name;
    private String organizationNumber;
    private String bankgiroNumber;
    private String plusgiroNumber;
    private String email;
    private String phone;
    private String address;
    private String postalCode;
    private String city;
    private String website;
    private String invoiceText;
    private Integer reminderDays;
    private BigDecimal reminderFee;
    
    // Default constructor
    public AssociationDTO() {
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getOrganizationNumber() {
        return organizationNumber;
    }
    
    public void setOrganizationNumber(String organizationNumber) {
        this.organizationNumber = organizationNumber;
    }
    
    public String getBankgiroNumber() {
        return bankgiroNumber;
    }
    
    public void setBankgiroNumber(String bankgiroNumber) {
        this.bankgiroNumber = bankgiroNumber;
    }
    
    public String getPlusgiroNumber() {
        return plusgiroNumber;
    }
    
    public void setPlusgiroNumber(String plusgiroNumber) {
        this.plusgiroNumber = plusgiroNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    
    public String getInvoiceText() {
        return invoiceText;
    }
    
    public void setInvoiceText(String invoiceText) {
        this.invoiceText = invoiceText;
    }
    
    public Integer getReminderDays() {
        return reminderDays;
    }
    
    public void setReminderDays(Integer reminderDays) {
        this.reminderDays = reminderDays;
    }
    
    public BigDecimal getReminderFee() {
        return reminderFee;
    }
    
    public void setReminderFee(BigDecimal reminderFee) {
        this.reminderFee = reminderFee;
    }
}
