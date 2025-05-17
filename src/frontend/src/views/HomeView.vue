<!-- views/HomeView.vue -->
<template>
  <v-container>
    <v-row class="text-center">
      <v-col cols="12">
        <h1 class="text-h3 mb-4">Välkommen til Apelgårdens vägsamfällighet</h1>
        <p class="text-body-1">
          RoadVault är vårt digitala verktyg för att hantera vägsamfällighetens ekonomi och fastigheter.
        </p>
        
        <v-card class="mt-5 pa-4">
          <v-card-title class="text-h5">Snabblänkar</v-card-title>
          <v-card-text>
            <v-row>
              <v-col cols="12" md="6">
                <v-btn block color="primary" to="/properties" class="mb-2">
                  <v-icon left>mdi-home</v-icon>
                  Hantera fastigheter
                </v-btn>
              </v-col>
              <v-col cols="12" md="6">
                <v-btn block color="secondary" to="/properties" class="mb-2">
                  <v-icon left>mdi-file-document</v-icon>
                  Se fakturor
                </v-btn>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
export default {
  name: 'HomeView'
};
</script>