package com.billerby.roadvault.service;

import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.Owner;
import com.billerby.roadvault.model.Property;
import com.billerby.roadvault.repository.OwnerRepository;
import com.billerby.roadvault.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing properties.
 */
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final OwnerRepository ownerRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository, OwnerRepository ownerRepository) {
        this.propertyRepository = propertyRepository;
        this.ownerRepository = ownerRepository;
    }

    /**
     * Get all properties.
     *
     * @return List of all properties
     */
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    /**
     * Get all properties with their owners.
     *
     * @return List of all properties with owners
     */
    public List<Property> getAllPropertiesWithOwners() {
        return propertyRepository.findAllWithOwners();
    }

    /**
     * Get a property by ID.
     *
     * @param id The property ID
     * @return The property
     * @throws ResourceNotFoundException if property is not found
     */
    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
    }

    /**
     * Get a property with its owner by ID.
     *
     * @param id The property ID
     * @return The property with owner
     * @throws ResourceNotFoundException if property is not found
     */
    public Property getPropertyWithOwnerById(Long id) {
        return propertyRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
    }

    /**
     * Create a new property.
     *
     * @param property The property to create
     * @return The created property
     */
    @Transactional
    public Property createProperty(Property property) {
        return propertyRepository.save(property);
    }

    /**
     * Create a new property with owner.
     *
     * @param property The property to create
     * @param ownerId The ID of the owner
     * @return The created property
     * @throws ResourceNotFoundException if owner is not found
     */
    @Transactional
    public Property createPropertyWithOwner(Property property, Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));
        
        property.setOwner(owner);
        return propertyRepository.save(property);
    }

    /**
     * Update a property.
     *
     * @param id The property ID
     * @param propertyDetails The updated property details
     * @return The updated property
     * @throws ResourceNotFoundException if property is not found
     */
    @Transactional
    public Property updateProperty(Long id, Property propertyDetails) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        
        property.setPropertyDesignation(propertyDetails.getPropertyDesignation());
        property.setShareRatio(propertyDetails.getShareRatio());
        property.setAddress(propertyDetails.getAddress());
        
        // Only update owner if provided
        if (propertyDetails.getOwner() != null && propertyDetails.getOwner().getId() != null) {
            Owner owner = ownerRepository.findById(propertyDetails.getOwner().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + propertyDetails.getOwner().getId()));
            property.setOwner(owner);
        }
        
        return propertyRepository.save(property);
    }

    /**
     * Delete a property.
     *
     * @param id The property ID
     * @throws ResourceNotFoundException if property is not found
     */
    @Transactional
    public void deleteProperty(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        
        propertyRepository.delete(property);
    }

    /**
     * Search properties by designation.
     *
     * @param designation The property designation to search for
     * @return List of matching properties
     */
    public List<Property> searchPropertiesByDesignation(String designation) {
        return propertyRepository.findByPropertyDesignationContainingIgnoreCase(designation);
    }

    /**
     * Calculate total share ratio of all properties.
     *
     * @return The total share ratio
     */
    public BigDecimal calculateTotalShareRatio() {
        List<Property> properties = propertyRepository.findAll();
        return properties.stream()
                .map(Property::getShareRatio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Change owner of a property.
     *
     * @param propertyId The property ID
     * @param ownerId The new owner ID
     * @return The updated property
     * @throws ResourceNotFoundException if property or owner is not found
     */
    @Transactional
    public Property changeOwner(Long propertyId, Long ownerId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
        
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));
        
        property.setOwner(owner);
        return propertyRepository.save(property);
    }
}
