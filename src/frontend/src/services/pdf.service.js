import api from './api';

/**
 * Service for managing PDF operations in the frontend.
 */
class PdfService {
  /**
   * Get PDF for an invoice
   * @param {number} invoiceId Invoice ID
   * @returns {Promise} Promise with the PDF blob
   */
  getInvoicePdf(invoiceId) {
    return api.get(`/v1/pdf/invoice/${invoiceId}`, { 
      responseType: 'blob',
      // Handle errors properly for blob responses
      validateStatus: status => {
        return status >= 200 && status < 300; // default
      },
      // Make sure to transform the response properly
      transformResponse: [(data) => {
        if (typeof data === 'string') {
          try {
            return JSON.parse(data);
          } catch (e) {
            // Not JSON, return as blob
            return data;
          }
        }
        return data;
      }]
    });
  }

  /**
   * Generate and store PDF for an invoice
   * @param {number} invoiceId Invoice ID
   * @returns {Promise} Promise with status message
   */
  generateAndStoreInvoicePdf(invoiceId) {
    return api.post(`/v1/pdf/invoice/${invoiceId}/generate`);
  }
}

export default new PdfService();