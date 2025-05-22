<!-- views/BathTemperatureView.vue -->
<template>
  <v-container fluid>
    <div class="mb-4">
      <h1 class="text-h4 mb-2">
        <v-icon class="mr-2" color="primary">mdi-waves</v-icon>
        Badtemperatur vid båtbryggan
      </h1>
      <p class="text-body-1 text-medium-emphasis">
        Realtidsdata och historisk utveckling av badtemperaturen vid Apelgårdens båtbrygga
      </p>
    </div>

    <v-row>
      <!-- Aktuell temperatur widget -->
      <v-col cols="12" lg="4">
        <BathTemperatureWidget />
      </v-col>
      
      <!-- Temperaturdiagram -->
      <v-col cols="12" lg="8">
        <BathTemperatureChart />
      </v-col>
    </v-row>

    <v-row class="mt-4">
      <v-col cols="12">
        <v-card>
          <v-card-title>
            <v-icon class="mr-2">mdi-information</v-icon>
            Om temperatursensorn
          </v-card-title>
          <v-card-text>
            <v-row>
              <v-col cols="12" md="6">
                <h3 class="text-h6 mb-2">Teknisk information</h3>
                <ul class="text-body-2">
                  <li><strong>Sensortyp:</strong> Digital temperaturmätare</li>
                  <li><strong>Mätfrekvens:</strong> Var 15:e minut</li>
                  <li><strong>Precision:</strong> ±0.1°C</li>
                  <li><strong>Placering:</strong> Vid båtbryggan, 0.5m djup</li>
                  <li><strong>Kommunikation:</strong> LoRaWAN via The Things Network</li>
                </ul>
              </v-col>
              <v-col cols="12" md="6">
                <h3 class="text-h6 mb-2">Badtips</h3>
                <div class="text-body-2">
                  <v-chip-group column>
                    <v-chip color="blue-darken-1" size="small">
                      <10°C: Mycket kallt, endast för härdade
                    </v-chip>
                    <v-chip color="blue" size="small">
                      10-15°C: Kallt, kort dopp
                    </v-chip>
                    <v-chip color="light-blue" size="small">
                      15-18°C: Svalt, uppfriskande
                    </v-chip>
                    <v-chip color="green-lighten-1" size="small">
                      18-22°C: Behagligt badväder
                    </v-chip>
                    <v-chip color="amber-lighten-1" size="small">
                      22-25°C: Varmt och skönt
                    </v-chip>
                    <v-chip color="deep-orange-lighten-1" size="small">
                      >25°C: Mycket varmt!
                    </v-chip>
                  </v-chip-group>
                </div>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import BathTemperatureWidget from '../components/BathTemperatureWidget.vue';
import BathTemperatureChart from '../components/BathTemperatureChart.vue';

export default {
  name: 'BathTemperatureView',
  components: {
    BathTemperatureWidget,
    BathTemperatureChart
  }
};
</script>

<style scoped>
.v-chip-group {
  gap: 8px;
}
</style>
