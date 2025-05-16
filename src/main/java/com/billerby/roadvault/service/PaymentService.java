package com.billerby.roadvault.service;

import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.Invoice;
import com.billerby.roadvault.model.Payment;
import com.billerby.roadvault.repository.InvoiceRepository;
import com.billerby.roadvault.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing payments.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final OcrService ocrService;

    @Autowired
    public PaymentService(
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository,
            OcrService ocrService) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.ocrService = ocrService;
    }

    /**
     * Get all payments.
     *
     * @return List of all payments
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Get a payment by ID.
     *
     * @param id The payment ID
     * @return The payment
     * @throws ResourceNotFoundException if payment is not found
     */
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    /**
     * Get payments by invoice ID.
     *
     * @param invoiceId The invoice ID
     * @return List of payments for the invoice
     */
    public List<Payment> getPaymentsByInvoiceId(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }

    /**
     * Get payments by date range.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of payments in the date range
     */
    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.findByPaymentDateBetween(startDate, endDate);
    }

    /**
     * Get payments by payment type.
     *
     * @param paymentType The payment type
     * @return List of payments of the type
     */
    public List<Payment> getPaymentsByType(Payment.PaymentType paymentType) {
        return paymentRepository.findByPaymentType(paymentType);
    }

    /**
     * Register a payment for an invoice.
     *
     * @param invoiceId The invoice ID
     * @param payment The payment to register
     * @return The registered payment
     * @throws ResourceNotFoundException if invoice is not found
     */
    @Transactional
    public Payment registerPayment(Long invoiceId, Payment payment) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        
        payment.setInvoice(invoice);
        Payment savedPayment = paymentRepository.save(payment);
        
        // Update invoice status
        updateInvoiceStatus(invoice);
        
        return savedPayment;
    }

    /**
     * Register a payment by OCR number.
     *
     * @param ocrNumber The OCR number
     * @param amount The payment amount
     * @param paymentDate The payment date
     * @param paymentType The payment type
     * @param comment Optional comment
     * @return The registered payment
     * @throws ResourceNotFoundException if invoice is not found
     */
    @Transactional
    public Payment registerPaymentByOcr(
            String ocrNumber,
            BigDecimal amount,
            LocalDate paymentDate,
            Payment.PaymentType paymentType,
            String comment) {
        
        // Validate OCR number
        if (!ocrService.validateOcr(ocrNumber)) {
            throw new IllegalArgumentException("Invalid OCR number: " + ocrNumber);
        }
        
        // Find invoice by OCR number
        Invoice invoice = invoiceRepository.findByOcrNumber(ocrNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with OCR number: " + ocrNumber));
        
        // Create payment
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setPaymentDate(paymentDate);
        payment.setPaymentType(paymentType);
        payment.setComment(comment);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Update invoice status
        updateInvoiceStatus(invoice);
        
        return savedPayment;
    }

    /**
     * Update a payment.
     *
     * @param id The payment ID
     * @param paymentDetails The updated payment details
     * @return The updated payment
     * @throws ResourceNotFoundException if payment is not found
     */
    @Transactional
    public Payment updatePayment(Long id, Payment paymentDetails) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        payment.setAmount(paymentDetails.getAmount());
        payment.setPaymentDate(paymentDetails.getPaymentDate());
        payment.setPaymentType(paymentDetails.getPaymentType());
        payment.setComment(paymentDetails.getComment());
        
        Payment updatedPayment = paymentRepository.save(payment);
        
        // Update invoice status
        updateInvoiceStatus(updatedPayment.getInvoice());
        
        return updatedPayment;
    }

    /**
     * Delete a payment.
     *
     * @param id The payment ID
     * @throws ResourceNotFoundException if payment is not found
     */
    @Transactional
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        Invoice invoice = payment.getInvoice();
        
        paymentRepository.delete(payment);
        
        // Update invoice status
        updateInvoiceStatus(invoice);
    }

    /**
     * Import payments from a file.
     *
     * @param payments List of payments to import
     * @return Number of imported payments
     */
    @Transactional
    public int importPayments(List<Payment> payments) {
        int importedCount = 0;
        
        for (Payment payment : payments) {
            try {
                Invoice invoice = payment.getInvoice();
                if (invoice == null || invoice.getId() == null) {
                    // Try to find invoice by OCR if provided
                    String ocrNumber = payment.getComment(); // Assuming OCR is stored in comment field during import
                    if (ocrNumber != null && ocrService.validateOcr(ocrNumber)) {
                        invoice = invoiceRepository.findByOcrNumber(ocrNumber)
                                .orElse(null);
                        payment.setInvoice(invoice);
                    }
                }
                
                if (invoice != null) {
                    paymentRepository.save(payment);
                    updateInvoiceStatus(invoice);
                    importedCount++;
                }
            } catch (Exception e) {
                // Log error and continue with next payment
                // In a real application, you'd want to return details about failed imports
            }
        }
        
        return importedCount;
    }
    
    /**
     * Process a payment file.
     *
     * @param fileContent The file content
     * @param fileType The file type (BGMax, CSV, etc.)
     * @return Number of processed payments
     */
    @Transactional
    public int processPaymentFile(String fileContent, String fileType) {
        // This is a placeholder for the actual implementation
        // In a real application, you would parse the file based on its type
        // and call importPayments() with the parsed payments
        
        // For now, just return 0
        return 0;
    }

    /**
     * Update invoice status based on payments.
     *
     * @param invoice The invoice to update
     */
    private void updateInvoiceStatus(Invoice invoice) {
        List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
        
        BigDecimal totalPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalPaid.compareTo(invoice.getAmount()) >= 0) {
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
        } else if (invoice.getStatus() == Invoice.InvoiceStatus.PAID 
                || invoice.getStatus() == Invoice.InvoiceStatus.PARTIALLY_PAID) {
            invoice.setStatus(Invoice.InvoiceStatus.SENT);
        }
        
        invoiceRepository.save(invoice);
    }
}
