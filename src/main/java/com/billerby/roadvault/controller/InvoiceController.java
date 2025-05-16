package com.billerby.roadvault.controller;

import com.billerby.roadvault.dto.InvoiceDTO;
import com.billerby.roadvault.dto.PaymentDTO;
import com.billerby.roadvault.model.Invoice;
import com.billerby.roadvault.model.Payment;
import com.billerby.roadvault.service.InvoiceService;
import com.billerby.roadvault.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing invoices.
 */
@RestController
@RequestMapping("/v1/invoices")
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    
    @Autowired
    public InvoiceController(InvoiceService invoiceService, PaymentService paymentService) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
    }
    
    /**
     * GET /v1/invoices : Get all invoices.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of invoices in body
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        List<InvoiceDTO> invoiceDTOs = invoices.stream()
                .map(InvoiceDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoiceDTOs);
    }
    
    /**
     * GET /v1/invoices/:id : Get the "id" invoice.
     *
     * @param id the id of the invoice to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the invoice, or with status 404 (Not Found)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<InvoiceDTO> getInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceWithDetailsById(id);
        return ResponseEntity.ok(InvoiceDTO.fromEntity(invoice));
    }
    
    /**
     * GET /v1/invoices/by-property/:propertyId : Get invoices for a property.
     *
     * @param propertyId the id of the property
     * @return the ResponseEntity with status 200 (OK) and the list of invoices in body
     */
    @GetMapping("/by-property/{propertyId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<InvoiceDTO>> getInvoicesByProperty(@PathVariable Long propertyId) {
        List<Invoice> invoices = invoiceService.getInvoicesByPropertyId(propertyId);
        List<InvoiceDTO> invoiceDTOs = invoices.stream()
                .map(InvoiceDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoiceDTOs);
    }
    
    /**
     * GET /v1/invoices/by-status?status=:status : Get invoices by status.
     *
     * @param status the status to filter by
     * @return the ResponseEntity with status 200 (OK) and the list of invoices in body
     */
    @GetMapping("/by-status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<InvoiceDTO>> getInvoicesByStatus(@RequestParam String status) {
        try {
            Invoice.InvoiceStatus invoiceStatus = Invoice.InvoiceStatus.valueOf(status.toUpperCase());
            List<Invoice> invoices = invoiceService.getInvoicesByStatus(invoiceStatus);
            List<InvoiceDTO> invoiceDTOs = invoices.stream()
                    .map(InvoiceDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(invoiceDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * GET /v1/invoices/by-ocr?ocrNumber=:ocrNumber : Find invoice by OCR number.
     *
     * @param ocrNumber the OCR number to search for
     * @return the ResponseEntity with status 200 (OK) and with body the invoice, or with status 404 (Not Found)
     */
    @GetMapping("/by-ocr")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<InvoiceDTO> findInvoiceByOcr(@RequestParam String ocrNumber) {
        Invoice invoice = invoiceService.findInvoiceByOcr(ocrNumber);
        return ResponseEntity.ok(InvoiceDTO.fromEntity(invoice));
    }
    
    /**
     * PUT /v1/invoices/:id/status : Update invoice status.
     *
     * @param id the id of the invoice to update
     * @param status the new status
     * @return the ResponseEntity with status 200 (OK) and with body the updated invoice
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvoiceDTO> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        try {
            Invoice.InvoiceStatus invoiceStatus = Invoice.InvoiceStatus.valueOf(status.toUpperCase());
            Invoice updatedInvoice = invoiceService.updateInvoiceStatus(id, invoiceStatus);
            return ResponseEntity.ok(InvoiceDTO.fromEntity(updatedInvoice));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * PUT /v1/invoices/:id/mark-as-sent : Mark invoice as sent.
     *
     * @param id the id of the invoice to mark
     * @return the ResponseEntity with status 200 (OK) and with body the updated invoice
     */
    @PutMapping("/{id}/mark-as-sent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvoiceDTO> markInvoiceAsSent(@PathVariable Long id) {
        Invoice updatedInvoice = invoiceService.markInvoiceAsSent(id);
        return ResponseEntity.ok(InvoiceDTO.fromEntity(updatedInvoice));
    }
    
    /**
     * POST /v1/invoices/mark-overdue : Mark overdue invoices.
     *
     * @return the ResponseEntity with status 200 (OK) and with body the number of marked invoices
     */
    @PostMapping("/mark-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> markOverdueInvoices() {
        int markedCount = invoiceService.markOverdueInvoices();
        return ResponseEntity.ok(markedCount);
    }
    
    /**
     * GET /v1/invoices/:id/payments : Get payments for an invoice.
     *
     * @param id the id of the invoice
     * @return the ResponseEntity with status 200 (OK) and the list of payments in body
     */
    @GetMapping("/{id}/payments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PaymentDTO>> getPaymentsForInvoice(@PathVariable Long id) {
        List<Payment> payments = paymentService.getPaymentsByInvoiceId(id);
        List<PaymentDTO> paymentDTOs = payments.stream()
                .map(PaymentDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(paymentDTOs);
    }
    
    /**
     * POST /v1/invoices/:id/payments : Register a payment for an invoice.
     *
     * @param id the id of the invoice
     * @param paymentDTO the payment to register
     * @return the ResponseEntity with status 201 (Created) and with body the registered payment
     */
    @PostMapping("/{id}/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentDTO> registerPayment(
            @PathVariable Long id,
            @RequestBody PaymentDTO paymentDTO) {
        
        Payment payment = convertToPaymentEntity(paymentDTO);
        Payment registeredPayment = paymentService.registerPayment(id, payment);
        
        return new ResponseEntity<>(PaymentDTO.fromEntity(registeredPayment), HttpStatus.CREATED);
    }
    
    /**
     * Convert a PaymentDTO to a Payment entity.
     *
     * @param dto the DTO to convert
     * @return the converted entity
     */
    private Payment convertToPaymentEntity(PaymentDTO dto) {
        Payment payment = new Payment();
        payment.setId(dto.getId());
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setComment(dto.getComment());
        
        if (dto.getPaymentType() != null) {
            try {
                payment.setPaymentType(Payment.PaymentType.valueOf(dto.getPaymentType()));
            } catch (IllegalArgumentException e) {
                // Use default type if invalid
                payment.setPaymentType(Payment.PaymentType.MANUAL);
            }
        } else {
            payment.setPaymentType(Payment.PaymentType.MANUAL);
        }
        
        return payment;
    }
}
