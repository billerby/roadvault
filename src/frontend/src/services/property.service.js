// services/property.service.js
import api from './api';

const propertyService = {
  getAllProperties() {
    return api.get('/v1/properties');
  },

  getProperty(id) {
    return api.get(`/v1/properties/${id}`);
  },

  createProperty(property) {
    return api.post('/v1/properties', property);
  },

  updateProperty(id, property) {
    return api.put(`/v1/properties/${id}`, property);
  },

  deleteProperty(id) {
    return api.delete(`/v1/properties/${id}`);
  }
};

export default propertyService;