<!-- App.vue -->
<template>
  <v-app>
    <v-app-bar app elevation="2" class="app-header">
      <v-app-bar-nav-icon color="white" @click="drawer = !drawer"></v-app-bar-nav-icon>
      
      <v-toolbar-title class="text-white text-h5 font-weight-medium">
        <v-icon large color="white" class="mr-2">mdi-road-variant</v-icon>
        RoadVault
      </v-toolbar-title>
      
      <v-spacer></v-spacer>
      
      <div v-if="isAuthenticated" class="d-flex align-center">
        <v-btn icon color="white" class="mr-3" title="Notifikationer">
          <v-icon>mdi-bell-outline</v-icon>
        </v-btn>
        
        <v-menu
          transition="slide-y-transition"
          bottom
          offset-y
        >
          <template v-slot:activator="{ props }">
            <v-btn 
              v-bind="props"
              color="white" 
              variant="text"
              prepend-icon="mdi-account-circle"
              class="text-subtitle-1 font-weight-medium"
            >
              Admin
              <v-icon right>mdi-chevron-down</v-icon>
            </v-btn>
          </template>
          <v-list>
            <v-list-item @click="logout">
              <template v-slot:prepend>
                <v-icon color="error">mdi-logout</v-icon>
              </template>
              <v-list-item-title>Logga ut</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </div>
    </v-app-bar>
    
    <v-navigation-drawer
      v-model="drawer"
      app
      class="primary lighten-5"
      floating
    >
      <v-list-item
        prepend-avatar="https://ui-avatars.com/api/?name=Admin&background=2E7D32&color=fff"
        title="Admin"
        subtitle="admin@example.com"
        class="py-4 primary lighten-4"
      ></v-list-item>
      
      <v-divider></v-divider>
      
      <v-list density="compact" nav class="py-0">
        <v-list-item 
          to="/" 
          :active="$route.path === '/'"
          active-color="primary"
          prepend-icon="mdi-view-dashboard"
          title="Översikt"
          rounded="lg"
          class="my-1"
        ></v-list-item>
        
        <v-list-item 
          to="/properties" 
          :active="$route.path.startsWith('/properties')"
          active-color="primary"
          prepend-icon="mdi-home-group"
          title="Fastigheter"
          rounded="lg"
          class="my-1"
        ></v-list-item>
        
        <v-list-item 
          prepend-icon="mdi-file-document-multiple"
          title="Fakturor"
          rounded="lg"
          class="my-1"
        ></v-list-item>
        
        <v-list-item 
          prepend-icon="mdi-cash-multiple"
          title="Betalningar"
          rounded="lg"
          class="my-1"
        ></v-list-item>
      </v-list>
      
      <template v-slot:append>
        <div class="pa-4">
          <v-btn 
            prepend-icon="mdi-cog"
            block 
            color="primary"
            variant="outlined"
          >
            Inställningar
          </v-btn>
        </div>
      </template>
    </v-navigation-drawer>
    
    <v-main class="bg-background">
      <v-container class="py-8 px-6">
        <router-view></router-view>
      </v-container>
    </v-main>
    
    <v-footer app class="bg-surface py-4">
      <v-row no-gutters justify="center" align="center">
        <span class="text-caption text-medium-emphasis">
          &copy; {{ new Date().getFullYear() }} RoadVault
        </span>
      </v-row>
    </v-footer>
  </v-app>
</template>

<script>
import authService from './services/auth.service';

export default {
  name: 'App',
  
  data() {
    return {
      drawer: null,
    };
  },
  
  computed: {
    isAuthenticated() {
      return authService.isAuthenticated();
    }
  },
  
  methods: {
    logout() {
      authService.logout();
      this.$router.push('/login');
    }
  }
};
</script>

<style>
.app-header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.text-white {
  color: white !important;
}

.v-navigation-drawer.primary {
  color: rgba(0, 0, 0, 0.87);
}
</style>