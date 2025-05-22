<!-- views/HomeView.vue -->
<template>
  <v-container>
    <v-row>
      <v-col cols="12" md="8">
        <div class="text-center">
          <h1 class="text-h3 mb-4">Välkommen till Apelgårdens vägsamfällighet</h1>
          <p class="text-body-1">
            RoadVault är vårt digitala verktyg för att hantera vägsamfällighetens ekonomi och fastigheter.
          </p>
          
          
        </div>
      </v-col>
      
      <v-col cols="12" md="4">
        <BathTemperatureWidget class="mt-md-10" />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import BathTemperatureWidget from '../components/BathTemperatureWidget.vue';

export default {
  name: 'HomeView',
  components: {
    BathTemperatureWidget
  }
};
</script>