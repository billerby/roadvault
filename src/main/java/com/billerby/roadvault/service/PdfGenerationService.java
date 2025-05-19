package com.billerby.roadvault.service;

import com.billerby.roadvault.dto.*;
import com.billerby.roadvault.model.Association;
import com.billerby.roadvault.model.Billing;
import com.billerby.roadvault.model.Invoice;
import com.billerby.roadvault.model.Owner;
import com.billerby.roadvault.service.mapper.DTOMapperService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.fop.apps.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for generating PDF invoices using FreeMarker templates and Apache FOP.
 */
@Service
public class PdfGenerationService {
    private static final Logger logger = LoggerFactory.getLogger(PdfGenerationService.class);

    private final Configuration freemarkerConfig;
    private final FopFactory fopFactory;
    
    // Add service dependencies here
    private final InvoiceService invoiceService;
    private final AssociationService associationService;
    private final DTOMapperService dtoMapperService;
    
    @Autowired
    public PdfGenerationService(
        @Qualifier("freemarkerConfiguration") Configuration freemarkerConfig, 
        @Lazy InvoiceService invoiceService,
        AssociationService associationService,
        DTOMapperService dtoMapperService) {
        
        this.freemarkerConfig = freemarkerConfig;
        this.invoiceService = invoiceService;
        this.associationService = associationService;
        this.dtoMapperService = dtoMapperService;
        
        // Initialize FOP Factory using the non-deprecated approach
        try {
            // Create a temporary directory for FOP configuration if needed
            Path tempConfig = Files.createTempDirectory("fop-config");
            // Use FopFactoryBuilder to create FopFactory instance
            FopFactoryBuilder builder = new FopFactoryBuilder(tempConfig.toUri());
            this.fopFactory = builder.build();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing FOP Factory", e);
        }
    }
    
    /**
     * Generate a PDF invoice from an invoice ID.
     * 
     * @param invoiceId The ID of the invoice to generate
     * @return The PDF as a byte array
     * @throws IOException If there is an error reading templates
     * @throws TemplateException If there is an error processing templates
     * @throws FOPException If there is an error in FOP processing
     * @throws TransformerException If there is an error in XML transformation
     */
    public byte[] generateInvoicePdf(Long invoiceId) throws IOException, TemplateException, FOPException, TransformerException {
        // Get all the required data
        InvoiceDTO invoiceDTO = invoiceService.getInvoiceDTOById(invoiceId);
        BillingDTO billingDTO = invoiceService.getBillingForInvoice(invoiceId);
        AssociationDTO associationDTO = associationService.getAssociation();
        OwnerDTO ownerDTO = invoiceService.getOwnerForInvoice(invoiceId);
        
        // Create composite DTO for template
        InvoicePdfDTO invoicePdfDTO = InvoicePdfDTO.from(invoiceDTO, billingDTO, associationDTO, ownerDTO);
        
        // Process the FreeMarker template to generate XSL-FO
        String xslFo = processTemplate(invoicePdfDTO);
        
        // Convert XSL-FO to PDF
        return generatePdf(xslFo);
    }
    
    /**
     * Generate a PDF for a newly created invoice directly from entity objects.
     * This method avoids circular dependencies when used in invoice generation flow.
     * 
     * @param invoice The invoice entity
     * @param billing The billing entity
     * @param owner The owner entity
     * @param association The association entity
     * @return The generated PDF as a byte array
     */
    public byte[] generateInvoicePdfDirect(
            Invoice invoice, 
            Billing billing, 
            Owner owner, 
            Association association) throws IOException, TemplateException, FOPException, TransformerException {
        
        // Convert entities to DTOs
        InvoiceDTO invoiceDTO = dtoMapperService.toInvoiceDTO(invoice);
        BillingDTO billingDTO = dtoMapperService.toBillingDTO(billing);
        OwnerDTO ownerDTO = dtoMapperService.toOwnerDTO(owner);
        AssociationDTO associationDTO = dtoMapperService.toAssociationDTO(association);
        
        // Create composite DTO for template
        InvoicePdfDTO invoicePdfDTO = InvoicePdfDTO.from(invoiceDTO, billingDTO, associationDTO, ownerDTO);
        
        // Process the FreeMarker template to generate XSL-FO
        String xslFo = processTemplate(invoicePdfDTO);
        
        // Convert XSL-FO to PDF
        return generatePdf(xslFo);
    }
    
    /**
     * Process the FreeMarker template with the provided data.
     * 
     * @param invoicePdfDTO The data to use in the template
     * @return The processed XSL-FO as a string
     * @throws IOException If there is an error reading the template
     * @throws TemplateException If there is an error processing the template
     */
    private String processTemplate(InvoicePdfDTO invoicePdfDTO) throws IOException, TemplateException {
        // Get the template
        Template template = freemarkerConfig.getTemplate("invoice.ftlx");
        
        // Create a data model for the template
        Map<String, Object> model = new HashMap<>();
        model.put("invoice", invoicePdfDTO);
        
        // Process the template
        StringWriter out = new StringWriter();
        template.process(model, out);
        
        return out.toString();
    }
    
    /**
     * Convert XSL-FO to PDF using Apache FOP.
     * 
     * @param xslFo The XSL-FO markup to convert
     * @return The PDF as a byte array
     * @throws FOPException If there is an error in FOP processing
     * @throws TransformerException If there is an error in XML transformation
     * @throws IOException If there is an I/O error
     */
    private byte[] generatePdf(String xslFo) throws FOPException, TransformerException, IOException {
        // Create a ByteArrayOutputStream to store the PDF
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        
        // Setup FOP
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        
        // Configure FOP for PDF output
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfStream);
        
        // Setup JAXP using identity transformer
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(); // identity transformer
        
        // Create the source (XSL-FO)
        Source src = new StreamSource(new ByteArrayInputStream(xslFo.getBytes(StandardCharsets.UTF_8)));
        
        // Create the result (PDF)
        Result res = new SAXResult(fop.getDefaultHandler());
        
        // Transform XSL-FO to PDF
        transformer.transform(src, res);
        
        // Return the PDF bytes
        return pdfStream.toByteArray();
    }
    
    /**
     * Store the generated PDF in the database.
     * 
     * @param invoiceId The ID of the invoice
     * @param pdfBytes The PDF data to store
     */
    public void storePdfInDatabase(Long invoiceId, byte[] pdfBytes) {
        // Store the PDF data in the database, associating it with the invoice
        invoiceService.storePdf(invoiceId, pdfBytes);
    }
    
    /**
     * Generate and store a PDF invoice.
     * 
     * @param invoiceId The ID of the invoice
     * @return true if successful, false otherwise
     */
    public boolean generateAndStorePdf(Long invoiceId) {
        try {
            byte[] pdfBytes = generateInvoicePdf(invoiceId);
            storePdfInDatabase(invoiceId, pdfBytes);
            return true;
        } catch (Exception e) {
            // Log the exception
            logger.error("Error generating and storing PDF for invoice {}: {}", invoiceId, e.getMessage(), e);
            return false;
        }
    }
}