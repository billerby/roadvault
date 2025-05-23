package com.billerby.roadvault.service;

import com.billerby.roadvault.dto.BillingDTO;
import com.billerby.roadvault.dto.InvoiceDTO;
import com.billerby.roadvault.dto.OwnerDTO;
import com.billerby.roadvault.exception.ResourceNotFoundException;
import com.billerby.roadvault.model.*;
import com.billerby.roadvault.repository.BillingRepository;
import com.billerby.roadvault.repository.InvoiceRepository;
import com.billerby.roadvault.repository.PropertyRepository;
import com.billerby.roadvault.service.mapper.DTOMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private final DTOMapperService dtoMapperService;
    private final PdfGenerationService pdfGenerationService;
    private final AssociationService associationService;

    @Autowired
    public InvoiceService(
            InvoiceRepository invoiceRepository,
            BillingRepository billingRepository,
            PropertyRepository propertyRepository,
            OcrService ocrService,
            DTOMapperService dtoMapperService,
            @Lazy PdfGenerationService pdfGenerationService,
            AssociationService associationService) {
        this.invoiceRepository = invoiceRepository;
        this.billingRepository = billingRepository;
        this.propertyRepository = propertyRepository;
        this.ocrService = ocrService;
        this.dtoMapperService = dtoMapperService;
        this.pdfGenerationService = pdfGenerationService;
        this.associationService = associationService;
    }

    /**
     * Get all invoices as DTOs.
     *
     * @return List of all invoice DTOs
     */
    @Transactional
    public List<InvoiceDTO> getAllInvoiceDTOs() {
        List<Invoice> invoices = invoiceRepository.findAll();
        return dtoMapperService.toInvoiceDTOList(invoices);
    }

    /**
     * Get all invoices.
     *
     * @return List of all invoices
     */
    @Transactional
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
    @Transactional
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }
    
    /**
     * Get an invoice DTO by ID.
     *
     * @param id The invoice ID
     * @return The invoice DTO
     * @throws ResourceNotFoundException if invoice is not found
     */
    @Transactional
    public InvoiceDTO getInvoiceDTOById(Long id) {
        Invoice invoice = getInvoiceById(id);
        return dtoMapperService.toInvoiceDTO(invoice);
    }

    /**
     * Get an invoice with details by ID.
     *
     * @param id The invoice ID
     * @return The invoice with details
     * @throws ResourceNotFoundException if invoice is not found
     */
    @Transactional
    public Invoice getInvoiceWithDetailsById(Long id) {
        return invoiceRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }
    
    /**
     * Get an invoice with details by ID as a DTO.
     *
     * @param id The invoice ID
     * @return The invoice DTO with details
     * @throws ResourceNotFoundException if invoice is not found
     */
    @Transactional
    public InvoiceDTO getInvoiceWithDetailsDTOById(Long id) {
        Invoice invoice = getInvoiceWithDetailsById(id);
        return dtoMapperService.toInvoiceDTO(invoice);
    }
    
    /**
     * Get PDF data for an invoice.
     *
     * @param id The invoice ID
     * @return The PDF data or null if not available
     * @throws ResourceNotFoundException if invoice is not found
     */
    @Transactional
    public byte[] getPdfById(Long id) {
        Invoice invoice = getInvoiceById(id);
        return invoice.getPdf();
    }

    /**
     * Get invoices by billing ID.
     *
     * @param billingId The billing ID
     * @return List of invoices for the billing
     */
    @Transactional
    public List<Invoice> getInvoicesByBillingId(Long billingId) {
        return invoiceRepository.findByBillingId(billingId);
    }
    
    /**
     * Get invoice DTOs by billing ID.
     *
     * @param billingId The billing ID
     * @return List of invoice DTOs for the billing
     */
    @Transactional
    public List<InvoiceDTO> getInvoiceDTOsByBillingId(Long billingId) {
        List<Invoice> invoices = getInvoicesByBillingId(billingId);
        return dtoMapperService.toInvoiceDTOList(invoices);
    }

    /**
     * Get invoices by property ID.
     *
     * @param propertyId The property ID
     * @return List of invoices for the property
     */
    @Transactional
    public List<Invoice> getInvoicesByPropertyId(Long propertyId) {
        return invoiceRepository.findByPropertyId(propertyId);
    }
    
    /**
     * Get invoice DTOs by property ID.
     *
     * @param propertyId The property ID
     * @return List of invoice DTOs for the property
     */
    @Transactional
    public List<InvoiceDTO> getInvoiceDTOsByPropertyId(Long propertyId) {
        List<Invoice> invoices = getInvoicesByPropertyId(propertyId);
        return dtoMapperService.toInvoiceDTOList(invoices);
    }

    /**
     * Get invoices by status.
     *
     * @param status The invoice status
     * @return List of invoices with the status
     */
    @Transactional
    public List<Invoice> getInvoicesByStatus(Invoice.InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }
    
    /**
     * Get invoice DTOs by status.
     *
     * @param status The invoice status
     * @return List of invoice DTOs with the status
     */
    @Transactional
    public List<InvoiceDTO> getInvoiceDTOsByStatus(Invoice.InvoiceStatus status) {
        List<Invoice> invoices = getInvoicesByStatus(status);
        return dtoMapperService.toInvoiceDTOList(invoices);
    }

    /**
     * Find invoice by OCR number.
     *
     * @param ocrNumber The OCR number
     * @return The invoice if found
     * @throws ResourceNotFoundException if invoice is not found
     */
    @Transactional
    public Invoice findInvoiceByOcr(String ocrNumber) {
        return invoiceRepository.findByOcrNumber(ocrNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with OCR number: " + ocrNumber));
    }
    
    /**
     * Find invoice DTO by OCR number.
     *
     * @param ocrNumber The OCR number
     * @return The invoice DTO if found
     * @throws ResourceNotFoundException if invoice is not found
     */
    @Transactional
    public InvoiceDTO findInvoiceDTOByOcr(String ocrNumber) {
        Invoice invoice = findInvoiceByOcr(ocrNumber);
        return dtoMapperService.toInvoiceDTO(invoice);
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
        
        // Get association for PDF generation
        Association association = associationService.getAssociationEntity();
        
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
                    totalShareRatio);
            
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
            Invoice savedInvoice = invoiceRepository.save(invoice);
            generatedInvoices.add(savedInvoice);
            
            // Get owner (main contact) for PDF generation
            Owner owner = property.getMainContact();
            
            // Generate and store PDF for the invoice if owner exists
            if (owner != null) {
                try {
                    // Use direct method to avoid circular dependency
                    byte[] pdfBytes = pdfGenerationService.generateInvoicePdfDirect(
                            savedInvoice, billing, owner, association);
                    
                    // Store PDF
                    savedInvoice.setPdf(pdfBytes);
                    invoiceRepository.save(savedInvoice);
                } catch (Exception e) {
                    // Log error but continue with next invoice
                    System.err.println("Error generating PDF for invoice " + savedInvoice.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
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
     * Update invoice status and return DTO.
     *
     * @param id The invoice ID
     * @param status The new status
     * @return The updated invoice DTO
     * @throws ResourceNotFoundException if invoice is not found
     */
    @Transactional
    public InvoiceDTO updateInvoiceStatusDTO(Long id, Invoice.InvoiceStatus status) {
        Invoice updatedInvoice = updateInvoiceStatus(id, status);
        return dtoMapperService.toInvoiceDTO(updatedInvoice);
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
     * Mark invoice as sent and return DTO.
     *
     * @param id The invoice ID
     * @return The updated invoice DTO
     * @throws ResourceNotFoundException if invoice is not found
     */
    @Transactional
    public InvoiceDTO markInvoiceAsSentDTO(Long id) {
        Invoice updatedInvoice = markInvoiceAsSent(id);
        return dtoMapperService.toInvoiceDTO(updatedInvoice);
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
     * Get billing information for an invoice.
     *
     * @param invoiceId The invoice ID
     * @return The billing DTO
     * @throws ResourceNotFoundException if invoice or billing is not found
     */
    @Transactional
    public BillingDTO getBillingForInvoice(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        if (invoice.getBilling() == null) {
            throw new ResourceNotFoundException("Billing not found for invoice with id: " + invoiceId);
        }
        
        return dtoMapperService.toBillingDTO(invoice.getBilling());
    }
    
    /**
     * Get owner information for an invoice.
     *
     * @param invoiceId The invoice ID
     * @return The owner DTO
     * @throws ResourceNotFoundException if invoice, property, or owner is not found
     */
    @Transactional
    public OwnerDTO getOwnerForInvoice(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        if (invoice.getProperty() == null) {
            throw new ResourceNotFoundException("Property not found for invoice with id: " + invoiceId);
        }
        
        if (invoice.getProperty().getMainContact() == null) {
            throw new ResourceNotFoundException("Main contact not found for invoice with id: " + invoiceId);
        }
        
        return dtoMapperService.toOwnerDTO(invoice.getProperty().getMainContact());
    }
    
    /**
     * Store a PDF for an invoice.
     *
     * @param invoiceId The invoice ID
     * @param pdfBytes The PDF content
     * @throws ResourceNotFoundException if invoice is not found
     */
    @Transactional
    public void storePdf(Long invoiceId, byte[] pdfBytes) {
        Invoice invoice = getInvoiceById(invoiceId);
        invoice.setPdf(pdfBytes);
        invoiceRepository.save(invoice);
    }
    
    /**
     * Calculate invoice amount based on share ratio.
     * 
     * @param totalAmount The total amount
     * @param shareRatio The property's share ratio
     * @param totalShareRatio The total share ratio of all properties
     * @return The calculated amount for the invoice
     */
    public BigDecimal calculateInvoiceAmount(
            BigDecimal totalAmount,
            BigDecimal shareRatio,
            BigDecimal totalShareRatio) {

        BigDecimal amount = totalAmount
                .multiply(shareRatio)
                .divide(totalShareRatio, 2, RoundingMode.HALF_UP);

        // Avrunda till hela kronor (ingen decimal)
        return amount.setScale(0, RoundingMode.HALF_UP);
    }
}
