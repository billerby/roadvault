<!-- views/PropertiesView.vue -->
<template>
  <v-container>
    <h1 class="text-h4 mb-4">Fastigheter i samfälligheten</h1>
    <PropertyList />
  </v-container>
</template>

<script>
import PropertyList from '../components/PropertyList.vue';

export default {
  name: 'PropertiesView',
  components: {
    PropertyList
  }
};
</script>