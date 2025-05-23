package com.billerby.roadvault.controller;

import com.billerby.roadvault.dto.BillingDTO;
import com.billerby.roadvault.dto.InvoiceDTO;
import com.billerby.roadvault.model.Invoice;
import com.billerby.roadvault.service.BillingService;
import com.billerby.roadvault.service.InvoiceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing billings.
 */
@RestController
@RequestMapping("/api/v1/billings")
public class BillingController {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(BillingController.class);
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
        try {
            List<BillingDTO> billingDTOs = billingService.getAllBillingDTOs();
            return ResponseEntity.ok(billingDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;  // Re-throw to let the error handler deal with it
        }
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
        BillingDTO billingDTO = billingService.getBillingDTOById(id);
        return ResponseEntity.ok(billingDTO);
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
        BillingDTO billingDTO = billingService.getBillingWithInvoicesDTOById(id);
        return ResponseEntity.ok(billingDTO);
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
        List<InvoiceDTO> invoiceDTOs = invoiceService.getInvoiceDTOsByBillingId(id);
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
        List<BillingDTO> billingDTOs = billingService.getBillingDTOsByYear(year);
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
            @RequestParam(defaultValue = "false") String generateInvoices) {
        
        // Convert the string parameter to boolean, handling various formats
        boolean generateInvoicesBoolean = Boolean.parseBoolean(generateInvoices);
        
        BillingDTO createdBillingDTO = billingService.createBillingWithInvoicesDTO(billingDTO, generateInvoicesBoolean);
        return new ResponseEntity<>(createdBillingDTO, HttpStatus.CREATED);
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
        
        BillingDTO updatedBillingDTO = billingService.updateBillingDTO(id, billingDTO);
        return ResponseEntity.ok(updatedBillingDTO);
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
        // Note: Since we're calling directly to the invoiceService, we'll need a DTO version there
        // Assuming InvoiceService has a generateInvoicesForBillingDTO method
        List<Invoice> invoices = invoiceService.generateInvoicesForBilling(id);
        List<InvoiceDTO> invoiceDTOs = invoiceService.getInvoiceDTOsByBillingId(id);
        return ResponseEntity.ok(invoiceDTOs);
    }
    

}
