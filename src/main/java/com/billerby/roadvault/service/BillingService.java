package com.billerby.roadvault.service;

import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.Billing;
import com.billerby.roadvault.repository.BillingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing billings.
 */
@Service
public class BillingService {

    private final BillingRepository billingRepository;
    private final InvoiceService invoiceService;

    @Autowired
    public BillingService(BillingRepository billingRepository, InvoiceService invoiceService) {
        this.billingRepository = billingRepository;
        this.invoiceService = invoiceService;
    }

    /**
     * Get all billings.
     *
     * @return List of all billings
     */
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
    public Billing getBillingById(Long id) {
        return billingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing not found with id: " + id));
    }

    /**
     * Get a billing with invoices by ID.
     *
     * @param id The billing ID
     * @return The billing with invoices
     * @throws ResourceNotFoundException if billing is not found
     */
    public Billing getBillingWithInvoicesById(Long id) {
        return billingRepository.findByIdWithInvoices(id);
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
        
        return getBillingWithInvoicesById(createdBilling.getId());
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
        billing.setExtraCharge(billingDetails.getExtraCharge());
        billing.setIssueDate(billingDetails.getIssueDate());
        billing.setDueDate(billingDetails.getDueDate());
        billing.setType(billingDetails.getType());
        
        return billingRepository.save(billing);
    }

    /**
     * Delete a billing.
     *
     * @param id The billing ID
     * @throws ResourceNotFoundException if billing is not found
     */
    @Transactional
    public void deleteBilling(Long id) {
        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing not found with id: " + id));
        
        // Check if billing has invoices
        if (billing.getInvoices() != null && !billing.getInvoices().isEmpty()) {
            throw new IllegalStateException("Cannot delete billing with invoices. Delete invoices first.");
        }
        
        billingRepository.delete(billing);
    }
}
