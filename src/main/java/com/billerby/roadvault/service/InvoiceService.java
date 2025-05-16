package com.billerby.roadvault.service;

import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.Billing;
import com.billerby.roadvault.model.Invoice;
import com.billerby.roadvault.model.Property;
import com.billerby.roadvault.repository.BillingRepository;
import com.billerby.roadvault.repository.InvoiceRepository;
import com.billerby.roadvault.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing invoices.
 */
@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BillingRepository billingRepository;
    private final PropertyRepository propertyRepository;
    private final OcrService ocrService;

    @Autowired
    public InvoiceService(
            InvoiceRepository invoiceRepository,
            BillingRepository billingRepository,
            PropertyRepository propertyRepository,
            OcrService ocrService) {
        this.invoiceRepository = invoiceRepository;
        this.billingRepository = billingRepository;
        this.propertyRepository = propertyRepository;
        this.ocrService = ocrService;
    }

    /**
     * Get all invoices.
     *
     * @return List of all invoices
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    /**
     * Get an invoice by ID.
     *
     * @param id The invoice ID
     * @return The invoice
     * @throws ResourceNotFoundException if invoice is not found
     */
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }

    /**
     * Get an invoice with details by ID.
     *
     * @param id The invoice ID
     * @return The invoice with details
     * @throws ResourceNotFoundException if invoice is not found
     */
    public Invoice getInvoiceWithDetailsById(Long id) {
        return invoiceRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }

    /**
     * Get invoices by billing ID.
     *
     * @param billingId The billing ID
     * @return List of invoices for the billing
     */
    public List<Invoice> getInvoicesByBillingId(Long billingId) {
        return invoiceRepository.findByBillingId(billingId);
    }

    /**
     * Get invoices by property ID.
     *
     * @param propertyId The property ID
     * @return List of invoices for the property
     */
    public List<Invoice> getInvoicesByPropertyId(Long propertyId) {
        return invoiceRepository.findByPropertyId(propertyId);
    }

    /**
     * Get invoices by status.
     *
     * @param status The invoice status
     * @return List of invoices with the status
     */
    public List<Invoice> getInvoicesByStatus(Invoice.InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    /**
     * Find invoice by OCR number.
     *
     * @param ocrNumber The OCR number
     * @return The invoice if found
     * @throws ResourceNotFoundException if invoice is not found
     */
    public Invoice findInvoiceByOcr(String ocrNumber) {
        return invoiceRepository.findByOcrNumber(ocrNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with OCR number: " + ocrNumber));
    }

    /**
     * Generate invoices for a billing.
     *
     * @param billingId The billing ID
     * @return List of generated invoices
     * @throws ResourceNotFoundException if billing is not found
     */
    @Transactional
    public List<Invoice> generateInvoicesForBilling(Long billingId) {
        Billing billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing not found with id: " + billingId));
        
        List<Property> properties = propertyRepository.findAll();
        List<Invoice> generatedInvoices = new ArrayList<>();
        
        // Calculate total share ratio
        BigDecimal totalShareRatio = properties.stream()
                .map(Property::getShareRatio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Generate invoices for each property
        for (Property property : properties) {
            // Calculate amount using the helper method
            BigDecimal amount = calculateInvoiceAmount(
                    billing.getTotalAmount(),
                    property.getShareRatio(),
                    totalShareRatio,
                    billing.getExtraCharge());
            
            // Create invoice
            Invoice invoice = new Invoice();
            invoice.setBilling(billing);
            invoice.setProperty(property);
            invoice.setAmount(amount);
            invoice.setDueDate(billing.getDueDate());
            invoice.setStatus(Invoice.InvoiceStatus.CREATED);
            
            // Generate invoice number
            String year = String.valueOf(billing.getYear());
            Integer maxInvoiceNumber = invoiceRepository.findMaxInvoiceNumberByYear(year);
            int nextInvoiceNumber = (maxInvoiceNumber != null) ? maxInvoiceNumber + 1 : 1;
            invoice.setInvoiceNumber(year + "-" + nextInvoiceNumber);
            
            // Generate OCR number
            String ocrNumber = ocrService.generateOcr(billing.getYear(), property.getId(), nextInvoiceNumber);
            invoice.setOcrNumber(ocrNumber);
            
            // Save invoice
            generatedInvoices.add(invoiceRepository.save(invoice));
        }
        
        return generatedInvoices;
    }

    /**
     * Update invoice status.
     *
     * @param id The invoice ID
     * @param status The new status
     * @return The updated invoice
     * @throws ResourceNotFoundException if invoice is not found
     */
    @Transactional
    public Invoice updateInvoiceStatus(Long id, Invoice.InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    /**
     * Mark invoice as sent.
     *
     * @param id The invoice ID
     * @return The updated invoice
     * @throws ResourceNotFoundException if invoice is not found
     */
    @Transactional
    public Invoice markInvoiceAsSent(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        
        if (invoice.getStatus() == Invoice.InvoiceStatus.CREATED) {
            invoice.setStatus(Invoice.InvoiceStatus.SENT);
            return invoiceRepository.save(invoice);
        }
        
        return invoice;
    }

    /**
     * Mark overdue invoices.
     *
     * @return Number of marked invoices
     */
    @Transactional
    public int markOverdueInvoices() {
        List<Invoice> invoices = invoiceRepository.findByStatus(Invoice.InvoiceStatus.SENT);
        int markedCount = 0;
        
        LocalDate today = LocalDate.now();
        for (Invoice invoice : invoices) {
            if (today.isAfter(invoice.getDueDate())) {
                invoice.setStatus(Invoice.InvoiceStatus.OVERDUE);
                invoiceRepository.save(invoice);
                markedCount++;
            }
        }
        
        return markedCount;
    }
    
    /**
     * Calculate invoice amount based on property's share ratio.
     * This is separated for testing purposes.
     *
     * @param totalAmount The total billing amount
     * @param shareRatio The property's share ratio
     * @param totalShareRatio The sum of all share ratios
     * @param extraCharge Optional extra charge (can be null)
     * @return The calculated invoice amount
     */
    public BigDecimal calculateInvoiceAmount(
            BigDecimal totalAmount,
            BigDecimal shareRatio,
            BigDecimal totalShareRatio,
            BigDecimal extraCharge) {
        
        BigDecimal amount = totalAmount
                .multiply(shareRatio)
                .divide(totalShareRatio, 2, RoundingMode.HALF_UP);
        
        // Add extra charge if applicable
        if (extraCharge != null && extraCharge.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal extraAmount = extraCharge
                    .multiply(shareRatio)
                    .divide(totalShareRatio, 2, RoundingMode.HALF_UP);
            amount = amount.add(extraAmount);
        }
        
        return amount;
    }
}
