package com.billerby.roadvault.controller;

import com.billerby.roadvault.dto.OwnerDTO;
import com.billerby.roadvault.dto.PropertyDTO;
import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.Owner;
import com.billerby.roadvault.model.Property;
import com.billerby.roadvault.service.OwnerService;
import com.billerby.roadvault.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for managing properties.
 */
@RestController
@RequestMapping("/v1/properties")
public class PropertyController {
    
    private final PropertyService propertyService;
    private final OwnerService ownerService;
    
    @Autowired
    public PropertyController(PropertyService propertyService, OwnerService ownerService) {
        this.propertyService = propertyService;
        this.ownerService = ownerService;
    }
    
    /**
     * GET /v1/properties : Get all properties.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of properties in body
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PropertyDTO>> getAllProperties() {
        List<Property> properties = propertyService.getAllPropertiesWithOwners();
        List<PropertyDTO> propertyDTOs = properties.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(propertyDTOs);
    }
    
    /**
     * GET /v1/properties/:id : Get the "id" property.
     *
     * @param id the id of the property to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the property, or with status 404 (Not Found)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PropertyDTO> getProperty(@PathVariable Long id) {
        Property property = propertyService.getPropertyWithOwnerById(id);
        return ResponseEntity.ok(convertToDTO(property));
    }
    
    /**
     * POST /v1/properties : Create a new property.
     *
     * @param propertyDTO the property to create
     * @return the ResponseEntity with status 201 (Created) and with body the new property
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyDTO> createProperty(@RequestBody PropertyDTO propertyDTO) {
        Property property = convertToEntity(propertyDTO);
        
        // Extract owner IDs and main contact ID
        List<Long> ownerIds = null;
        Long mainContactId = null;
        
        if (propertyDTO.getOwners() != null && !propertyDTO.getOwners().isEmpty()) {
            ownerIds = propertyDTO.getOwners().stream()
                    .map(OwnerDTO::getId)
                    .collect(Collectors.toList());
        }
        
        if (propertyDTO.getMainContact() != null) {
            mainContactId = propertyDTO.getMainContact().getId();
        }
        
        Property createdProperty = propertyService.createPropertyWithOwners(property, ownerIds, mainContactId);
        return new ResponseEntity<>(convertToDTO(createdProperty), HttpStatus.CREATED);
    }
    
    /**
     * PUT /v1/properties/:id : Update an existing property.
     *
     * @param id the id of the property to update
     * @param propertyDTO the property to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated property
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyDTO> updateProperty(@PathVariable Long id, @RequestBody PropertyDTO propertyDTO) {
        Property property = convertToEntity(propertyDTO);
        Property updatedProperty = propertyService.updateProperty(id, property);
        
        // Update owners if provided
        if (propertyDTO.getOwners() != null) {
            List<Long> ownerIds = propertyDTO.getOwners().stream()
                    .map(OwnerDTO::getId)
                    .collect(Collectors.toList());
            
            updatedProperty = propertyService.updateOwners(id, ownerIds);
        }
        
        // Update main contact if provided
        if (propertyDTO.getMainContact() != null) {
            updatedProperty = propertyService.setMainContact(id, propertyDTO.getMainContact().getId());
        }
        
        return ResponseEntity.ok(convertToDTO(updatedProperty));
    }
    
    /**
     * DELETE /v1/properties/:id : Delete the "id" property.
     *
     * @param id the id of the property to delete
     * @return the ResponseEntity with status 204 (NO_CONTENT)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * GET /v1/properties/search?designation=:designation : Search properties by designation.
     *
     * @param designation the designation to search for
     * @return the ResponseEntity with status 200 (OK) and the list of matching properties in body
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PropertyDTO>> searchPropertiesByDesignation(@RequestParam String designation) {
        List<Property> properties = propertyService.searchPropertiesByDesignation(designation);
        List<PropertyDTO> propertyDTOs = properties.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(propertyDTOs);
    }
    
    /**
     * PUT /v1/properties/:id/owner/:ownerId/add : Add an owner to a property.
     *
     * @param id the id of the property
     * @param ownerId the id of the owner to add
     * @return the ResponseEntity with status 200 (OK) and with body the updated property
     */
    @PutMapping("/{id}/owner/{ownerId}/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyDTO> addOwner(@PathVariable Long id, @PathVariable Long ownerId) {
        Property updatedProperty = propertyService.addOwner(id, ownerId);
        return ResponseEntity.ok(convertToDTO(updatedProperty));
    }
    
    /**
     * PUT /v1/properties/:id/owner/:ownerId/remove : Remove an owner from a property.
     *
     * @param id the id of the property
     * @param ownerId the id of the owner to remove
     * @return the ResponseEntity with status 200 (OK) and with body the updated property
     */
    @PutMapping("/{id}/owner/{ownerId}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyDTO> removeOwner(@PathVariable Long id, @PathVariable Long ownerId) {
        Property updatedProperty = propertyService.removeOwner(id, ownerId);
        return ResponseEntity.ok(convertToDTO(updatedProperty));
    }
    
    /**
     * PUT /v1/properties/:id/main-contact/:ownerId : Set main contact for a property.
     *
     * @param id the id of the property
     * @param ownerId the id of the owner to set as main contact
     * @return the ResponseEntity with status 200 (OK) and with body the updated property
     */
    @PutMapping("/{id}/main-contact/{ownerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyDTO> setMainContact(@PathVariable Long id, @PathVariable Long ownerId) {
        Property updatedProperty = propertyService.setMainContact(id, ownerId);
        return ResponseEntity.ok(convertToDTO(updatedProperty));
    }
    
    /**
     * GET /v1/properties/total-share : Get the total share ratio of all properties.
     *
     * @return the ResponseEntity with status 200 (OK) and with body the total share ratio
     */
    @GetMapping("/total-share")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BigDecimal> getTotalShareRatio() {
        BigDecimal totalShare = propertyService.calculateTotalShareRatio();
        return ResponseEntity.ok(totalShare);
    }
    
    /**
     * Convert a Property entity to a PropertyDTO.
     *
     * @param property the entity to convert
     * @return the converted DTO
     */
    private PropertyDTO convertToDTO(Property property) {
        PropertyDTO dto = new PropertyDTO();
        dto.setId(property.getId());
        dto.setPropertyDesignation(property.getPropertyDesignation());
        dto.setShareRatio(property.getShareRatio());
        dto.setAddress(property.getAddress());
        
        // Convert owners
        Set<OwnerDTO> ownerDTOs = new HashSet<>();
        if (property.getOwners() != null) {
            for (Owner owner : property.getOwners()) {
                ownerDTOs.add(convertOwnerToDTO(owner));
            }
        }
        dto.setOwners(ownerDTOs);
        
        // Convert main contact
        if (property.getMainContact() != null) {
            dto.setMainContact(convertOwnerToDTO(property.getMainContact()));
        }
        
        return dto;
    }
    
    /**
     * Convert an Owner entity to an OwnerDTO.
     *
     * @param owner the entity to convert
     * @return the converted DTO
     */
    private OwnerDTO convertOwnerToDTO(Owner owner) {
        if (owner == null) {
            return null;
        }
        
        OwnerDTO dto = new OwnerDTO();
        dto.setId(owner.getId());
        dto.setName(owner.getName());
        dto.setEmail(owner.getEmail());
        dto.setPhone(owner.getPhone());
        dto.setAddress(owner.getAddress());
        dto.setPostalCode(owner.getPostalCode());
        dto.setCity(owner.getCity());
        
        return dto;
    }
    
    /**
     * Convert a PropertyDTO to a Property entity.
     *
     * @param dto the DTO to convert
     * @return the converted entity
     */
    private Property convertToEntity(PropertyDTO dto) {
        Property property = new Property();
        property.setId(dto.getId());
        property.setPropertyDesignation(dto.getPropertyDesignation());
        property.setShareRatio(dto.getShareRatio());
        property.setAddress(dto.getAddress());
        
        // Don't set owners and main contact here, it's handled separately in service methods
        
        return property;
    }
}
