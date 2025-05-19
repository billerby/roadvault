package com.billerby.roadvault.controller;

import com.billerby.roadvault.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for email operations.
 */
@RestController
@RequestMapping("/api/v1/emails")
public class EmailController {
    
    private final EmailService emailService;
    
    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }
    
    /**
     * POST /v1/emails/invoices : Send invoice emails for specific invoice IDs.
     *
     * @param request JSON payload with invoice IDs list
     * @return the ResponseEntity with status 200 (OK) and number of successfully sent emails
     */
    @PostMapping("/invoices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendInvoiceEmails(@RequestBody Map<String, List<Long>> request) {
        List<Long> invoiceIds = request.get("invoiceIds");
        
        if (invoiceIds == null || invoiceIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "No invoice IDs provided"
            ));
        }
        
        int sentCount = emailService.sendInvoices(invoiceIds);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "sentCount", sentCount,
                "totalCount", invoiceIds.size()
        ));
    }
    
    /**
     * POST /v1/emails/invoices/billing/{billingId} : Send invoice emails for all invoices in a billing.
     *
     * @param billingId the billing ID
     * @return the ResponseEntity with status 200 (OK) and number of successfully sent emails
     */
    @PostMapping("/invoices/billing/{billingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendInvoicesForBilling(@PathVariable Long billingId) {
        int sentCount = emailService.sendInvoicesForBilling(billingId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "sentCount", sentCount
        ));
    }
}
