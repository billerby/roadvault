<template>
  <v-card
    class="bath-temperature-widget"
    :color="getTemperatureColor"
    :loading="loading"
  >
    <v-card-title class="d-flex justify-space-between align-center">
      <span>Badtemperatur vid båtbryggan</span>
      <v-icon
        size="large"
        class="mr-2"
      >{{ getTemperatureIcon }}</v-icon>
    </v-card-title>
    
    <v-card-text>
      <div v-if="!loading && temperature">
        <div class="temperature-value text-h3 font-weight-bold">
          {{ temperature.temperature }}°C
        </div>
        <div class="temperature-details text-body-2 mt-2">
          <div>Uppdaterad: {{ formattedDate }}</div>
          <div v-if="temperature.deviceId">Sensor: {{ temperature.deviceId }}</div>
        </div>
      </div>
      <div v-else-if="!loading && error" class="text-center">
        <v-icon color="warning" size="large">mdi-alert</v-icon>
        <div class="text-body-1 mt-2">{{ error }}</div>
      </div>
      <div v-else-if="loading" class="text-center py-4">
        <v-progress-circular
          indeterminate
          color="primary"
        />
      </div>
    </v-card-text>
    
    <v-card-actions>
      <v-spacer />
      <v-btn
        variant="text"
        color="primary"
        size="small"
        @click="fetchTemperature"
        :disabled="loading"
      >
        <v-icon class="mr-1">mdi-refresh</v-icon>
        Uppdatera
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
import { format } from 'date-fns';
import bathTemperatureService from '../services/bathTemperature.service';
import authService from '../services/auth.service';

export default {
  name: 'BathTemperatureWidget',
  
  data() {
    return {
      temperature: null,
      loading: false,
      error: null
    };
  },
  
  computed: {
    formattedDate() {
      if (!this.temperature || !this.temperature.receivedAt) return '';
      
      const date = new Date(this.temperature.receivedAt);
      return format(date, 'd MMMM yyyy HH:mm');
    },
    
    getTemperatureColor() {
      if (!this.temperature) return 'grey-lighten-3';
      
      const temp = this.temperature.temperature;
      
      if (temp < 10) return 'blue-darken-1';
      if (temp < 15) return 'blue';
      if (temp < 18) return 'light-blue';
      if (temp < 22) return 'green-lighten-1';
      if (temp < 25) return 'amber-lighten-1';
      return 'deep-orange-lighten-1';
    },
    
    getTemperatureIcon() {
      if (!this.temperature) return 'mdi-thermometer';
      
      const temp = this.temperature.temperature;
      
      if (temp < 10) return 'mdi-snowflake';
      if (temp < 15) return 'mdi-thermometer-low';
      if (temp < 18) return 'mdi-thermometer';
      if (temp < 22) return 'mdi-thermometer-high';
      if (temp < 25) return 'mdi-weather-sunny';
      return 'mdi-weather-sunny-alert';
    }
  },
  
  created() {
    this.fetchTemperature();
  },
  
  methods: {
    hasRole(role) {
      return authService.hasRole(role);
    },
    
    async fetchTemperature() {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await bathTemperatureService.getLatestTemperature();
        this.temperature = response.data;
      } catch (err) {
        console.error('Error fetching bath temperature:', err);
        this.error = 'Kunde inte hämta badtemperatur: ' + (err.response?.data?.message || err.message);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.bath-temperature-widget {
  min-height: 200px;
  transition: all 0.3s ease;
}

.temperature-value {
  text-align: center;
  margin-top: 10px;
}

.temperature-details {
  text-align: center;
  color: rgba(0, 0, 0, 0.6);
}
</style>
