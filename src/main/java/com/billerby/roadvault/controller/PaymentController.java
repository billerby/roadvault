package com.billerby.roadvault.controller;

import com.billerby.roadvault.dto.PaymentDTO;
import com.billerby.roadvault.model.Payment;
import com.billerby.roadvault.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing payments.
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * GET /v1/payments : Get all payments.
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        List<PaymentDTO> paymentDTOs = paymentService.getAllPaymentDTOs();
        return ResponseEntity.ok(paymentDTOs);
    }

    /**
     * GET /v1/payments/:id : Get the "id" payment.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        PaymentDTO paymentDTO = paymentService.getPaymentDTOById(id);
        return ResponseEntity.ok(paymentDTO);
    }

    /**
     * GET /v1/payments/by-date-range : Get payments by date range.
     */
    @GetMapping("/by-date-range")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<PaymentDTO> paymentDTOs = paymentService.getPaymentDTOsByDateRange(startDate, endDate);
        return ResponseEntity.ok(paymentDTOs);
    }

    /**
     * GET /v1/payments/by-type : Get payments by payment type.
     */
    @GetMapping("/by-type")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByType(@RequestParam String type) {
        try {
            Payment.PaymentType paymentType = Payment.PaymentType.valueOf(type.toUpperCase());
            List<PaymentDTO> paymentDTOs = paymentService.getPaymentDTOsByType(paymentType);
            return ResponseEntity.ok(paymentDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /v1/payments/by-invoice/:invoiceId : Get payments for a specific invoice.
     */
    @GetMapping("/by-invoice/{invoiceId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByInvoiceId(@PathVariable Long invoiceId) {
        List<PaymentDTO> paymentDTOs = paymentService.getPaymentDTOsByInvoiceId(invoiceId);
        return ResponseEntity.ok(paymentDTOs);
    }

    /**
     * POST /v1/payments : Create a new payment.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentDTO> createPayment(@RequestBody PaymentDTO paymentDTO) {
        if (paymentDTO.getInvoiceId() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        PaymentDTO createdPaymentDTO = paymentService.registerPaymentDTO(paymentDTO.getInvoiceId(), paymentDTO);
        return new ResponseEntity<>(createdPaymentDTO, HttpStatus.CREATED);
    }

    /**
     * PUT /v1/payments/:id : Update an existing payment.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentDTO> updatePayment(
            @PathVariable Long id,
            @RequestBody PaymentDTO paymentDTO) {
        
        Payment paymentDetails = new Payment();
        paymentDetails.setAmount(paymentDTO.getAmount());
        paymentDetails.setPaymentDate(paymentDTO.getPaymentDate());
        paymentDetails.setComment(paymentDTO.getComment());
        
        try {
            Payment.PaymentType paymentType = Payment.PaymentType.valueOf(paymentDTO.getPaymentType().toUpperCase());
            paymentDetails.setPaymentType(paymentType);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        
        Payment updatedPayment = paymentService.updatePayment(id, paymentDetails);
        PaymentDTO updatedPaymentDTO = paymentService.getPaymentDTOById(updatedPayment.getId());
        
        return ResponseEntity.ok(updatedPaymentDTO);
    }

    /**
     * DELETE /v1/payments/:id : Delete a payment.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /v1/payments/import : Import payments from a CSV file.
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> importPaymentsFromCsv(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("text/csv")) {
                return ResponseEntity.badRequest().build();
            }

            String fileContent = new String(file.getBytes());
            int importedCount = paymentService.processPaymentFile(fileContent, "CSV");
            
            Map<String, Object> result = Map.of(
                "imported", importedCount,
                "message", "Successfully imported " + importedCount + " payments"
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = Map.of(
                "error", "Failed to import payments: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    /**
     * POST /v1/payments/register-by-ocr : Register a payment using OCR number.
     */
    @PostMapping("/register-by-ocr")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentDTO> registerPaymentByOcr(@RequestBody OcrPaymentRequest request) {
        try {
            Payment.PaymentType paymentType = Payment.PaymentType.valueOf(request.getPaymentType().toUpperCase());
            
            Payment registeredPayment = paymentService.registerPaymentByOcr(
                request.getOcrNumber(),
                request.getAmount(),
                request.getPaymentDate(),
                paymentType,
                request.getComment()
            );
            
            PaymentDTO paymentDTO = paymentService.getPaymentDTOById(registeredPayment.getId());
            return new ResponseEntity<>(paymentDTO, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /v1/payments/types : Get all available payment types.
     */
    @GetMapping("/types")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<String>> getPaymentTypes() {
        List<String> paymentTypes = List.of(
            Payment.PaymentType.BANKGIRO.name(),
            Payment.PaymentType.PLUSGIRO.name(),
            Payment.PaymentType.SWISH.name(),
            Payment.PaymentType.MANUAL.name(),
            Payment.PaymentType.OTHER.name()
        );
        return ResponseEntity.ok(paymentTypes);
    }

    /**
     * Inner class for OCR payment requests.
     */
    public static class OcrPaymentRequest {
        private String ocrNumber;
        private java.math.BigDecimal amount;
        private LocalDate paymentDate;
        private String paymentType;
        private String comment;

        public OcrPaymentRequest() {}

        public String getOcrNumber() {
            return ocrNumber;
        }

        public void setOcrNumber(String ocrNumber) {
            this.ocrNumber = ocrNumber;
        }

        public java.math.BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(java.math.BigDecimal amount) {
            this.amount = amount;
        }

        public LocalDate getPaymentDate() {
            return paymentDate;
        }

        public void setPaymentDate(LocalDate paymentDate) {
            this.paymentDate = paymentDate;
        }

        public String getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(String paymentType) {
            this.paymentType = paymentType;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }
}
