package com.billerby.roadvault.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a property (fastighet) in the road association.
 */
@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_designation", nullable = false)
    private String propertyDesignation;

    @Column(name = "share_ratio", nullable = false, precision = 7, scale = 3)
    private BigDecimal shareRatio;

    @Column(name = "address")
    private String address;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Owner owner;

    // Default constructor required by JPA
    public Property() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Property property = (Property) o;

        return id != null ? id.equals(property.id) : property.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Property{" +
                "id=" + id +
                ", propertyDesignation='" + propertyDesignation + '\'' +
                ", shareRatio=" + shareRatio +
                '}';
    }
}
