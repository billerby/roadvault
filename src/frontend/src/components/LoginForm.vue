<!-- components/LoginForm.vue -->
<template>
  <v-card class="mx-auto pa-4" max-width="500">
    <v-card-title class="text-center">Login to RoadVault</v-card-title>
    
    <v-alert
      v-if="error"
      type="error"
      dense
      class="mb-4"
    >
      {{ error }}
    </v-alert>
    
    <v-form @submit.prevent="handleLogin">
      <v-text-field
        v-model="username"
        label="Username"
        required
        prepend-icon="mdi-account"
        :error-messages="usernameError"
      ></v-text-field>
      
      <v-text-field
        v-model="password"
        label="Password"
        type="password"
        required
        prepend-icon="mdi-lock"
        :error-messages="passwordError"
      ></v-text-field>
      
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="primary"
          type="submit"
          :disabled="loading"
          :loading="loading"
        >
          Login
        </v-btn>
      </v-card-actions>
    </v-form>
  </v-card>
</template>

<script>
import authService from '../services/auth.service';

export default {
  name: 'LoginForm',
  
  data() {
    return {
      username: '',
      password: '',
      loading: false,
      error: null,
      usernameError: '',
      passwordError: ''
    };
  },
  
  methods: {
    validateForm() {
      let isValid = true;
      this.usernameError = '';
      this.passwordError = '';
      
      if (!this.username.trim()) {
        this.usernameError = 'Username is required';
        isValid = false;
      }
      
      if (!this.password) {
        this.passwordError = 'Password is required';
        isValid = false;
      }
      
      return isValid;
    },
    
    async handleLogin() {
      if (!this.validateForm()) return;
      
      this.loading = true;
      this.error = null;
      
      try {
        await authService.login({
          username: this.username,
          password: this.password
        });
        
        // Redirect to home page after successful login
        this.$router.push('/');
      } catch (err) {
        this.error = err.response?.data?.message || 'Login failed. Please try again.';
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>