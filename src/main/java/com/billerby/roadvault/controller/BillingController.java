package com.billerby.roadvault.controller;

import com.billerby.roadvault.dto.BillingDTO;
import com.billerby.roadvault.dto.InvoiceDTO;
import com.billerby.roadvault.model.Billing;
import com.billerby.roadvault.model.Invoice;
import com.billerby.roadvault.service.BillingService;
import com.billerby.roadvault.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing billings.
 */
@RestController
@RequestMapping("/v1/billings")
public class BillingController {
    
    private final BillingService billingService;
    private final InvoiceService invoiceService;
    
    @Autowired
    public BillingController(BillingService billingService, InvoiceService invoiceService) {
        this.billingService = billingService;
        this.invoiceService = invoiceService;
    }
    
    /**
     * GET /v1/billings : Get all billings.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of billings in body
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BillingDTO>> getAllBillings() {
        List<Billing> billings = billingService.getAllBillings();
        List<BillingDTO> billingDTOs = billings.stream()
                .map(BillingDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(billingDTOs);
    }
    
    /**
     * GET /v1/billings/:id : Get the "id" billing.
     *
     * @param id the id of the billing to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the billing, or with status 404 (Not Found)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BillingDTO> getBilling(@PathVariable Long id) {
        Billing billing = billingService.getBillingById(id);
        return ResponseEntity.ok(BillingDTO.fromEntity(billing));
    }
    
    /**
     * GET /v1/billings/:id/with-invoices : Get the "id" billing with invoices.
     *
     * @param id the id of the billing to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the billing with invoices, or with status 404 (Not Found)
     */
    @GetMapping("/{id}/with-invoices")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BillingDTO> getBillingWithInvoices(@PathVariable Long id) {
        Billing billing = billingService.getBillingWithInvoicesById(id);
        return ResponseEntity.ok(BillingDTO.fromEntity(billing));
    }
    
    /**
     * GET /v1/billings/:id/invoices : Get invoices for a billing.
     *
     * @param id the id of the billing
     * @return the ResponseEntity with status 200 (OK) and the list of invoices in body
     */
    @GetMapping("/{id}/invoices")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<InvoiceDTO>> getInvoicesForBilling(@PathVariable Long id) {
        List<Invoice> invoices = invoiceService.getInvoicesByBillingId(id);
        List<InvoiceDTO> invoiceDTOs = invoices.stream()
                .map(InvoiceDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoiceDTOs);
    }
    
    /**
     * GET /v1/billings/by-year?year=:year : Get billings by year.
     *
     * @param year the year to filter by
     * @return the ResponseEntity with status 200 (OK) and the list of billings in body
     */
    @GetMapping("/by-year")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BillingDTO>> getBillingsByYear(@RequestParam Integer year) {
        List<Billing> billings = billingService.getBillingsByYear(year);
        List<BillingDTO> billingDTOs = billings.stream()
                .map(BillingDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(billingDTOs);
    }
    
    /**
     * POST /v1/billings : Create a new billing.
     *
     * @param billingDTO the billing to create
     * @param generateInvoices whether to generate invoices for the billing
     * @return the ResponseEntity with status 201 (Created) and with body the new billing
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillingDTO> createBilling(
            @RequestBody BillingDTO billingDTO,
            @RequestParam(defaultValue = "false") boolean generateInvoices) {
        
        Billing billing = convertToEntity(billingDTO);
        Billing createdBilling = billingService.createBillingWithInvoices(billing, generateInvoices);
        
        return new ResponseEntity<>(BillingDTO.fromEntity(createdBilling), HttpStatus.CREATED);
    }
    
    /**
     * PUT /v1/billings/:id : Update an existing billing.
     *
     * @param id the id of the billing to update
     * @param billingDTO the billing to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated billing
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillingDTO> updateBilling(
            @PathVariable Long id,
            @RequestBody BillingDTO billingDTO) {
        
        Billing billing = convertToEntity(billingDTO);
        Billing updatedBilling = billingService.updateBilling(id, billing);
        
        return ResponseEntity.ok(BillingDTO.fromEntity(updatedBilling));
    }
    
    /**
     * DELETE /v1/billings/:id : Delete the "id" billing.
     *
     * @param id the id of the billing to delete
     * @return the ResponseEntity with status 204 (NO_CONTENT)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBilling(@PathVariable Long id) {
        billingService.deleteBilling(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * POST /v1/billings/:id/generate-invoices : Generate invoices for a billing.
     *
     * @param id the id of the billing
     * @return the ResponseEntity with status 200 (OK) and the list of generated invoices in body
     */
    @PostMapping("/{id}/generate-invoices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvoiceDTO>> generateInvoices(@PathVariable Long id) {
        List<Invoice> invoices = invoiceService.generateInvoicesForBilling(id);
        List<InvoiceDTO> invoiceDTOs = invoices.stream()
                .map(InvoiceDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoiceDTOs);
    }
    
    /**
     * Convert a BillingDTO to a Billing entity.
     *
     * @param dto the DTO to convert
     * @return the converted entity
     */
    private Billing convertToEntity(BillingDTO dto) {
        Billing billing = new Billing();
        billing.setId(dto.getId());
        billing.setYear(dto.getYear());
        billing.setDescription(dto.getDescription());
        billing.setTotalAmount(dto.getTotalAmount());
        billing.setExtraCharge(dto.getExtraCharge());
        billing.setIssueDate(dto.getIssueDate());
        billing.setDueDate(dto.getDueDate());
        
        if (dto.getType() != null) {
            try {
                billing.setType(Billing.BillingType.valueOf(dto.getType()));
            } catch (IllegalArgumentException e) {
                // Use default type if invalid
                billing.setType(Billing.BillingType.ANNUAL_FEE);
            }
        }
        
        return billing;
    }
}
