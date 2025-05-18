import api from './api';

/**
 * Service for managing invoices in the frontend.
 */
class InvoiceService {
  /**
   * Get all invoices
   * @returns {Promise} Promise with all invoices
   */
  getAllInvoices() {
    return api.get('/v1/invoices');
  }

  /**
   * Get invoice by ID
   * @param {number} id Invoice ID
   * @returns {Promise} Promise with invoice details
   */
  getInvoiceById(id) {
    return api.get(`/v1/invoices/${id}`);
  }

  /**
   * Get invoices by property ID
   * @param {number} propertyId Property ID
   * @returns {Promise} Promise with invoices for the property
   */
  getInvoicesByProperty(propertyId) {
    return api.get(`/v1/invoices/by-property/${propertyId}`);
  }

  /**
   * Get invoices by status
   * @param {string} status Invoice status (CREATED, SENT, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED)
   * @returns {Promise} Promise with invoices matching the status
   */
  getInvoicesByStatus(status) {
    return api.get(`/v1/invoices/by-status?status=${status}`);
  }

  /**
   * Find invoice by OCR number
   * @param {string} ocrNumber OCR number
   * @returns {Promise} Promise with the matching invoice
   */
  findInvoiceByOcr(ocrNumber) {
    return api.get(`/v1/invoices/by-ocr?ocrNumber=${ocrNumber}`);
  }

  /**
   * Update invoice status
   * @param {number} id Invoice ID
   * @param {string} status New status (CREATED, SENT, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED)
   * @returns {Promise} Promise with the updated invoice
   */
  updateInvoiceStatus(id, status) {
    return api.put(`/v1/invoices/${id}/status?status=${status}`, {});
  }

  /**
   * Mark invoice as sent
   * @param {number} id Invoice ID
   * @returns {Promise} Promise with the updated invoice
   */
  markInvoiceAsSent(id) {
    return api.put(`/v1/invoices/${id}/mark-as-sent`, {});
  }

  /**
   * Mark overdue invoices
   * @returns {Promise} Promise with the count of marked invoices
   */
  markOverdueInvoices() {
    return api.post(`/v1/invoices/mark-overdue`, {});
  }

  /**
   * Get payments for an invoice
   * @param {number} invoiceId Invoice ID
   * @returns {Promise} Promise with the invoice payments
   */
  getInvoicePayments(invoiceId) {
    return api.get(`/v1/invoices/${invoiceId}/payments`);
  }

  /**
   * Register a payment for an invoice
   * @param {number} invoiceId Invoice ID
   * @param {Object} payment Payment data
   * @returns {Promise} Promise with the registered payment
   */
  registerPayment(invoiceId, payment) {
    return api.post(`/v1/invoices/${invoiceId}/payments`, payment);
  }

  /**
   * Update a payment
   * @param {number} paymentId Payment ID
   * @param {Object} payment Updated payment data
   * @returns {Promise} Promise with the updated payment
   */
  updatePayment(paymentId, payment) {
    return api.put(`/v1/payments/${paymentId}`, payment);
  }

  /**
   * Delete a payment
   * @param {number} paymentId Payment ID
   * @returns {Promise} Promise with no content
   */
  deletePayment(paymentId) {
    return api.delete(`/v1/payments/${paymentId}`);
  }

  /**
   * Send invoice reminders
   * @param {Array} invoiceIds Array of invoice IDs to send reminders for
   * @returns {Promise} Promise with the count of sent reminders
   */
  sendReminders(invoiceIds) {
    return api.post(`/v1/invoices/send-reminders`, { invoiceIds });
  }

  /**
   * Export invoices to PDF
   * @param {Array} invoiceIds Array of invoice IDs to export
   * @returns {Promise} Promise with the PDF blob
   */
  exportInvoicesToPdf(invoiceIds) {
    return api.post(`/v1/invoices/export-pdf`, { invoiceIds }, { 
      responseType: 'blob'
    });
  }

  /**
   * Get invoice statistics
   * @returns {Promise} Promise with invoice statistics
   */
  getInvoiceStatistics() {
    return api.get(`/v1/invoices/statistics`);
  }
}

export default new InvoiceService();