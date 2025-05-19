package com.billerby.roadvault.service;

import com.billerby.roadvault.dto.OwnerDTO;
import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.Owner;
import com.billerby.roadvault.repository.OwnerRepository;
import com.billerby.roadvault.service.mapper.DTOMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing property owners.
 */
@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final DTOMapperService dtoMapperService;

    @Autowired
    public OwnerService(OwnerRepository ownerRepository, DTOMapperService dtoMapperService) {
        this.ownerRepository = ownerRepository;
        this.dtoMapperService = dtoMapperService;
    }

    /**
     * Get all owners as DTOs.
     *
     * @return List of all owner DTOs
     */
    public List<OwnerDTO> getAllOwnerDTOs() {
        List<Owner> owners = getAllOwners();
        return dtoMapperService.toOwnerDTOList(owners);
    }

    /**
     * Get all owners.
     *
     * @return List of all owners
     */
    public List<Owner> getAllOwners() {
        return ownerRepository.findAll();
    }

    /**
     * Get an owner by ID.
     *
     * @param id The owner ID
     * @return The owner
     * @throws ResourceNotFoundException if owner is not found
     */
    public Owner getOwnerById(Long id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + id));
    }
    
    /**
     * Get an owner DTO by ID.
     *
     * @param id The owner ID
     * @return The owner DTO
     * @throws ResourceNotFoundException if owner is not found
     */
    public OwnerDTO getOwnerDTOById(Long id) {
        Owner owner = getOwnerById(id);
        return dtoMapperService.toOwnerDTO(owner);
    }

    /**
     * Get an owner with properties by ID.
     *
     * @param id The owner ID
     * @return The owner with properties
     * @throws ResourceNotFoundException if owner is not found
     */
    public Owner getOwnerWithPropertiesById(Long id) {
        return ownerRepository.findByIdWithProperties(id)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + id));
    }
    
    /**
     * Get an owner with properties by ID as DTO.
     *
     * @param id The owner ID
     * @return The owner DTO with properties
     * @throws ResourceNotFoundException if owner is not found
     */
    public OwnerDTO getOwnerWithPropertiesDTOById(Long id) {
        Owner owner = getOwnerWithPropertiesById(id);
        return dtoMapperService.toOwnerDTO(owner);
    }

    /**
     * Create a new owner.
     *
     * @param owner The owner to create
     * @return The created owner
     */
    @Transactional
    public Owner createOwner(Owner owner) {
        return ownerRepository.save(owner);
    }
    
    /**
     * Create a new owner from DTO.
     *
     * @param ownerDTO The owner DTO to create
     * @return The created owner DTO
     */
    @Transactional
    public OwnerDTO createOwnerDTO(OwnerDTO ownerDTO) {
        Owner owner = dtoMapperService.toOwnerEntity(ownerDTO);
        Owner createdOwner = createOwner(owner);
        return dtoMapperService.toOwnerDTO(createdOwner);
    }

    /**
     * Update an owner.
     *
     * @param id The owner ID
     * @param ownerDetails The updated owner details
     * @return The updated owner
     * @throws ResourceNotFoundException if owner is not found
     */
    @Transactional
    public Owner updateOwner(Long id, Owner ownerDetails) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + id));
        
        owner.setName(ownerDetails.getName());
        owner.setEmail(ownerDetails.getEmail());
        owner.setPhone(ownerDetails.getPhone());
        owner.setAddress(ownerDetails.getAddress());
        owner.setPostalCode(ownerDetails.getPostalCode());
        owner.setCity(ownerDetails.getCity());
        
        return ownerRepository.save(owner);
    }
    
    /**
     * Update an owner from DTO.
     *
     * @param id The owner ID
     * @param ownerDTO The updated owner DTO
     * @return The updated owner DTO
     * @throws ResourceNotFoundException if owner is not found
     */
    @Transactional
    public OwnerDTO updateOwnerDTO(Long id, OwnerDTO ownerDTO) {
        Owner ownerDetails = dtoMapperService.toOwnerEntity(ownerDTO);
        Owner updatedOwner = updateOwner(id, ownerDetails);
        return dtoMapperService.toOwnerDTO(updatedOwner);
    }

    /**
     * Delete an owner.
     *
     * @param id The owner ID
     * @throws ResourceNotFoundException if owner is not found
     */
    @Transactional
    public void deleteOwner(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + id));
        
        // Check if owner has properties
        if (!owner.getProperties().isEmpty()) {
            throw new IllegalStateException("Cannot delete owner with properties. Remove properties first or transfer to another owner.");
        }
        
        ownerRepository.delete(owner);
    }

    /**
     * Search owners by name.
     *
     * @param name The name to search for
     * @return List of matching owners
     */
    public List<Owner> searchOwnersByName(String name) {
        return ownerRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Search owners by name and return DTOs.
     *
     * @param name The name to search for
     * @return List of matching owner DTOs
     */
    public List<OwnerDTO> searchOwnerDTOsByName(String name) {
        List<Owner> owners = searchOwnersByName(name);
        return dtoMapperService.toOwnerDTOList(owners);
    }

    /**
     * Find owner by email.
     *
     * @param email The email to search for
     * @return The owner if found
     */
    public Owner findOwnerByEmail(String email) {
        return ownerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with email: " + email));
    }
    
    /**
     * Find owner by email and return DTO.
     *
     * @param email The email to search for
     * @return The owner DTO if found
     */
    public OwnerDTO findOwnerDTOByEmail(String email) {
        Owner owner = findOwnerByEmail(email);
        return dtoMapperService.toOwnerDTO(owner);
    }
}
