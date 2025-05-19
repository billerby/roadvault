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
        AssociationDTO associationDTO = associationService.getOrCreateDefaultAssociationDTO();
        return ResponseEntity.ok(associationDTO);
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
        AssociationDTO updatedAssociationDTO;
        
        if (associationDTO.getId() != null) {
            updatedAssociationDTO = associationService.updateAssociationDTO(associationDTO.getId(), associationDTO);
        } else {
            AssociationDTO defaultAssociationDTO = associationService.getOrCreateDefaultAssociationDTO();
            updatedAssociationDTO = associationService.updateAssociationDTO(defaultAssociationDTO.getId(), associationDTO);
        }
        
        return ResponseEntity.ok(updatedAssociationDTO);
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
            AssociationDTO createdAssociationDTO = associationService.createAssociationDTO(associationDTO);
            return new ResponseEntity<>(createdAssociationDTO, HttpStatus.CREATED);
        } else {
            // Otherwise update the existing one
            return updateAssociation(associationDTO);
        }
    }
    

}
