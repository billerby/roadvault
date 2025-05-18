import axios from 'axios';
import authHeader from './auth-header';

const API_URL = '/api/v1/payments';

/**
 * Service for managing payments in the frontend.
 */
class PaymentService {
  /**
   * Get all payments
   * @returns {Promise} Promise with all payments
   */
  getAllPayments() {
    return axios.get(API_URL, { headers: authHeader() });
  }

  /**
   * Get payment by ID
   * @param {number} id Payment ID
   * @returns {Promise} Promise with payment details
   */
  getPaymentById(id) {
    return axios.get(`${API_URL}/${id}`, { headers: authHeader() });
  }

  /**
   * Get payments by date range
   * @param {string} startDate Start date (YYYY-MM-DD)
   * @param {string} endDate End date (YYYY-MM-DD)
   * @returns {Promise} Promise with payments in the date range
   */
  getPaymentsByDateRange(startDate, endDate) {
    return axios.get(`${API_URL}/by-date-range?startDate=${startDate}&endDate=${endDate}`, { headers: authHeader() });
  }

  /**
   * Get payments by type
   * @param {string} type Payment type (BANKGIRO, POSTGIRO, SWISH, MANUAL, OTHER)
   * @returns {Promise} Promise with payments of the specified type
   */
  getPaymentsByType(type) {
    return axios.get(`${API_URL}/by-type?type=${type}`, { headers: authHeader() });
  }

  /**
   * Create a new payment
   * @param {Object} payment Payment data
   * @returns {Promise} Promise with the created payment
   */
  createPayment(payment) {
    return axios.post(API_URL, payment, { headers: authHeader() });
  }

  /**
   * Update a payment
   * @param {number} id Payment ID
   * @param {Object} payment Updated payment data
   * @returns {Promise} Promise with the updated payment
   */
  updatePayment(id, payment) {
    return axios.put(`${API_URL}/${id}`, payment, { headers: authHeader() });
  }

  /**
   * Delete a payment
   * @param {number} id Payment ID
   * @returns {Promise} Promise with no content
   */
  deletePayment(id) {
    return axios.delete(`${API_URL}/${id}`, { headers: authHeader() });
  }

  /**
   * Import payments from a CSV file
   * @param {File} file CSV file with payment data
   * @returns {Promise} Promise with import results
   */
  importPaymentsFromCsv(file) {
    const formData = new FormData();
    formData.append('file', file);
    
    return axios.post(`${API_URL}/import`, formData, {
      headers: {
        ...authHeader(),
        'Content-Type': 'multipart/form-data'
      }
    });
  }
}

export default new PaymentService();