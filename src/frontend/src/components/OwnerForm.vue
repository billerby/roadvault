<!-- components/OwnerForm.vue -->
<template>
  <v-form ref="form" @submit.prevent="submitForm">
    <v-container>
      <v-row>
        <v-col cols="12">
          <v-text-field
            v-model="formData.name"
            label="Namn"
            required
            :rules="[v => !!v || 'Name is required']"
          ></v-text-field>
        </v-col>
        
        <v-col cols="12">
          <v-text-field
            v-model="formData.email"
            label="Epostadress"
            type="email"
          ></v-text-field>
        </v-col>
        
        <v-col cols="12">
          <v-text-field
            v-model="formData.phone"
            label="Telefonnummer"
          ></v-text-field>
        </v-col>
        
        <v-col cols="12">
          <v-text-field
            v-model="formData.address"
            label="Adress"
            required
            :rules="[v => !!v || 'Address is required']"
          ></v-text-field>
        </v-col>
        
        <v-col cols="6">
          <v-text-field
            v-model="formData.postalCode"
            label="Postnummer"
          ></v-text-field>
        </v-col>
        
        <v-col cols="6">
          <v-text-field
            v-model="formData.city"
            label="Postort"
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
export default {
  name: 'OwnerForm',
  
  props: {
    owner: {
      type: Object,
      required: true
    }
  },
  
  data() {
    return {
      formData: {
        id: null,
        name: '',
        email: '',
        phone: '',
        address: '',
        postalCode: '',
        city: ''
      },
      loading: false
    };
  },
  
  watch: {
    owner: {
      handler(newVal) {
        this.formData = { ...newVal };
      },
      immediate: true
    }
  },
  
  methods: {
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