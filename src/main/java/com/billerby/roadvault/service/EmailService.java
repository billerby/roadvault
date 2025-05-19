package com.billerby.roadvault.service;

import com.billerby.roadvault.dto.InvoiceDTO;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.List;

/**
 * Service interface for sending emails.
 */
public interface EmailService {
    
    /**
     * Send an invoice email to a recipient.
     *
     * @param invoiceDTO The invoice data to use
     * @param pdfAttachment PDF attachment to include with the email
     * @throws MessagingException If there is an error sending the message
     * @throws IOException If there is an error processing templates
     * @throws TemplateException If there is an error in the template
     */
    void sendInvoiceEmail(InvoiceDTO invoiceDTO, byte[] pdfAttachment) 
            throws MessagingException, IOException, TemplateException;
    
    /**
     * Send invoices to all recipients in the list.
     *
     * @param invoiceIds List of invoice IDs to send
     * @return Number of successfully sent emails
     */
    int sendInvoices(List<Long> invoiceIds);
    
    /**
     * Send invoices to all recipients for a specific billing.
     *
     * @param billingId The billing ID to send invoices for
     * @return Number of successfully sent emails
     */
    int sendInvoicesForBilling(Long billingId);
}
