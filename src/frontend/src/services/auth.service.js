// services/auth.service.js
import api from './api';
import { ref } from 'vue';

// Create a reactive authentication state
const isUserAuthenticated = ref(!!localStorage.getItem('token'));

const authService = {
  // Reactive authentication state that components can watch
  isUserAuthenticated,
  
  async login(credentials) {
    try {
      const response = await api.post('/v1/auth/login', credentials);
      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('user', JSON.stringify({
          id: response.data.id,
          username: response.data.username,
          email: response.data.email,
          roles: response.data.roles
        }));
        // Update reactive state
        isUserAuthenticated.value = true;
      }
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    // Update reactive state
    isUserAuthenticated.value = false;
  },

  getCurrentUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },

  isAuthenticated() {
    return isUserAuthenticated.value;
  },

  hasRole(role) {
    const user = this.getCurrentUser();
    return user && user.roles && user.roles.includes(role);
  },
  
  async changePassword(passwordData) {
    try {
      const response = await api.post('/v1/auth/change-password', passwordData);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};

export default authService;