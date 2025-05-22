package com.billerby.roadvault.controller;

import com.billerby.roadvault.service.PdfGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for PDF operations.
 */
@RestController
@RequestMapping("/api/v1/pdf")
public class PdfController {

    private final PdfGenerationService pdfGenerationService;
    
    @Autowired
    public PdfController(PdfGenerationService pdfGenerationService) {
        this.pdfGenerationService = pdfGenerationService;
    }
    
    /**
     * Generate and download a PDF invoice.
     * 
     * @param invoiceId The ID of the invoice
     * @return The PDF file as a response
     */
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<?> downloadInvoicePdf(@PathVariable Long invoiceId) {
        try {
            byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(invoiceId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice-" + invoiceId + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating PDF: " + e.getMessage());
        }
    }
    
    /**
     * Generate and store a PDF invoice in the database.
     * 
     * @param invoiceId The ID of the invoice
     * @return A response indicating success or failure
     */
    @PostMapping("/invoice/{invoiceId}/generate")
    public ResponseEntity<?> generateAndStoreInvoicePdf(@PathVariable Long invoiceId) {
        boolean success = pdfGenerationService.generateAndStorePdf(invoiceId);
        
        if (success) {
            return ResponseEntity.ok("PDF generated and stored successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating or storing PDF.");
        }
    }
    
    /**
     * Generate and store PDF invoices for all invoices in a billing.
     * 
     * @param billingId The ID of the billing
     * @return A response indicating success or failure
     */
    @PostMapping("/billing/{billingId}/generate-all")
    public ResponseEntity<?> generateAndStoreAllInvoicePdfs(@PathVariable Long billingId) {
        // This would be implemented in the service to get all invoice IDs for a billing
        // and then call generateAndStorePdf for each
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body("Bulk PDF generation not yet implemented.");
    }
}
