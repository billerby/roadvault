package com.billerby.roadvault.controller;

import com.billerby.roadvault.dto.OwnerDTO;
import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.Owner;
import com.billerby.roadvault.service.OwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing owners.
 */
@RestController
@RequestMapping("/v1/owners")
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
        List<Owner> owners = ownerService.getAllOwners();
        List<OwnerDTO> ownerDTOs = owners.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
        Owner owner = ownerService.getOwnerById(id);
        return ResponseEntity.ok(convertToDTO(owner));
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
        Owner owner = ownerService.getOwnerWithPropertiesById(id);
        return ResponseEntity.ok(convertToDTO(owner));
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
        Owner owner = convertToEntity(ownerDTO);
        Owner createdOwner = ownerService.createOwner(owner);
        return new ResponseEntity<>(convertToDTO(createdOwner), HttpStatus.CREATED);
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
        Owner owner = convertToEntity(ownerDTO);
        Owner updatedOwner = ownerService.updateOwner(id, owner);
        return ResponseEntity.ok(convertToDTO(updatedOwner));
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
        List<Owner> owners = ownerService.searchOwnersByName(name);
        List<OwnerDTO> ownerDTOs = owners.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
        Owner owner = ownerService.findOwnerByEmail(email);
        return ResponseEntity.ok(convertToDTO(owner));
    }
    
    /**
     * Convert an Owner entity to an OwnerDTO.
     *
     * @param owner the entity to convert
     * @return the converted DTO
     */
    private OwnerDTO convertToDTO(Owner owner) {
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
     * Convert an OwnerDTO to an Owner entity.
     *
     * @param dto the DTO to convert
     * @return the converted entity
     */
    private Owner convertToEntity(OwnerDTO dto) {
        Owner owner = new Owner();
        owner.setId(dto.getId());
        owner.setName(dto.getName());
        owner.setEmail(dto.getEmail());
        owner.setPhone(dto.getPhone());
        owner.setAddress(dto.getAddress());
        owner.setPostalCode(dto.getPostalCode());
        owner.setCity(dto.getCity());
        return owner;
    }
}
