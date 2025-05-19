package com.billerby.roadvault.controller;

import com.billerby.roadvault.dto.OwnerDTO;
import com.billerby.roadvault.service.OwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing owners.
 */
@RestController
@RequestMapping("/api/v1/owners")
public class OwnerController {
    
    private final OwnerService ownerService;
    
    @Autowired
    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }
    
    /**
     * GET /v1/owners : Get all owners.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of owners in body
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OwnerDTO>> getAllOwners() {
        List<OwnerDTO> ownerDTOs = ownerService.getAllOwnerDTOs();
        return ResponseEntity.ok(ownerDTOs);
    }
    
    /**
     * GET /v1/owners/:id : Get the "id" owner.
     *
     * @param id the id of the owner to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the owner, or with status 404 (Not Found)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OwnerDTO> getOwner(@PathVariable Long id) {
        OwnerDTO ownerDTO = ownerService.getOwnerDTOById(id);
        return ResponseEntity.ok(ownerDTO);
    }
    
    /**
     * GET /v1/owners/:id/with-properties : Get the "id" owner with properties.
     *
     * @param id the id of the owner to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the owner with properties, or with status 404 (Not Found)
     */
    @GetMapping("/{id}/with-properties")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OwnerDTO> getOwnerWithProperties(@PathVariable Long id) {
        OwnerDTO ownerDTO = ownerService.getOwnerWithPropertiesDTOById(id);
        return ResponseEntity.ok(ownerDTO);
    }
    
    /**
     * POST /v1/owners : Create a new owner.
     *
     * @param ownerDTO the owner to create
     * @return the ResponseEntity with status 201 (Created) and with body the new owner
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OwnerDTO> createOwner(@RequestBody OwnerDTO ownerDTO) {
        OwnerDTO createdOwnerDTO = ownerService.createOwnerDTO(ownerDTO);
        return new ResponseEntity<>(createdOwnerDTO, HttpStatus.CREATED);
    }
    
    /**
     * PUT /v1/owners/:id : Update an existing owner.
     *
     * @param id the id of the owner to update
     * @param ownerDTO the owner to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated owner
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OwnerDTO> updateOwner(@PathVariable Long id, @RequestBody OwnerDTO ownerDTO) {
        OwnerDTO updatedOwnerDTO = ownerService.updateOwnerDTO(id, ownerDTO);
        return ResponseEntity.ok(updatedOwnerDTO);
    }
    
    /**
     * DELETE /v1/owners/:id : Delete the "id" owner.
     *
     * @param id the id of the owner to delete
     * @return the ResponseEntity with status 204 (NO_CONTENT)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOwner(@PathVariable Long id) {
        ownerService.deleteOwner(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * GET /v1/owners/search?name=:name : Search owners by name.
     *
     * @param name the name to search for
     * @return the ResponseEntity with status 200 (OK) and the list of matching owners in body
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OwnerDTO>> searchOwnersByName(@RequestParam String name) {
        List<OwnerDTO> ownerDTOs = ownerService.searchOwnerDTOsByName(name);
        return ResponseEntity.ok(ownerDTOs);
    }
    
    /**
     * GET /v1/owners/by-email?email=:email : Find owner by email.
     *
     * @param email the email to search for
     * @return the ResponseEntity with status 200 (OK) and with body the owner, or with status 404 (Not Found)
     */
    @GetMapping("/by-email")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OwnerDTO> findOwnerByEmail(@RequestParam String email) {
        OwnerDTO ownerDTO = ownerService.findOwnerDTOByEmail(email);
        return ResponseEntity.ok(ownerDTO);
    }
    

}
