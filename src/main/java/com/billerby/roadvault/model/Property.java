package com.billerby.roadvault.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    
    @ManyToMany
    @JoinTable(
        name = "property_owners",
        joinColumns = @JoinColumn(name = "property_id"),
        inverseJoinColumns = @JoinColumn(name = "owner_id")
    )
    private Set<Owner> owners = new HashSet<>();
    
    @ManyToOne
    @JoinColumn(name = "main_contact_id")
    private Owner mainContact;

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
    
    // Helper methods
    
    public void addOwner(Owner owner) {
        this.owners.add(owner);
        owner.getProperties().add(this);
    }
    
    public void removeOwner(Owner owner) {
        this.owners.remove(owner);
        owner.getProperties().remove(this);
        
        // If the removed owner was the main contact, set main contact to null
        if (this.mainContact != null && this.mainContact.equals(owner)) {
            this.mainContact = null;
        }
    }
    
    /**
     * Sets the main contact and ensures the owner is in the owners set.
     *
     * @param owner The owner to set as main contact
     */
    public void setMainContactWithCheck(Owner owner) {
        // Ensure the owner is in the owners set
        if (owner != null && !this.owners.contains(owner)) {
            addOwner(owner);
        }
        
        this.mainContact = owner;
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

    public Set<Owner> getOwners() {
        return owners;
    }

    public void setOwners(Set<Owner> owners) {
        this.owners = owners;
    }
    
    public Owner getMainContact() {
        return mainContact;
    }

    public void setMainContact(Owner mainContact) {
        this.mainContact = mainContact;
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
