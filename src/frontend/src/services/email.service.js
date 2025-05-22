import api from './api';

/**
 * Service for managing email operations in the frontend.
 */
class EmailService {
  /**
   * Send a single invoice via email
   * @param {number} invoiceId Invoice ID
   * @returns {Promise} Promise with email send result
   */
  sendInvoiceEmail(invoiceId) {
    return api.post(`/v1/emails/invoices/${invoiceId}`);
  }

  /**
   * Send a reminder for a single invoice via email
   * @param {number} invoiceId Invoice ID
   * @returns {Promise} Promise with email send result
   */
  sendInvoiceReminder(invoiceId) {
    return api.post(`/v1/emails/invoices/${invoiceId}/reminder`);
  }

  /**
   * Send multiple invoices via email
   * @param {Array<number>} invoiceIds Array of invoice IDs
   * @returns {Promise} Promise with email send results
   */
  sendInvoiceEmails(invoiceIds) {
    return api.post('/v1/emails/invoices', { invoiceIds });
  }

  /**
   * Send all invoices for a billing via email
   * @param {number} billingId Billing ID
   * @returns {Promise} Promise with email send results
   */
  sendInvoicesForBilling(billingId) {
    return api.post(`/v1/emails/invoices/billing/${billingId}`);
  }
}

export default new EmailService();
