/**
 * plugins/vuetify.js
 *
 * Framework documentation: https://vuetifyjs.com`
 */

// Styles
import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

// Composables
import { createVuetify } from 'vuetify'

// https://vuetifyjs.com/en/introduction/why-vuetify/#feature-guides
export default createVuetify({
  theme: {
    defaultTheme: 'roadVaultTheme',
    themes: {
      roadVaultTheme: {
        dark: false,
        colors: {
          primary: '#2E7D32',       // Skog/natur-grön för att representera landsbygd & vägar
          secondary: '#1565C0',     // Blå för vatten, himmel
          tertiary: '#795548',      // Jord/trä-ton för landsbygdsvägar
          accent: '#FF8F00',        // Varm accent (solnedgång/höst)
          error: '#D84315',
          info: '#0288D1',
          success: '#388E3C',
          warning: '#F57C00',
          background: '#FAFAFA',    // Ljust papper-vit bakgrund
          surface: '#FFFFFF',
        },
      },
    },
  },
  display: {
    mobileBreakpoint: 'sm',
    thresholds: {
      xs: 0,
      sm: 600,
      md: 960,
      lg: 1280,
      xl: 1920,
    },
  },
})
