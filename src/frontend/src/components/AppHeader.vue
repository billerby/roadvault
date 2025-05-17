<!-- components/AppHeader.vue -->
<template>
  <v-app-bar app color="primary" dark>
    <v-toolbar-title>RoadVault</v-toolbar-title>
    
    <v-spacer></v-spacer>
    
    <div v-if="isAuthenticated">
      <v-btn text to="/">
        Hem
      </v-btn>
      <v-btn text to="/properties">
        Fastigheter
      </v-btn>
      <v-btn text @click="logout">
        Logga ut
      </v-btn>
    </div>
  </v-app-bar>
</template>

<script>
import authService from '../services/auth.service';

export default {
  name: 'AppHeader',
  
  data() {
    return {
      isAuthenticated: authService.isAuthenticated()
    };
  },
  
  created() {
    // Uppdatera autentiseringsstatus när komponenten skapas
    this.updateAuthStatus();
    
    // Lyssna på route-ändringar för att uppdatera autentiseringsstatus
    this.$router.beforeEach(() => {
      this.updateAuthStatus();
    });
  },
  
  methods: {
    updateAuthStatus() {
      this.isAuthenticated = authService.isAuthenticated();
    },
    
    logout() {
      authService.logout();
      this.$router.push('/login');
    }
  }
};
</script>