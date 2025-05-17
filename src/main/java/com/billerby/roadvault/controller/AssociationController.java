package com.billerby.roadvault.controller;

import com.billerby.roadvault.dto.AssociationDTO;
import com.billerby.roadvault.model.Association;
import com.billerby.roadvault.service.AssociationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing association information.
 */
@RestController
@RequestMapping("/v1/association")
public class AssociationController {
    
    private final AssociationService associationService;
    
    @Autowired
    public AssociationController(AssociationService associationService) {
        this.associationService = associationService;
    }
    
    /**
     * GET /v1/association : Get the association information.
     * In typical usage, there will be only one association record.
     *
     * @return the ResponseEntity with status 200 (OK) and with body the association
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AssociationDTO> getAssociation() {
        Association association = associationService.getOrCreateDefaultAssociation();
        return ResponseEntity.ok(AssociationDTO.fromEntity(association));
    }
    
    /**
     * PUT /v1/association : Update the association information.
     *
     * @param associationDTO the association to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated association
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssociationDTO> updateAssociation(@RequestBody AssociationDTO associationDTO) {
        Association association = convertToEntity(associationDTO);
        
        Association updatedAssociation;
        if (association.getId() != null) {
            updatedAssociation = associationService.updateAssociation(association.getId(), association);
        } else {
            Association defaultAssociation = associationService.getOrCreateDefaultAssociation();
            association.setId(defaultAssociation.getId());
            updatedAssociation = associationService.updateAssociation(defaultAssociation.getId(), association);
        }
        
        return ResponseEntity.ok(AssociationDTO.fromEntity(updatedAssociation));
    }
    
    /**
     * POST /v1/association : Create a new association.
     * This is typically only used during system setup.
     *
     * @param associationDTO the association to create
     * @return the ResponseEntity with status 201 (Created) and with body the new association
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssociationDTO> createAssociation(@RequestBody AssociationDTO associationDTO) {
        // Only create if no association exists
        if (associationService.getAllAssociations().isEmpty()) {
            Association association = convertToEntity(associationDTO);
            Association createdAssociation = associationService.createAssociation(association);
            return new ResponseEntity<>(AssociationDTO.fromEntity(createdAssociation), HttpStatus.CREATED);
        } else {
            // Otherwise update the existing one
            return updateAssociation(associationDTO);
        }
    }
    
    /**
     * Convert DTO to entity.
     *
     * @param dto the DTO to convert
     * @return the converted entity
     */
    private Association convertToEntity(AssociationDTO dto) {
        Association association = new Association();
        association.setId(dto.getId());
        association.setName(dto.getName());
        association.setOrganizationNumber(dto.getOrganizationNumber());
        association.setBankgiroNumber(dto.getBankgiroNumber());
        association.setPlusgiroNumber(dto.getPlusgiroNumber());
        association.setEmail(dto.getEmail());
        association.setPhone(dto.getPhone());
        association.setAddress(dto.getAddress());
        association.setPostalCode(dto.getPostalCode());
        association.setCity(dto.getCity());
        association.setWebsite(dto.getWebsite());
        association.setInvoiceText(dto.getInvoiceText());
        association.setReminderDays(dto.getReminderDays());
        association.setReminderFee(dto.getReminderFee());
        return association;
    }
}
