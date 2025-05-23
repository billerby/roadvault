package com.billerby.roadvault.service;

import com.billerby.roadvault.dto.AssociationDTO;
import com.billerby.roadvault.dto.InvoiceDTO;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of the EmailService for sending invoice emails.
 */
@Service
public class EmailServiceImpl implements EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    private final JavaMailSender mailSender;
    private final Configuration freemarkerConfig;
    private final InvoiceService invoiceService;
    private final PdfGenerationService pdfGenerationService;
    private final AssociationService associationService;
    
    @Value("${mail.fromAddress:info@apelgarden.se}")
    private String fromAddress;
    
    @Value("${mail.subject:Faktura från Apelgårdens vägsamfällighet}")
    private String emailSubject;
    
    @Autowired
    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Qualifier("freemarkerConfiguration") Configuration freemarkerConfig,
            InvoiceService invoiceService,
            PdfGenerationService pdfGenerationService,
            AssociationService associationService) {
        this.mailSender = mailSender;
        this.freemarkerConfig = freemarkerConfig;
        this.invoiceService = invoiceService;
        this.pdfGenerationService = pdfGenerationService;
        this.associationService = associationService;
    }

    @Override
    public void sendInvoiceEmail(InvoiceDTO invoiceDTO, byte[] pdfAttachment) 
            throws MessagingException, IOException, TemplateException {
        
        // Skip if recipient email is missing
        if (invoiceDTO.getOwnerEmail() == null || invoiceDTO.getOwnerEmail().trim().isEmpty()) {
            logger.warn("Cannot send invoice email: Recipient email is missing for invoice {}", invoiceDTO.getId());
            return;
        }
        
        // Get association data for payment information
        AssociationDTO associationDTO = associationService.getAssociation();
        
        // Prepare the email context data
        Map<String, Object> model = new HashMap<>();
        model.put("invoice", invoiceDTO);
        model.put("recipientName", invoiceDTO.getOwnerName());
        model.put("invoiceNumber", invoiceDTO.getInvoiceNumber());
        model.put("dueDate", invoiceDTO.getDueDate().toString());
        model.put("amount", invoiceDTO.getAmount());
        model.put("propertyDesignation", invoiceDTO.getPropertyDesignation());
        model.put("ocrNumber", invoiceDTO.getOcrNumber());
        model.put("plusgiroNumber", associationDTO.getPlusgiroNumber());
        model.put("bankgiroNumber", associationDTO.getBankgiroNumber());
        
        // Process the email template
        Template emailTemplate = freemarkerConfig.getTemplate("invoice-email-template.ftlh");
        String emailContent = FreeMarkerTemplateUtils.processTemplateIntoString(emailTemplate, model);
        
        // Create the email message
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        // Set email details
        helper.setTo(invoiceDTO.getOwnerEmail());
        helper.setFrom(fromAddress);
        helper.setSubject(emailSubject);
        helper.setText(emailContent, true); // true indicates HTML content
        
        // Attach the PDF invoice
        String filename = "faktura_" + invoiceDTO.getInvoiceNumber() + ".pdf";
        helper.addAttachment(filename, () -> java.nio.file.Files.newInputStream(java.nio.file.Files.write(
                java.nio.file.Files.createTempFile("invoice_", ".pdf"), pdfAttachment)));
        
        // Send the email
        mailSender.send(message);
        
        logger.info("Invoice email sent successfully to: {} for invoice: {}", 
                invoiceDTO.getOwnerEmail(), invoiceDTO.getInvoiceNumber());
    }
    
    @Override
    public int sendInvoices(List<Long> invoiceIds) {
        AtomicInteger successCount = new AtomicInteger(0);
        
        invoiceIds.forEach(invoiceId -> {
            try {
                InvoiceDTO invoiceDTO = invoiceService.getInvoiceWithDetailsDTOById(invoiceId);
                
                // Get or generate the PDF attachment
                byte[] pdfBytes = invoiceService.getPdfById(invoiceId);
                if (pdfBytes == null || pdfBytes.length == 0) {
                    // Generate and store the PDF
                    pdfBytes = pdfGenerationService.generateInvoicePdf(invoiceId);
                    pdfGenerationService.storePdfInDatabase(invoiceId, pdfBytes);
                }
                
                // Send the email with the PDF attachment
                sendInvoiceEmail(invoiceDTO, pdfBytes);
                
                // Mark invoice as sent
                invoiceService.markInvoiceAsSentDTO(invoiceId);
                
                // Increment success counter
                successCount.incrementAndGet();
                
            } catch (Exception e) {
                logger.error("Failed to send invoice email for invoice ID: {}", invoiceId, e);
            }
        });
        
        return successCount.get();
    }
    
    @Override
    public int sendInvoicesForBilling(Long billingId) {
        List<InvoiceDTO> invoiceDTOs = invoiceService.getInvoiceDTOsByBillingId(billingId);
        List<Long> invoiceIds = invoiceDTOs.stream()
                .map(InvoiceDTO::getId)
                .toList();
        
        return sendInvoices(invoiceIds);
    }
}
