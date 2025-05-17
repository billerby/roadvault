// services/owner.service.js
import api from './api';

const ownerService = {
  getAllOwners() {
    return api.get('/v1/owners');
  },

  getOwner(id) {
    return api.get(`/v1/owners/${id}`);
  },
  
  getOwnerWithProperties(id) {
    return api.get(`/v1/owners/${id}/with-properties`);
  },

  createOwner(owner) {
    return api.post('/v1/owners', owner);
  },

  updateOwner(id, owner) {
    return api.put(`/v1/owners/${id}`, owner);
  },

  deleteOwner(id) {
    return api.delete(`/v1/owners/${id}`);
  },
  
  searchOwnersByName(name) {
    return api.get(`/v1/owners/search?name=${encodeURIComponent(name)}`);
  }
};

export default ownerService;