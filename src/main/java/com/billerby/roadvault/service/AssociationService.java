package com.billerby.roadvault.service;

import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.Association;
import com.billerby.roadvault.repository.AssociationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing association information.
 */
@Service
public class AssociationService {

    private final AssociationRepository associationRepository;

    @Autowired
    public AssociationService(AssociationRepository associationRepository) {
        this.associationRepository = associationRepository;
    }

    /**
     * Get all associations.
     *
     * @return List of all associations
     */
    public List<Association> getAllAssociations() {
        return associationRepository.findAll();
    }

    /**
     * Get an association by ID.
     *
     * @param id The association ID
     * @return The association
     * @throws ResourceNotFoundException if association is not found
     */
    public Association getAssociationById(Long id) {
        return associationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found with id: " + id));
    }

    /**
     * Create a new association.
     *
     * @param association The association to create
     * @return The created association
     */
    @Transactional
    public Association createAssociation(Association association) {
        return associationRepository.save(association);
    }

    /**
     * Update an association.
     *
     * @param id The association ID
     * @param associationDetails The updated association details
     * @return The updated association
     * @throws ResourceNotFoundException if association is not found
     */
    @Transactional
    public Association updateAssociation(Long id, Association associationDetails) {
        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found with id: " + id));
        
        // Update the fields
        association.setName(associationDetails.getName());
        association.setOrganizationNumber(associationDetails.getOrganizationNumber());
        association.setBankgiroNumber(associationDetails.getBankgiroNumber());
        association.setPlusgiroNumber(associationDetails.getPlusgiroNumber());
        association.setEmail(associationDetails.getEmail());
        association.setPhone(associationDetails.getPhone());
        association.setAddress(associationDetails.getAddress());
        association.setPostalCode(associationDetails.getPostalCode());
        association.setCity(associationDetails.getCity());
        association.setWebsite(associationDetails.getWebsite());
        association.setInvoiceText(associationDetails.getInvoiceText());
        association.setReminderDays(associationDetails.getReminderDays());
        association.setReminderFee(associationDetails.getReminderFee());
        
        return associationRepository.save(association);
    }

    /**
     * Delete an association.
     *
     * @param id The association ID
     * @throws ResourceNotFoundException if association is not found
     */
    @Transactional
    public void deleteAssociation(Long id) {
        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found with id: " + id));
        
        associationRepository.delete(association);
    }
    
    /**
     * Get or create the default association.
     * In a typical scenario, there will be only one association in the system.
     *
     * @return The default association
     */
    @Transactional
    public Association getOrCreateDefaultAssociation() {
        List<Association> associations = associationRepository.findAll();
        
        if (associations.isEmpty()) {
            Association defaultAssociation = new Association();
            defaultAssociation.setName("Vägförening");
            return associationRepository.save(defaultAssociation);
        }
        
        return associations.get(0);
    }
}
