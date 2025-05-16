package com.billerby.roadvault.service;

import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.Owner;
import com.billerby.roadvault.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing property owners.
 */
@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;

    @Autowired
    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
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
     * Find owner by email.
     *
     * @param email The email to search for
     * @return The owner if found
     */
    public Owner findOwnerByEmail(String email) {
        return ownerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with email: " + email));
    }
}
