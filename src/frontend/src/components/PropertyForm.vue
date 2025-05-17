<!-- components/PropertyForm.vue -->
<template>
  <v-form ref="form" @submit.prevent="submitForm">
    <v-container>
      <v-row>
        <v-col cols="12">
          <v-text-field
            v-model="formData.propertyDesignation"
            label="Fastighetsbeteckning"
            required
            :rules="[v => !!v || 'Fastighetsbeteckning är obligatorisk']"
          ></v-text-field>
        </v-col>
        
        <v-col cols="12">
          <v-text-field
            v-model.number="formData.shareRatio"
            label="Delaktighetstal"
            type="number"
            step="0.001"
            required
            :rules="[
              v => !!v || 'Delaktighetstal är obligatorisk',
              v => v > 0 || 'Delaktighetstal måste vara större än 0'
            ]"
          ></v-text-field>
        </v-col>
        
        <v-col cols="12">
          <v-text-field
            v-model="formData.address"
            label="Adress"
          ></v-text-field>
        </v-col>
      </v-row>
      
      <v-row>
        <v-col class="d-flex justify-end">
          <v-btn text @click="cancel">Avbryt</v-btn>
          <v-btn color="primary" type="submit" :loading="loading">Spara</v-btn>
        </v-col>
      </v-row>
    </v-container>
  </v-form>
</template>

<script>
import authService from '@/services/auth.service';
export default {
  name: 'PropertyForm',
  
  props: {
    property: {
      type: Object,
      required: true
    }
  },
  
  data() {
    return {
      formData: {
        id: null,
        propertyDesignation: '',
        shareRatio: 0,
        address: ''
      },
      loading: false
    };
  },
  
  watch: {
    property: {
      handler(newVal) {
        this.formData = { ...newVal };
      },
      immediate: true
    }
  },
  
  methods: {
    hasRole(role) {
      return authService.hasRole(role);
    },
    async submitForm() {
      if (this.$refs.form.validate()) {
        this.loading = true;
        try {
          this.$emit('save', { ...this.formData });
        } finally {
          this.loading = false;
        }
      }
    },
    
    cancel() {
      this.$emit('cancel');
    }
  }
};
</script>