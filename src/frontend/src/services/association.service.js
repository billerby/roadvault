// src/frontend/src/services/association.service.js
import api from './api';

class AssociationService {
  
  getAssociation() {
    return api.get('/v1/association');
  }
  
  updateAssociation(association) {
    return api.put('/v1/association', association);
  }
  
  createAssociation(association) {
    return api.post('/v1/association', association);
  }
}

export default new AssociationService();
