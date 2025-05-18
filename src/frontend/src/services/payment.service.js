import api from './api';

/**
 * Service for managing payments in the frontend.
 */
class PaymentService {
  /**
   * Get all payments
   * @returns {Promise} Promise with all payments
   */
  getAllPayments() {
    return api.get('/v1/payments');
  }

  /**
   * Get payment by ID
   * @param {number} id Payment ID
   * @returns {Promise} Promise with payment details
   */
  getPaymentById(id) {
    return api.get(`/v1/payments/${id}`);
  }

  /**
   * Get payments by date range
   * @param {string} startDate Start date (YYYY-MM-DD)
   * @param {string} endDate End date (YYYY-MM-DD)
   * @returns {Promise} Promise with payments in the date range
   */
  getPaymentsByDateRange(startDate, endDate) {
    return api.get(`/v1/payments/by-date-range?startDate=${startDate}&endDate=${endDate}`);
  }

  /**
   * Get payments by type
   * @param {string} type Payment type (BANKGIRO, POSTGIRO, SWISH, MANUAL, OTHER)
   * @returns {Promise} Promise with payments of the specified type
   */
  getPaymentsByType(type) {
    return api.get(`/v1/payments/by-type?type=${type}`);
  }

  /**
   * Create a new payment
   * @param {Object} payment Payment data
   * @returns {Promise} Promise with the created payment
   */
  createPayment(payment) {
    return api.post('/v1/payments', payment);
  }

  /**
   * Update a payment
   * @param {number} id Payment ID
   * @param {Object} payment Updated payment data
   * @returns {Promise} Promise with the updated payment
   */
  updatePayment(id, payment) {
    return api.put(`/v1/payments/${id}`, payment);
  }

  /**
   * Delete a payment
   * @param {number} id Payment ID
   * @returns {Promise} Promise with no content
   */
  deletePayment(id) {
    return api.delete(`/v1/payments/${id}`);
  }

  /**
   * Import payments from a CSV file
   * @param {File} file CSV file with payment data
   * @returns {Promise} Promise with import results
   */
  importPaymentsFromCsv(file) {
    const formData = new FormData();
    formData.append('file', file);
    
    return api.post(`/v1/payments/import`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }
}

export default new PaymentService();