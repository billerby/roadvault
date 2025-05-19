<template>
  <v-container>
    <v-card class="mx-auto my-4" max-width="600">
      <v-card-title class="headline">Change Password</v-card-title>
      <v-card-text>
        <v-alert v-if="successMessage" type="success" dismissible>
          {{ successMessage }}
        </v-alert>
        
        <v-alert v-if="errorMessage" type="error" dismissible>
          {{ errorMessage }}
        </v-alert>
        
        <v-form ref="form" v-model="valid" @submit.prevent="changePassword">
          <v-text-field
            v-model="currentPassword"
            :append-icon="showCurrentPassword ? 'mdi-eye' : 'mdi-eye-off'"
            :type="showCurrentPassword ? 'text' : 'password'"
            label="Current Password"
            :rules="[rules.required]"
            @click:append="showCurrentPassword = !showCurrentPassword"
          ></v-text-field>
          
          <v-text-field
            v-model="newPassword"
            :append-icon="showNewPassword ? 'mdi-eye' : 'mdi-eye-off'"
            :type="showNewPassword ? 'text' : 'password'"
            label="New Password"
            :rules="passwordRules"
            @click:append="showNewPassword = !showNewPassword"
          ></v-text-field>
          
          <v-text-field
            v-model="confirmPassword"
            :append-icon="showConfirmPassword ? 'mdi-eye' : 'mdi-eye-off'"
            :type="showConfirmPassword ? 'text' : 'password'"
            label="Confirm New Password"
            :rules="[rules.required, rules.passwordMatch]"
            @click:append="showConfirmPassword = !showConfirmPassword"
          ></v-text-field>
          
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              color="primary"
              :disabled="!valid || loading"
              :loading="loading"
              type="submit"
            >
              Change Password
            </v-btn>
          </v-card-actions>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script>
import authService from '@/services/auth.service';

export default {
  name: 'ProfileView',
  data() {
    return {
      valid: false,
      loading: false,
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
      showCurrentPassword: false,
      showNewPassword: false,
      showConfirmPassword: false,
      successMessage: '',
      errorMessage: '',
      rules: {
        required: v => !!v || 'This field is required',
        passwordMatch: v => v === this.newPassword || 'Passwords do not match',
        minLength: v => v.length >= 8 || 'Password must be at least 8 characters',
        hasUppercase: v => /[A-Z]/.test(v) || 'Password must contain at least one uppercase letter',
        hasLowercase: v => /[a-z]/.test(v) || 'Password must contain at least one lowercase letter',
        hasNumber: v => /\d/.test(v) || 'Password must contain at least one number',
      }
    };
  },
  computed: {
    passwordRules() {
      return [
        this.rules.required,
        this.rules.minLength,
        this.rules.hasUppercase,
        this.rules.hasLowercase,
        this.rules.hasNumber
      ];
    }
  },
  methods: {
    async changePassword() {
      if (!this.$refs.form.validate()) return;
      
      this.loading = true;
      this.successMessage = '';
      this.errorMessage = '';
      
      try {
        const response = await authService.changePassword({
          currentPassword: this.currentPassword,
          newPassword: this.newPassword,
          confirmPassword: this.confirmPassword
        });
        
        if (response.success) {
          this.successMessage = response.message;
          this.resetForm();
        } else {
          this.errorMessage = response.message || 'Failed to change password';
        }
      } catch (error) {
        this.errorMessage = error.response?.data?.message || 'An error occurred while changing password';
        console.error('Password change error:', error);
      } finally {
        this.loading = false;
      }
    },
    resetForm() {
      this.currentPassword = '';
      this.newPassword = '';
      this.confirmPassword = '';
      this.$refs.form.resetValidation();
    }
  }
};
</script>
