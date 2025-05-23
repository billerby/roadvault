package com.billerby.roadvault.service;

import com.billerby.roadvault.dto.BillingDTO;
import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.Billing;
import com.billerby.roadvault.repository.BillingRepository;
import com.billerby.roadvault.service.mapper.DTOMapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing billings.
 */
@Service
public class BillingService {
    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

    private final BillingRepository billingRepository;
    private final InvoiceService invoiceService;
    private final DTOMapperService dtoMapperService;

    @Autowired
    public BillingService(
            BillingRepository billingRepository, 
            @Lazy InvoiceService invoiceService,
            DTOMapperService dtoMapperService) {
        this.billingRepository = billingRepository;
        this.invoiceService = invoiceService;
        this.dtoMapperService = dtoMapperService;
    }

    /**
     * Get all billings with eagerly loaded invoices.
     *
     * @return List of all billings with their invoices
     */
    @Transactional(readOnly = true)
    public List<Billing> getAllBillingsWithInvoices() {
        try {
            return billingRepository.findAllWithInvoices();
        } catch (Exception e) {
            logger.error("Error retrieving billings with invoices: {}", e.getMessage(), e);
            // Fallback to standard findAll if the eager fetch fails
            return getAllBillings();
        }
    }

    /**
     * Get all billings as DTOs.
     *
     * @return List of all billing DTOs
     */
    @Transactional(readOnly = true)
    public List<BillingDTO> getAllBillingDTOs() {
        try {
            // Fetch all billings with a direct query to ensure all fields are loaded
            List<Billing> billings = billingRepository.findAll();
            
            List<BillingDTO> dtos = billings.stream()
                .map(billing -> {
                    try {
                        return dtoMapperService.toBillingDTO(billing);
                    } catch (Exception e) {
                        logger.error("Error converting billing {} to DTO: {}", billing.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
            
            logger.debug("Retrieved {} billings as DTOs", dtos.size());
            return dtos;
        } catch (Exception e) {
            logger.error("Error retrieving all billing DTOs: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get all billings.
     *
     * @return List of all billings
     */
    @Transactional(readOnly = true)
    public List<Billing> getAllBillings() {
        return billingRepository.findAll();
    }

    /**
     * Get a billing by ID.
     *
     * @param id The billing ID
     * @return The billing
     * @throws ResourceNotFoundException if billing is not found
     */
    @Transactional(readOnly = true)
    public Billing getBillingById(Long id) {
        return billingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing not found with id: " + id));
    }
    
    /**
     * Get a billing DTO by ID.
     *
     * @param id The billing ID
     * @return The billing DTO
     * @throws ResourceNotFoundException if billing is not found
     */
    @Transactional(readOnly = true)
    public BillingDTO getBillingDTOById(Long id) {
        Billing billing = getBillingById(id);
        return dtoMapperService.toBillingDTO(billing);
    }

    /**
     * Get a billing with invoices by ID.
     *
     * @param id The billing ID
     * @return The billing with invoices
     * @throws ResourceNotFoundException if billing is not found
     */
    @Transactional(readOnly = true)
    public Billing getBillingWithInvoicesById(Long id) {
        Billing billing = billingRepository.findByIdWithInvoices(id);
        if (billing == null) {
            throw new ResourceNotFoundException("Billing not found with id: " + id);
        }
        return billing;
    }
    
    /**
     * Get a billing with invoices by ID as DTO.
     *
     * @param id The billing ID
     * @return The billing DTO with invoices
     * @throws ResourceNotFoundException if billing is not found
     */
    @Transactional(readOnly = true)
    public BillingDTO getBillingWithInvoicesDTOById(Long id) {
        Billing billing = getBillingWithInvoicesById(id);
        return dtoMapperService.toBillingDTO(billing);
    }

    /**
     * Get billings by year.
     *
     * @param year The year
     * @return List of billings for the year
     */
    public List<Billing> getBillingsByYear(Integer year) {
        return billingRepository.findByYear(year);
    }
    
    /**
     * Get billing DTOs by year.
     *
     * @param year The year
     * @return List of billing DTOs for the year
     */
    public List<BillingDTO> getBillingDTOsByYear(Integer year) {
        List<Billing> billings = getBillingsByYear(year);
        return dtoMapperService.toBillingDTOList(billings);
    }

    /**
     * Get billings by year and type.
     *
     * @param year The year
     * @param type The billing type
     * @return List of billings for the year and type
     */
    public List<Billing> getBillingsByYearAndType(Integer year, Billing.BillingType type) {
        return billingRepository.findByYearAndType(year, type);
    }
    
    /**
     * Get billing DTOs by year and type.
     *
     * @param year The year
     * @param type The billing type
     * @return List of billing DTOs for the year and type
     */
    public List<BillingDTO> getBillingDTOsByYearAndType(Integer year, Billing.BillingType type) {
        List<Billing> billings = getBillingsByYearAndType(year, type);
        return dtoMapperService.toBillingDTOList(billings);
    }

    /**
     * Create a new billing.
     *
     * @param billing The billing to create
     * @return The created billing
     */
    @Transactional
    public Billing createBilling(Billing billing) {
        return billingRepository.save(billing);
    }
    
    /**
     * Create a new billing from DTO.
     *
     * @param billingDTO The billing DTO to create
     * @return The created billing DTO
     */
    @Transactional
    public BillingDTO createBillingDTO(BillingDTO billingDTO) {
        Billing billing = dtoMapperService.toBillingEntity(billingDTO);
        Billing createdBilling = createBilling(billing);
        return dtoMapperService.toBillingDTO(createdBilling);
    }

    /**
     * Create a new billing and generate invoices.
     *
     * @param billing The billing to create
     * @param generateInvoices Whether to generate invoices
     * @return The created billing
     */
    @Transactional
    public Billing createBillingWithInvoices(Billing billing, boolean generateInvoices) {
        Billing createdBilling = billingRepository.save(billing);
        
        if (generateInvoices) {
            invoiceService.generateInvoicesForBilling(createdBilling.getId());
        }
        
        Billing billingWithInvoices = getBillingWithInvoicesById(createdBilling.getId());
        return billingWithInvoices;
    }
    
    /**
     * Create a new billing and generate invoices from DTO.
     *
     * @param billingDTO The billing DTO to create
     * @param generateInvoices Whether to generate invoices
     * @return The created billing DTO
     */
    @Transactional
    public BillingDTO createBillingWithInvoicesDTO(BillingDTO billingDTO, boolean generateInvoices) {
        Billing billing = dtoMapperService.toBillingEntity(billingDTO);
        Billing createdBilling = createBillingWithInvoices(billing, generateInvoices);
        return dtoMapperService.toBillingDTO(createdBilling);
    }

    /**
     * Update a billing.
     *
     * @param id The billing ID
     * @param billingDetails The updated billing details
     * @return The updated billing
     * @throws ResourceNotFoundException if billing is not found
     */
    @Transactional
    public Billing updateBilling(Long id, Billing billingDetails) {
        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing not found with id: " + id));
        
        billing.setYear(billingDetails.getYear());
        billing.setDescription(billingDetails.getDescription());
        billing.setTotalAmount(billingDetails.getTotalAmount());
        billing.setIssueDate(billingDetails.getIssueDate());
        billing.setDueDate(billingDetails.getDueDate());
        billing.setType(billingDetails.getType());
        
        return billingRepository.save(billing);
    }
    
    /**
     * Update a billing from DTO.
     *
     * @param id The billing ID
     * @param billingDTO The updated billing DTO
     * @return The updated billing DTO
     * @throws ResourceNotFoundException if billing is not found
     */
    @Transactional
    public BillingDTO updateBillingDTO(Long id, BillingDTO billingDTO) {
        Billing billingDetails = dtoMapperService.toBillingEntity(billingDTO);
        Billing updatedBilling = updateBilling(id, billingDetails);
        return dtoMapperService.toBillingDTO(updatedBilling);
    }

    /**
     * Delete a billing.
     *
     * @param id The billing ID
     * @throws ResourceNotFoundException if billing is not found
     */
    @Transactional
    public void deleteBilling(Long id) {
        Billing billing = billingRepository.findByIdWithInvoices(id);
        if (billing == null) {
            throw new ResourceNotFoundException("Billing not found with id: " + id);
        }
        
        // The entity relationship already has cascade = CascadeType.ALL and orphanRemoval = true,
        // so we can delete the billing and all related invoices will be automatically deleted.
        billingRepository.delete(billing);
        
        logger.info("Deleted billing ID {} and all associated invoices", id);
    }
}
