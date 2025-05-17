<!-- views/PropertyView.vue -->
<template>
  <v-container>
    <v-row v-if="loading">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary"></v-progress-circular>
      </v-col>
    </v-row>
    
    <div v-else-if="property">
      <v-row>
        <v-col cols="12">
          <v-btn
            icon
            class="mb-4"
            to="/properties"
          >
            <v-icon>mdi-arrow-left</v-icon>
          </v-btn>
          <h1 class="text-h4">{{ property.propertyDesignation }}</h1>
        </v-col>
      </v-row>
      
      <!-- Property Details -->
      <v-card class="mb-4">
        <v-card-title class="text-h5">
          Fastighetsdetaljer
          <v-spacer></v-spacer>
          <v-btn
            icon
            @click="editProperty"
          >
            <v-icon>mdi-pencil</v-icon>
          </v-btn>
        </v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6">
              <div class="text-subtitle-1 font-weight-bold">Delaktighetstal</div>
              <div>{{ property.shareRatio }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="text-subtitle-1 font-weight-bold">Adress</div>
              <div>{{ property.address || 'Ej angiven' }}</div>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>
      
      <!-- Owner Information -->
      <v-card class="mb-4">
        <v-card-title class="text-h5">
          Huvudkontakt
          <v-spacer></v-spacer>
          <v-btn
            icon
            @click="editMainContact"
            v-if="property.mainContact"
          >
            <v-icon>mdi-pencil</v-icon>
          </v-btn>
          <v-btn
            icon
            @click="addMainContact"
            v-else
          >
            <v-icon>mdi-plus</v-icon>
          </v-btn>
        </v-card-title>
        <v-card-text v-if="property.mainContact">
          <v-row>
            <v-col cols="12" md="6">
              <div class="text-subtitle-1 font-weight-bold">Namn</div>
              <div>{{ property.mainContact.name }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="text-subtitle-1 font-weight-bold">E-post</div>
              <div>{{ property.mainContact.email || 'Ej angiven' }}</div>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="12" md="6">
              <div class="text-subtitle-1 font-weight-bold">Telefon</div>
              <div>{{ property.mainContact.phone || 'Ej angiven' }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="text-subtitle-1 font-weight-bold">Adress</div>
              <div>{{ property.mainContact.address || 'Ej angiven' }}</div>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="12" md="6">
              <div class="text-subtitle-1 font-weight-bold">Postnummer</div>
              <div>{{ property.mainContact.postalCode || 'Ej angiven' }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="text-subtitle-1 font-weight-bold">Ort</div>
              <div>{{ property.mainContact.city || 'Ej angiven' }}</div>
            </v-col>
          </v-row>
        </v-card-text>
        <v-card-text v-else>
          <p>Ingen huvudkontakt har tilldelats denna fastighet.</p>
          <v-btn color="primary" @click="addMainContact">Lägg till huvudkontakt</v-btn>
        </v-card-text>
      </v-card>
      
      <!-- All Owners -->
      <v-card class="mb-4">
        <v-card-title class="text-h5">
          Alla ägare
          <v-spacer></v-spacer>
          <v-btn
            icon
            @click="addOwner"
          >
            <v-icon>mdi-plus</v-icon>
          </v-btn>
        </v-card-title>
        <v-card-text>
          <v-data-table
            :headers="ownerHeaders"
            :items="property.owners || []"
            :loading="loading"
            class="elevation-0"
          >
            <template v-slot:item.actions="{ item }">
              <v-icon small class="mr-2" @click="editOwner(item)">
                mdi-pencil
              </v-icon>
              <v-icon small @click="setAsMainContact(item)" v-if="!isMainContact(item)">
                mdi-star-outline
              </v-icon>
              <v-icon small color="amber" v-else>
                mdi-star
              </v-icon>
            </template>
            
            <template v-slot:no-data>
              <p>Inga ägare för denna fastighet.</p>
            </template>
          </v-data-table>
        </v-card-text>
      </v-card>
    </div>
    
    <v-alert
      v-if="error"
      type="error"
      class="mb-4"
    >
      {{ error }}
    </v-alert>
    
    <!-- Property Edit Dialog -->
    <v-dialog v-model="propertyDialog" max-width="600px">
      <v-card>
        <v-card-title class="text-h5">Redigera fastighet</v-card-title>
        <v-card-text>
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
      <v-card>
        <v-card-title class="text-h5">{{ ownerFormTitle }}</v-card-title>
        <v-card-text>
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
        { title: 'Namn', key: 'name' },
        { title: 'E-post', key: 'email' },
        { title: 'Telefon', key: 'phone' },
        { title: 'Åtgärder', key: 'actions', sortable: false }
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