<!-- views/PropertyView.vue -->
<template>
  <v-container>
    <v-row v-if="loading">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="var(--color-primary-light)" size="64"></v-progress-circular>
      </v-col>
    </v-row>
    
    <div v-else-if="property">
      <!-- Property Header -->
      <div class="rv-property-header">
        <v-row align="center">
          <v-col cols="auto">
            <v-btn
              icon
              to="/properties"
              class="rv-back-button"
              elevation="0"
            >
              <v-icon>mdi-arrow-left</v-icon>
            </v-btn>
          </v-col>
          <v-col>
            <h1 class="text-h2">{{ property.propertyDesignation }}</h1>
          </v-col>
        </v-row>
      </div>
      
      <!-- Property Details -->
      <v-card class="rv-card rv-mb-md">
        <v-card-title class="rv-card-header">
          <div class="rv-card-header__title">
            <v-icon>mdi-home-variant</v-icon>
            <span>Fastighetsdetaljer</span>
          </div>
          <div class="rv-card-header__actions">
            <v-btn class="rv-btn-icon rv-btn--primary">
              <v-icon>mdi-pencil</v-icon>
            </v-btn>
          </div>
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text class="rv-card-body">
          <v-row>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">Delaktighetstal</div>
              <div class="rv-detail-value">{{ property.shareRatio }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">Adress</div>
              <div class="rv-detail-value">{{ property.address || 'Ej angiven' }}</div>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>
      
      <!-- Owner Information -->
      <v-card class="rv-card rv-mb-md">
        <v-card-title class="rv-card-header">
          <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-account</v-icon>
          <span class="text-h5">Huvudkontakt</span>
          <v-spacer></v-spacer>
          <v-btn
            v-if="property.mainContact && hasRole('ADMIN')"
            class="rv-btn-icon rv-btn--primary"
            @click="editMainContact"
          >
            <v-icon>mdi-pencil</v-icon>
          </v-btn>
          <v-btn
            v-else-if="hasRole('ADMIN')"
            class="rv-btn-icon rv-btn--primary"
            @click="addMainContact"
          >
            <v-icon>mdi-plus</v-icon>
          </v-btn>
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text class="rv-p-md" v-if="property.mainContact">
          <v-row>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">Namn</div>
              <div class="rv-detail-value">{{ property.mainContact.name }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">E-post</div>
              <div class="rv-detail-value">
                <a v-if="property.mainContact.email" :href="`mailto:${property.mainContact.email}`">
                  {{ property.mainContact.email }}
                </a>
                <span v-else>Ej angiven</span>
              </div>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">Telefon</div>
              <div class="rv-detail-value">
                <a v-if="property.mainContact.phone" :href="`tel:${property.mainContact.phone}`">
                  {{ property.mainContact.phone }}
                </a>
                <span v-else>Ej angiven</span>
              </div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">Adress</div>
              <div class="rv-detail-value">{{ property.mainContact.address || 'Ej angiven' }}</div>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">Postnummer</div>
              <div class="rv-detail-value">{{ property.mainContact.postalCode || 'Ej angiven' }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">Ort</div>
              <div class="rv-detail-value">{{ property.mainContact.city || 'Ej angiven' }}</div>
            </v-col>
          </v-row>
        </v-card-text>
        <v-card-text v-else class="rv-p-md rv-empty-state">
          <div class="text-center">
            <v-icon size="64" color="grey lighten-1">mdi-account-question</v-icon>
            <p class="mt-4">Ingen huvudkontakt har tilldelats denna fastighet.</p>
            <v-btn 
              color="var(--color-primary-light)" 
              class="mt-4 rv-btn rv-btn--primary"
              
              @click="addMainContact"
            >
              <v-icon left>mdi-plus</v-icon>
              Lägg till huvudkontakt
            </v-btn>
          </div>
        </v-card-text>
      </v-card>
      
      <!-- All Owners -->
      <v-card class="rv-card rv-mb-md">
        <v-card-title class="rv-card-header">
          <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-account-group</v-icon>
          <span class="text-h5">Alla ägare</span>
          <v-spacer></v-spacer>
          <v-btn
            v-if="hasRole('ADMIN')"
            class="rv-btn-icon rv-btn--secondary"
            @click="addOwner"
          >
            <v-icon>mdi-plus</v-icon>
          </v-btn>
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text class="rv-p-md">
          <v-data-table
            :headers="ownerHeaders"
            :items="property.owners || []"
            :loading="loading"
            class="rv-table"
            :items-per-page="10"
            :footer-props="{ 
              'items-per-page-options': [5, 10, 15, -1],
              'items-per-page-text': 'Rader per sida'
            }"
          >
            <template v-slot:item.name="{ item }">
              <div class="d-flex align-center">
                <v-icon 
                  small 
                  color="amber darken-2" 
                  class="mr-2" 
                  v-if="isMainContact(item)"
                >
                  mdi-star
                </v-icon>
                {{ item.name }}
              </div>
            </template>
            
            <template v-slot:item.email="{ item }">
              <a v-if="item.email" :href="`mailto:${item.email}`">{{ item.email }}</a>
              <span v-else class="grey--text">Ej angiven</span>
            </template>
            
            <template v-slot:item.phone="{ item }">
              <a v-if="item.phone" :href="`tel:${item.phone}`">{{ item.phone }}</a>
              <span v-else class="grey--text">Ej angiven</span>
            </template>
            
            <template v-slot:item.actions="{ item }">
              <div class="rv-actions-container">
                <v-btn 
                  v-if="hasRole('ADMIN')"
                  class="rv-rv-btn-icon edit-action"
                  @click="editOwner(item)"
                >
                  <v-icon small>mdi-pencil</v-icon>
                </v-btn>
                <v-btn 
                  
                  class="rv-rv-btn-icon star-action"
                  @click="setAsMainContact(item)" 
                  v-if="!isMainContact(item) && hasRole('ADMIN')"
                >
                  <v-icon small>mdi-star-outline</v-icon>
                </v-btn>
                <v-icon 
                  small 
                  color="amber darken-2" 
                  class="star-icon"
                  v-else
                >
                  mdi-star
                </v-icon>
              </div>
            </template>
            
            <template v-slot:no-data>
              <div class="rv-empty-state text-center rv-p-md">
                <v-icon size="64" color="grey lighten-1">mdi-account-group</v-icon>
                <p class="mt-4">Inga ägare för denna fastighet.</p>
                <v-btn 
                  v-if="hasRole('ADMIN')"
                  color="var(--color-primary-light)" 
                  class="mt-4 rv-btn rv-btn--primary"
                  @click="addOwner"
                >
                  <v-icon left>mdi-plus</v-icon>
                  Lägg till ägare
                </v-btn>
              </div>
            </template>
          </v-data-table>
        </v-card-text>
      </v-card>
    </div>
    
    <v-alert
      v-if="error"
      type="error"
      class="rv-card rv-mb-md"
    >
      {{ error }}
    </v-alert>
    
    <!-- Property Edit Dialog -->
    <v-dialog v-model="propertyDialog" max-width="600px">
      <v-card class="rv-dialog">
        <v-card-title class="rv-dialog-header">
          <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-home-edit</v-icon>
          Redigera fastighet
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text class="rv-p-md">
          <PropertyForm 
            :property="editedProperty" 
            @save="saveProperty" 
            @cancel="propertyDialog = false"
          />
        </v-card-text>
      </v-card>
    </v-dialog>
    
    <!-- Owner Edit Dialog -->
    <v-dialog v-model="ownerDialog" max-width="600px">
      <v-card class="rv-dialog">
        <v-card-title class="rv-dialog-header">
          <v-icon color="var(--color-primary-dark)" class="mr-2">
            {{ editedOwner.id ? 'mdi-account-edit' : 'mdi-account-plus' }}
          </v-icon>
          {{ ownerFormTitle }}
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text class="rv-p-md">
          <OwnerForm 
            :owner="editedOwner" 
            @save="saveOwner" 
            @cancel="ownerDialog = false"
          />
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script>
import propertyService from '../services/property.service';
import ownerService from '../services/owner.service';
import PropertyForm from '../components/PropertyForm.vue';
import OwnerForm from '../components/OwnerForm.vue';
import authService from '../services/auth.service';

export default {
  name: 'PropertyView',
  components: {
    PropertyForm,
    OwnerForm
  },
  
  data() {
    return {
      property: null,
      loading: false,
      error: null,
      propertyDialog: false,
      ownerDialog: false,
      editedProperty: {},
      editedOwner: {},
      defaultOwner: {
        id: null,
        name: '',
        email: '',
        phone: '',
        address: '',
        postalCode: '',
        city: ''
      },
      isExistingOwner: false,
      ownerHeaders: [
        { title: 'Namn', key: 'name', sortable: true },
        { title: 'E-post', key: 'email', sortable: true },
        { title: 'Telefon', key: 'phone', sortable: true },
        { title: 'Åtgärder', key: 'actions', sortable: false, align: 'center', width: '120px' }
      ]
    };
  },
  
  computed: {
    propertyId() {
      return Number(this.$route.params.id);
    },
    
    ownerFormTitle() {
      return this.editedOwner.id ? 'Redigera ägare' : 'Lägg till ägare';
    }
  },
  
  created() {
    this.fetchProperty();
  },
  
  methods: {
    hasRole(role) {
      return authService.hasRole(role);
    },
    async fetchProperty() {
      this.loading = true;
      this.error = null;
      
      try {
        console.log('Hämtar fastighet med ID:', this.propertyId);
        const response = await propertyService.getProperty(this.propertyId);
        this.property = response.data;
      } catch (err) {
        console.error('Feldetaljer:', err);
        this.error = 'Misslyckades med att ladda fastighet: ' + (err.response?.data?.message || err.message);
        console.error('Fel vid hämtning av fastighet:', err);
      } finally {
        this.loading = false;
      }
    },
    
    editProperty() {
      this.editedProperty = { ...this.property };
      this.propertyDialog = true;
    },
    
    async saveProperty(property) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await propertyService.updateProperty(property.id, property);
        this.property = response.data;
        this.propertyDialog = false;
      } catch (err) {
        this.error = 'Misslyckades med att uppdatera fastighet: ' + (err.response?.data?.message || err.message);
        console.error('Fel vid uppdatering av fastighet:', err);
      } finally {
        this.loading = false;
      }
    },
    
    editMainContact() {
      if (this.property.mainContact) {
        this.editedOwner = { ...this.property.mainContact };
        this.isExistingOwner = true;
        this.ownerDialog = true;
      }
    },
    
    addMainContact() {
      this.editedOwner = { ...this.defaultOwner };
      this.isExistingOwner = false;
      this.ownerDialog = true;
    },
    
    editOwner(owner) {
      this.editedOwner = { ...owner };
      this.isExistingOwner = true;
      this.ownerDialog = true;
    },
    
    addOwner() {
      this.editedOwner = { ...this.defaultOwner };
      this.isExistingOwner = false;
      this.ownerDialog = true;
    },
    
    isMainContact(owner) {
      return this.property.mainContact && this.property.mainContact.id === owner.id;
    },
    
    async setAsMainContact(owner) {
      this.loading = true;
      this.error = null;
      
      try {
        await propertyService.updateProperty(this.property.id, {
          ...this.property,
          mainContact: { id: owner.id }
        });
        
        // Refresh property data
        await this.fetchProperty();
      } catch (err) {
        this.error = 'Misslyckades med att uppdatera huvudkontakt: ' + (err.response?.data?.message || err.message);
        console.error('Fel vid uppdatering av huvudkontakt:', err);
      } finally {
        this.loading = false;
      }
    },
    
    async saveOwner(owner) {
      this.loading = true;
      this.error = null;
      
      try {
        let response;
        
        if (owner.id) {
          // Update existing owner
          response = await ownerService.updateOwner(owner.id, owner);
        } else {
          // Create new owner
          response = await ownerService.createOwner(owner);
        }
        
        if (response.data.id) {
          if (!this.property.owners) {
            this.property.owners = [];
          }
          
          // If it's a new owner, add to the property's owners list
          if (!this.isExistingOwner) {
            // Add owner to property
            // This would typically be done via a backend API call
            // For now, we'll just update the local state
            const updatedOwners = [...this.property.owners, response.data];
            
            await propertyService.updateProperty(this.property.id, {
              ...this.property,
              owners: updatedOwners
            });
          }
          
          // If there's no main contact or user chose to set this as main contact
          if (!this.property.mainContact) {
            await propertyService.updateProperty(this.property.id, {
              ...this.property,
              mainContact: { id: response.data.id }
            });
          }
        }
        
        // Refresh property data
        await this.fetchProperty();
        this.ownerDialog = false;
      } catch (err) {
        this.error = 'Misslyckades med att spara ägare: ' + (err.response?.data?.message || err.message);
        console.error('Fel vid sparande av ägare:', err);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>


.edit-action {
  background-color: var(--color-secondary-light) !important;
  color: white !important;
}

.star-action {
  background-color: #FFC107 !important; /* Amber */
  color: white !important;
}

.star-icon {
  margin: 0 4px;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
}


.custom-table {
  border: none;
  border-radius: var(--card-border-radius);
}


</style>
