package com.billerby.roadvault.service;

import com.billerby.roadvault.dto.AssociationDTO;
import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.Association;
import com.billerby.roadvault.repository.AssociationRepository;
import com.billerby.roadvault.service.mapper.DTOMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing association information.
 */
@Service
public class AssociationService {

    private final AssociationRepository associationRepository;
    private final DTOMapperService dtoMapperService;

    @Autowired
    public AssociationService(
            AssociationRepository associationRepository,
            DTOMapperService dtoMapperService) {
        this.associationRepository = associationRepository;
        this.dtoMapperService = dtoMapperService;
    }

    /**
     * Get all associations as DTOs.
     *
     * @return List of all association DTOs
     */
    public List<AssociationDTO> getAllAssociationDTOs() {
        List<Association> associations = getAllAssociations();
        return associations.stream()
                .map(dtoMapperService::toAssociationDTO)
                .collect(Collectors.toList());
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
     * Get an association DTO by ID.
     *
     * @param id The association ID
     * @return The association DTO
     * @throws ResourceNotFoundException if association is not found
     */
    public AssociationDTO getAssociationDTOById(Long id) {
        Association association = getAssociationById(id);
        return dtoMapperService.toAssociationDTO(association);
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
     * Create a new association from DTO.
     *
     * @param associationDTO The association DTO to create
     * @return The created association DTO
     */
    @Transactional
    public AssociationDTO createAssociationDTO(AssociationDTO associationDTO) {
        Association association = dtoMapperService.toAssociationEntity(associationDTO);
        Association createdAssociation = createAssociation(association);
        return dtoMapperService.toAssociationDTO(createdAssociation);
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
     * Update an association from DTO.
     *
     * @param id The association ID
     * @param associationDTO The updated association DTO
     * @return The updated association DTO
     * @throws ResourceNotFoundException if association is not found
     */
    @Transactional
    public AssociationDTO updateAssociationDTO(Long id, AssociationDTO associationDTO) {
        Association associationDetails = dtoMapperService.toAssociationEntity(associationDTO);
        Association updatedAssociation = updateAssociation(id, associationDetails);
        return dtoMapperService.toAssociationDTO(updatedAssociation);
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
    
    /**
     * Get or create the default association as DTO.
     * In a typical scenario, there will be only one association in the system.
     *
     * @return The default association DTO
     */
    @Transactional
    public AssociationDTO getOrCreateDefaultAssociationDTO() {
        Association association = getOrCreateDefaultAssociation();
        return dtoMapperService.toAssociationDTO(association);
    }
    
    /**
     * Get the association information.
     * This is a convenience method for other services.
     *
     * @return The association DTO
     */
    public AssociationDTO getAssociation() {
        return getOrCreateDefaultAssociationDTO();
    }
    
    /**
     * Get the association entity.
     * This is a convenience method for other services that need the entity directly.
     *
     * @return The association entity
     */
    public Association getAssociationEntity() {
        return getOrCreateDefaultAssociation();
    }
}
