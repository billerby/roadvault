// src/frontend/src/services/billing.service.js
import api from './api';

class BillingService {
  
  getAllBillings() {
    return api.get('/v1/billings');
  }
  
  getBillingById(id) {
    return api.get(`/v1/billings/${id}`);
  }
  
  getBillingsByYear(year) {
    return api.get(`/v1/billings/by-year?year=${year}`);
  }
  
  getBillingWithInvoices(id) {
    return api.get(`/v1/billings/${id}/with-invoices`);
  }
  
  getInvoicesForBilling(id) {
    return api.get(`/v1/billings/${id}/invoices`);
  }
  
  createBilling(billing, generateInvoices = false) {
    // Ensure generateInvoices is a proper boolean string value (true or false)
    const generateInvoicesParam = generateInvoices === true ? 'true' : 'false';
    return api.post(`/v1/billings?generateInvoices=${generateInvoicesParam}`, billing);
  }
  
  updateBilling(id, billing) {
    return api.put(`/v1/billings/${id}`, billing);
  }
  
  deleteBilling(id) {
    return api.delete(`/v1/billings/${id}`);
  }
  
  generateInvoices(id) {
    return api.post(`/v1/billings/${id}/generate-invoices`, {});
  }
}

export default new BillingService();
