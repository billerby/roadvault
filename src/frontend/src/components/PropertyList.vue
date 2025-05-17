<!-- components/PropertyList.vue -->
<template>
  <div>
    <v-data-table
      :headers="headers"
      :items="properties"
      :loading="loading"
      class="elevation-1"
      items-per-page="50"
    >
      <template v-slot:top>
        <v-toolbar flat>
          <v-toolbar-title>Fastigheter</v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn
            v-if="hasRole('ADMIN')"
            color="primary"
            dark
            class="mb-2"
            @click="openDialog()"
          >
            <v-icon left>mdi-plus</v-icon>
            Lägg till fastighet
          </v-btn>
        </v-toolbar>
      </template>
      
      <!-- Owner column with clickable name -->
      <template v-slot:item.mainContact.name="{ item }">
        <a @click.prevent="editOwner(item)" href="#" class="text-decoration-none">
          {{ item.mainContact ? item.mainContact.name : 'Ingen ägare' }}
        </a>
      </template>
      
      <template v-slot:item.actions="{ item }">
        <v-icon small class="mr-2" @click="viewProperty(item)">
          mdi-eye
        </v-icon>
        <v-icon v-if='hasRole("ADMIN")' small class="mr-2" @click="openDialog(item)">
          mdi-pencil
        </v-icon>
        <v-icon v-if='hasRole("ADMIN")' small @click="confirmDelete(item)">
          mdi-delete
        </v-icon>
      </template>
      
      <!-- Om det inte finns några fastigheter -->
      <template v-slot:no-data>
        <div class="text-center py-3">
          <p v-if="loading">Laddar fastigheter...</p>
          <p v-else>Hittade inga fastigheter</p>
        </div>
      </template>
      
      <!-- Error message -->
      <template v-if="error" v-slot:footer>
        <v-alert type="error" dense>
          {{ error }}
        </v-alert>
      </template>
    </v-data-table>
    
    <!-- Property Form Dialog -->
    <v-dialog v-model="dialog" max-width="600px">
      <v-card>
        <v-card-title>
          <span class="text-h5">{{ formTitle }}</span>
        </v-card-title>
        
        <v-card-text>
          <PropertyForm 
            :property="editedItem" 
            @save="saveProperty" 
            @cancel="closeDialog"
          />
        </v-card-text>
      </v-card>
    </v-dialog>
    
    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="400px">
      <v-card>
        <v-card-title class="text-h5">Konfirmera borttagning</v-card-title>
        <v-card-text>
          Är du säker på att du vill ta bort {{ propertyToDelete ? propertyToDelete.propertyDesignation : '' }}?
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue darken-1" text @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="red darken-1" text @click="deleteProperty">Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
    <!-- Owner Form Dialog -->
    <v-dialog v-model="ownerDialog" max-width="600px">
      <v-card>
        <v-card-title>
          <span class="text-h5">{{ ownerFormTitle }}</span>
        </v-card-title>
        
        <v-card-text>
          <OwnerForm 
            :owner="editedOwner" 
            @save="saveOwner" 
            @cancel="ownerDialog = false"
          />
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import propertyService from '../services/property.service';
import authService from '../services/auth.service';
import ownerService from '../services/owner.service';
import PropertyForm from './PropertyForm.vue';
import OwnerForm from './OwnerForm.vue';

export default {
  name: 'PropertyList',
  components: {
    PropertyForm,
    OwnerForm
  },
  
  data() {
    return {
      properties: [],
      loading: false,
      error: null,
      dialog: false,
      deleteDialog: false,
      ownerDialog: false,
      editedItem: {
        id: null,
        propertyDesignation: '',
        shareRatio: 0,
        address: ''
      },
      defaultItem: {
        id: null,
        propertyDesignation: '',
        shareRatio: 0,
        address: ''
      },
      editedOwner: {
        id: null,
        name: '',
        email: '',
        phone: '',
        address: '',
        postalCode: '',
        city: ''
      },
      defaultOwner: {
        id: null,
        name: '',
        email: '',
        phone: '',
        address: '',
        postalCode: '',
        city: ''
      },
      selectedProperty: null,
      propertyToDelete: null,
      headers: [
        { title: 'Fastighetsbeteckning', key: 'propertyDesignation' },
        { title: 'Delaktighetstal', key: 'shareRatio' },
        { title: 'Adress', key: 'address' },
        { title: 'Huvudkontakt', key: 'mainContact.name' },
        { title: 'Åtgärder', key: 'actions', sortable: false }
      ]
    };
  },
  
  computed: {
    formTitle() {
      return this.editedItem.id ? 'Redigera fastighet' : 'Ny fastighet';
    },
    
    ownerFormTitle() {
      return this.editedOwner.id ? 'Redigera ägare' : 'Lägg till ägare';
    }
  },
  
  created() {
    this.fetchProperties();
  },
  
  methods: {
    hasRole(role) {
      return authService.hasRole(role);
    },
    async fetchProperties() {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await propertyService.getAllProperties();
        this.properties = response.data;
      } catch (err) {
        this.error = 'Misslyckades med att ladda fastigheter: ' + (err.response?.data?.message || err.message);
        console.error('Error fetching properties:', err);
      } finally {
        this.loading = false;
      }
    },
    
    viewProperty(item) {
      this.$router.push(`/properties/${item.id}`);
    },
    
    editOwner(item) {
      this.selectedProperty = item;
      if (item.mainContact) {
        this.editedOwner = Object.assign({}, item.mainContact);
      } else {
        this.editedOwner = Object.assign({}, this.defaultOwner);
      }
      this.ownerDialog = true;
    },
    
    async saveOwner(owner) {
      if (!this.selectedProperty) return;
      
      this.loading = true;
      this.error = null;
      
      try {
        let updatedOwner;
        
        if (owner.id) {
          // Update existing owner
          const response = await ownerService.updateOwner(owner.id, owner);
          updatedOwner = response.data;
        } else {
          // Create new owner
          const response = await ownerService.createOwner(owner);
          updatedOwner = response.data;
          
          // Add the owner to the property if it's new
          if (!this.selectedProperty.owners) {
            this.selectedProperty.owners = [];
          }
          this.selectedProperty.owners.push(updatedOwner);
        }
        
        // Update property with mainContact if needed
        if (!this.selectedProperty.mainContact) {
          await propertyService.updateProperty(this.selectedProperty.id, {
            ...this.selectedProperty,
            mainContact: { id: updatedOwner.id }
          });
        }
        
        // Close dialog and refresh data
        this.ownerDialog = false;
        this.fetchProperties();
      } catch (err) {
        this.error = 'Misslyckades med att spara ägare: ' + (err.response?.data?.message || err.message);
        console.error('Error saving owner:', err);
      } finally {
        this.loading = false;
      }
    },
    
    openDialog(item) {
      if (item) {
        // Redigera befintlig property
        this.editedItem = Object.assign({}, item);
      } else {
        // Skapa ny property
        this.editedItem = Object.assign({}, this.defaultItem);
      }
      this.dialog = true;
    },
    
    closeDialog() {
      this.dialog = false;
      // Vänta så dialog-animationen slutförs innan reset
      setTimeout(() => {
        this.editedItem = Object.assign({}, this.defaultItem);
      }, 300);
    },
    
    async saveProperty(property) {
      this.loading = true;
      this.error = null;
      
      try {
        if (property.id) {
          // Uppdatera existerande property
          await propertyService.updateProperty(property.id, property);
        } else {
          // Skapa ny property
          await propertyService.createProperty(property);
        }
        
        // Stäng dialogen och uppdatera listan
        this.closeDialog();
        this.fetchProperties();
      } catch (err) {
        this.error = 'Misslyckades med att spara fastighet: ' + (err.response?.data?.message || err.message);
        console.error('Error saving property:', err);
      } finally {
        this.loading = false;
      }
    },
    
    confirmDelete(item) {
      this.propertyToDelete = item;
      this.deleteDialog = true;
    },
    
    async deleteProperty() {
      if (!this.propertyToDelete) return;
      
      this.loading = true;
      this.error = null;
      
      try {
        await propertyService.deleteProperty(this.propertyToDelete.id);
        this.properties = this.properties.filter(p => p.id !== this.propertyToDelete.id);
      } catch (err) {
        this.error = 'Misslyckades med att radera fastighet: ' + (err.response?.data?.message || err.message);
        console.error('Error deleting property:', err);
      } finally {
        this.loading = false;
        this.deleteDialog = false;
        this.propertyToDelete = null;
      }
    }
  }
};
</script>