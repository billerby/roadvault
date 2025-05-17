// main.js
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import vuetify from './plugins/vuetify'; // Anta att du har en Vuetify-konfigurationsfil

// Import custom styles
import './assets/styles/custom.css';

createApp(App)
  .use(router)
  .use(vuetify)
  .mount('#app');