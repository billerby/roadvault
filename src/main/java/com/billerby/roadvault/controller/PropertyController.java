package com.billerby.roadvault.controller;

import com.billerby.roadvault.dto.PropertyDTO;
import com.billerby.roadvault.service.OwnerService;
import com.billerby.roadvault.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for managing properties.
 */
@RestController
@RequestMapping("/api/v1/properties")
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
        List<PropertyDTO> propertyDTOs = propertyService.getAllPropertiesWithOwnersDTOs();
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
        PropertyDTO propertyDTO = propertyService.getPropertyWithOwnerDTOById(id);
        return ResponseEntity.ok(propertyDTO);
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
        PropertyDTO createdPropertyDTO = propertyService.createPropertyWithOwnersDTO(propertyDTO);
        return new ResponseEntity<>(createdPropertyDTO, HttpStatus.CREATED);
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
        PropertyDTO updatedPropertyDTO = propertyService.updatePropertyDTO(id, propertyDTO);
        return ResponseEntity.ok(updatedPropertyDTO);
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
        List<PropertyDTO> propertyDTOs = propertyService.searchPropertiesDTOsByDesignation(designation);
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
        PropertyDTO updatedPropertyDTO = propertyService.addOwnerDTO(id, ownerId);
        return ResponseEntity.ok(updatedPropertyDTO);
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
        PropertyDTO updatedPropertyDTO = propertyService.removeOwnerDTO(id, ownerId);
        return ResponseEntity.ok(updatedPropertyDTO);
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
        PropertyDTO updatedPropertyDTO = propertyService.setMainContactDTO(id, ownerId);
        return ResponseEntity.ok(updatedPropertyDTO);
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
    

}
