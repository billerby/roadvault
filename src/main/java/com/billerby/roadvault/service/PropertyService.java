package com.billerby.roadvault.service;

import com.billerby.roadvault.dto.PropertyDTO;
import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.Owner;
import com.billerby.roadvault.model.Property;
import com.billerby.roadvault.repository.OwnerRepository;
import com.billerby.roadvault.repository.PropertyRepository;
import com.billerby.roadvault.service.mapper.DTOMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing properties.
 */
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final OwnerRepository ownerRepository;
    private final DTOMapperService dtoMapperService;

    @Autowired
    public PropertyService(
            PropertyRepository propertyRepository, 
            OwnerRepository ownerRepository,
            DTOMapperService dtoMapperService) {
        this.propertyRepository = propertyRepository;
        this.ownerRepository = ownerRepository;
        this.dtoMapperService = dtoMapperService;
    }

    /**
     * Get all properties as DTOs.
     *
     * @return List of all property DTOs
     */
    public List<PropertyDTO> getAllPropertyDTOs() {
        List<Property> properties = getAllProperties();
        return dtoMapperService.toPropertyDTOList(properties);
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
     * Get all properties with their owners as DTOs.
     *
     * @return List of all property DTOs with owners
     */
    public List<PropertyDTO> getAllPropertiesWithOwnersDTOs() {
        List<Property> properties = getAllPropertiesWithOwners();
        return dtoMapperService.toPropertyDTOList(properties);
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
     * Get a property DTO by ID.
     *
     * @param id The property ID
     * @return The property DTO
     * @throws ResourceNotFoundException if property is not found
     */
    public PropertyDTO getPropertyDTOById(Long id) {
        Property property = getPropertyById(id);
        return dtoMapperService.toPropertyDTO(property);
    }

    /**
     * Get a property with its owners by ID.
     *
     * @param id The property ID
     * @return The property with owners
     * @throws ResourceNotFoundException if property is not found
     */
    public Property getPropertyWithOwnerById(Long id) {
        return propertyRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
    }
    
    /**
     * Get a property with its owners by ID as DTO.
     *
     * @param id The property ID
     * @return The property DTO with owners
     * @throws ResourceNotFoundException if property is not found
     */
    public PropertyDTO getPropertyWithOwnerDTOById(Long id) {
        Property property = getPropertyWithOwnerById(id);
        return dtoMapperService.toPropertyDTO(property);
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
     * Create a new property from DTO.
     *
     * @param propertyDTO The property DTO to create
     * @return The created property DTO
     */
    @Transactional
    public PropertyDTO createPropertyDTO(PropertyDTO propertyDTO) {
        Property property = dtoMapperService.toPropertyEntity(propertyDTO);
        Property createdProperty = createProperty(property);
        return dtoMapperService.toPropertyDTO(createdProperty);
    }

    /**
     * Create a new property with owners and main contact.
     *
     * @param property The property to create
     * @param ownerIds List of owner IDs
     * @param mainContactId The ID of the main contact (should be one of the owners)
     * @return The created property
     * @throws ResourceNotFoundException if any owner is not found
     * @throws IllegalArgumentException if main contact is not in the owner list
     */
    @Transactional
    public Property createPropertyWithOwners(Property property, List<Long> ownerIds, Long mainContactId) {
        // Add owners to the property
        if (ownerIds != null && !ownerIds.isEmpty()) {
            for (Long ownerId : ownerIds) {
                Owner owner = ownerRepository.findById(ownerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));
                property.addOwner(owner);
            }
        }
        
        // Set main contact if provided
        if (mainContactId != null) {
            Owner mainContact = ownerRepository.findById(mainContactId)
                    .orElseThrow(() -> new ResourceNotFoundException("Main contact not found with id: " + mainContactId));
            
            // Set the main contact - this method will also add the owner if needed
            property.setMainContactWithCheck(mainContact);
        }
        
        return propertyRepository.save(property);
    }
    
    /**
     * Create a new property with owners and main contact from DTO.
     *
     * @param propertyDTO The property DTO to create
     * @return The created property DTO
     * @throws ResourceNotFoundException if any owner is not found
     * @throws IllegalArgumentException if main contact is not in the owner list
     */
    @Transactional
    public PropertyDTO createPropertyWithOwnersDTO(PropertyDTO propertyDTO) {
        Property property = dtoMapperService.toPropertyEntity(propertyDTO);
        
        // Extract owner IDs and main contact ID
        List<Long> ownerIds = null;
        Long mainContactId = null;
        
        if (propertyDTO.getOwners() != null && !propertyDTO.getOwners().isEmpty()) {
            ownerIds = propertyDTO.getOwners().stream()
                    .map(ownerDTO -> ownerDTO.getId())
                    .collect(Collectors.toList());
        }
        
        if (propertyDTO.getMainContact() != null) {
            mainContactId = propertyDTO.getMainContact().getId();
        }
        
        Property createdProperty = createPropertyWithOwners(property, ownerIds, mainContactId);
        return dtoMapperService.toPropertyDTO(createdProperty);
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
        
        return propertyRepository.save(property);
    }
    
    /**
     * Update a property from DTO.
     *
     * @param id The property ID
     * @param propertyDTO The updated property DTO
     * @return The updated property DTO
     * @throws ResourceNotFoundException if property is not found
     */
    @Transactional
    public PropertyDTO updatePropertyDTO(Long id, PropertyDTO propertyDTO) {
        Property propertyDetails = dtoMapperService.toPropertyEntity(propertyDTO);
        Property updatedProperty = updateProperty(id, propertyDetails);
        
        // Update owners if provided
        if (propertyDTO.getOwners() != null) {
            List<Long> ownerIds = propertyDTO.getOwners().stream()
                    .map(ownerDTO -> ownerDTO.getId())
                    .collect(Collectors.toList());
            
            updatedProperty = updateOwners(id, ownerIds);
        }
        
        // Update main contact if provided
        if (propertyDTO.getMainContact() != null) {
            updatedProperty = setMainContact(id, propertyDTO.getMainContact().getId());
        }
        
        return dtoMapperService.toPropertyDTO(updatedProperty);
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
     * Search properties by designation and return DTOs.
     *
     * @param designation The property designation to search for
     * @return List of matching property DTOs
     */
    public List<PropertyDTO> searchPropertiesDTOsByDesignation(String designation) {
        List<Property> properties = searchPropertiesByDesignation(designation);
        return dtoMapperService.toPropertyDTOList(properties);
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
     * Add an owner to a property.
     *
     * @param propertyId The property ID
     * @param ownerId The owner ID to add
     * @return The updated property
     * @throws ResourceNotFoundException if property or owner is not found
     */
    @Transactional
    public Property addOwner(Long propertyId, Long ownerId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
        
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));
        
        property.addOwner(owner);
        return propertyRepository.save(property);
    }
    
    /**
     * Add an owner to a property and return DTO.
     *
     * @param propertyId The property ID
     * @param ownerId The owner ID to add
     * @return The updated property DTO
     * @throws ResourceNotFoundException if property or owner is not found
     */
    @Transactional
    public PropertyDTO addOwnerDTO(Long propertyId, Long ownerId) {
        Property updatedProperty = addOwner(propertyId, ownerId);
        return dtoMapperService.toPropertyDTO(updatedProperty);
    }

    /**
     * Remove an owner from a property.
     *
     * @param propertyId The property ID
     * @param ownerId The owner ID to remove
     * @return The updated property
     * @throws ResourceNotFoundException if property or owner is not found
     * @throws IllegalStateException if trying to remove the only owner
     */
    @Transactional
    public Property removeOwner(Long propertyId, Long ownerId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
        
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));
        
        // Check if this is the only owner
        if (property.getOwners().size() <= 1) {
            throw new IllegalStateException("Cannot remove the only owner of a property");
        }
        
        property.removeOwner(owner);
        return propertyRepository.save(property);
    }
    
    /**
     * Remove an owner from a property and return DTO.
     *
     * @param propertyId The property ID
     * @param ownerId The owner ID to remove
     * @return The updated property DTO
     * @throws ResourceNotFoundException if property or owner is not found
     * @throws IllegalStateException if trying to remove the only owner
     */
    @Transactional
    public PropertyDTO removeOwnerDTO(Long propertyId, Long ownerId) {
        Property updatedProperty = removeOwner(propertyId, ownerId);
        return dtoMapperService.toPropertyDTO(updatedProperty);
    }

    /**
     * Set the main contact for a property.
     *
     * @param propertyId The property ID
     * @param ownerId The owner ID to set as main contact
     * @return The updated property
     * @throws ResourceNotFoundException if property or owner is not found
     * @throws IllegalArgumentException if the owner is not associated with the property
     */
    @Transactional
    public Property setMainContact(Long propertyId, Long ownerId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
        
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));
        
        property.setMainContactWithCheck(owner);
        return propertyRepository.save(property);
    }
    
    /**
     * Set the main contact for a property and return DTO.
     *
     * @param propertyId The property ID
     * @param ownerId The owner ID to set as main contact
     * @return The updated property DTO
     * @throws ResourceNotFoundException if property or owner is not found
     * @throws IllegalArgumentException if the owner is not associated with the property
     */
    @Transactional
    public PropertyDTO setMainContactDTO(Long propertyId, Long ownerId) {
        Property updatedProperty = setMainContact(propertyId, ownerId);
        return dtoMapperService.toPropertyDTO(updatedProperty);
    }
    
    /**
     * Update owners for a property.
     *
     * @param propertyId The property ID
     * @param ownerIds The list of owner IDs
     * @return The updated property
     * @throws ResourceNotFoundException if property or any owner is not found
     */
    @Transactional
    public Property updateOwners(Long propertyId, List<Long> ownerIds) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
        
        // Clear existing owners
        property.getOwners().clear();
        
        // Add new owners
        if (ownerIds != null && !ownerIds.isEmpty()) {
            for (Long ownerId : ownerIds) {
                Owner owner = ownerRepository.findById(ownerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));
                property.addOwner(owner);
            }
        }
        
        // Reset main contact if not in new owners list
        if (property.getMainContact() != null && !property.getOwners().contains(property.getMainContact())) {
            property.setMainContact(null);
        }
        
        return propertyRepository.save(property);
    }
    
    /**
     * Update owners for a property and return DTO.
     *
     * @param propertyId The property ID
     * @param ownerIds The list of owner IDs
     * @return The updated property DTO
     * @throws ResourceNotFoundException if property or any owner is not found
     */
    @Transactional
    public PropertyDTO updateOwnersDTO(Long propertyId, List<Long> ownerIds) {
        Property updatedProperty = updateOwners(propertyId, ownerIds);
        return dtoMapperService.toPropertyDTO(updatedProperty);
    }
}
