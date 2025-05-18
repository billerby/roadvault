import axios from 'axios';
import authHeader from './auth-header';

const API_URL = '/api/v1/invoices';

/**
 * Service for managing invoices in the frontend.
 */
class InvoiceService {
  /**
   * Get all invoices
   * @returns {Promise} Promise with all invoices
   */
  getAllInvoices() {
    return axios.get(API_URL, { headers: authHeader() });
  }

  /**
   * Get invoice by ID
   * @param {number} id Invoice ID
   * @returns {Promise} Promise with invoice details
   */
  getInvoiceById(id) {
    return axios.get(`${API_URL}/${id}`, { headers: authHeader() });
  }

  /**
   * Get invoices by property ID
   * @param {number} propertyId Property ID
   * @returns {Promise} Promise with invoices for the property
   */
  getInvoicesByProperty(propertyId) {
    return axios.get(`${API_URL}/by-property/${propertyId}`, { headers: authHeader() });
  }

  /**
   * Get invoices by status
   * @param {string} status Invoice status (CREATED, SENT, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED)
   * @returns {Promise} Promise with invoices matching the status
   */
  getInvoicesByStatus(status) {
    return axios.get(`${API_URL}/by-status?status=${status}`, { headers: authHeader() });
  }

  /**
   * Find invoice by OCR number
   * @param {string} ocrNumber OCR number
   * @returns {Promise} Promise with the matching invoice
   */
  findInvoiceByOcr(ocrNumber) {
    return axios.get(`${API_URL}/by-ocr?ocrNumber=${ocrNumber}`, { headers: authHeader() });
  }

  /**
   * Update invoice status
   * @param {number} id Invoice ID
   * @param {string} status New status (CREATED, SENT, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED)
   * @returns {Promise} Promise with the updated invoice
   */
  updateInvoiceStatus(id, status) {
    return axios.put(`${API_URL}/${id}/status?status=${status}`, {}, { headers: authHeader() });
  }

  /**
   * Mark invoice as sent
   * @param {number} id Invoice ID
   * @returns {Promise} Promise with the updated invoice
   */
  markInvoiceAsSent(id) {
    return axios.put(`${API_URL}/${id}/mark-as-sent`, {}, { headers: authHeader() });
  }

  /**
   * Mark overdue invoices
   * @returns {Promise} Promise with the count of marked invoices
   */
  markOverdueInvoices() {
    return axios.post(`${API_URL}/mark-overdue`, {}, { headers: authHeader() });
  }

  /**
   * Get payments for an invoice
   * @param {number} invoiceId Invoice ID
   * @returns {Promise} Promise with the invoice payments
   */
  getInvoicePayments(invoiceId) {
    return axios.get(`${API_URL}/${invoiceId}/payments`, { headers: authHeader() });
  }

  /**
   * Register a payment for an invoice
   * @param {number} invoiceId Invoice ID
   * @param {Object} payment Payment data
   * @returns {Promise} Promise with the registered payment
   */
  registerPayment(invoiceId, payment) {
    return axios.post(`${API_URL}/${invoiceId}/payments`, payment, { headers: authHeader() });
  }

  /**
   * Update a payment
   * @param {number} paymentId Payment ID
   * @param {Object} payment Updated payment data
   * @returns {Promise} Promise with the updated payment
   */
  updatePayment(paymentId, payment) {
    return axios.put(`/api/v1/payments/${paymentId}`, payment, { headers: authHeader() });
  }

  /**
   * Delete a payment
   * @param {number} paymentId Payment ID
   * @returns {Promise} Promise with no content
   */
  deletePayment(paymentId) {
    return axios.delete(`/api/v1/payments/${paymentId}`, { headers: authHeader() });
  }

  /**
   * Send invoice reminders
   * @param {Array} invoiceIds Array of invoice IDs to send reminders for
   * @returns {Promise} Promise with the count of sent reminders
   */
  sendReminders(invoiceIds) {
    return axios.post(`${API_URL}/send-reminders`, { invoiceIds }, { headers: authHeader() });
  }

  /**
   * Export invoices to PDF
   * @param {Array} invoiceIds Array of invoice IDs to export
   * @returns {Promise} Promise with the PDF blob
   */
  exportInvoicesToPdf(invoiceIds) {
    return axios.post(`${API_URL}/export-pdf`, { invoiceIds }, { 
      headers: { ...authHeader(), 'Content-Type': 'application/json' },
      responseType: 'blob'
    });
  }

  /**
   * Get invoice statistics
   * @returns {Promise} Promise with invoice statistics
   */
  getInvoiceStatistics() {
    return axios.get(`${API_URL}/statistics`, { headers: authHeader() });
  }
}

export default new InvoiceService();