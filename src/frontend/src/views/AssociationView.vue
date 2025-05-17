<!-- src/frontend/src/views/AssociationView.vue -->
<template>
  <v-container>
    <div class="page-header">
      <h1 class="text-h3">Föreningsuppgifter</h1>
      <p class="subtitle-1">Administrera vägföreningens inställningar</p>
    </div>
    
    <v-card class="mb-golden elevation-2">
      <v-card-title class="d-flex align-center">
        <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-domain</v-icon>
        <span class="text-h5">Grunduppgifter</span>
        <v-spacer></v-spacer>
        <v-btn
          v-if="!editing"
          class="action-btn edit-btn"
          @click="startEditing"
        >
          <v-icon class="action-icon">mdi-pencil</v-icon>
        </v-btn>
      </v-card-title>
      <v-divider></v-divider>
      
      <v-card-text class="pa-golden">
        <div v-if="loading" class="text-center py-4">
          <v-progress-circular indeterminate color="var(--color-primary-light)" size="64"></v-progress-circular>
        </div>
        
        <v-form v-else-if="editing" ref="form" v-model="valid" lazy-validation>
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="editedAssociation.name"
                label="Föreningens namn"
                required
                :rules="[v => !!v || 'Namn är obligatoriskt']"
                hint="T.ex. 'Billerby Vägförening'"
                outlined
                dense
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="editedAssociation.organizationNumber"
                label="Organisationsnummer"
                hint="Format: XXXXXX-XXXX"
                outlined
                dense
              ></v-text-field>
            </v-col>
          </v-row>
          
          <v-row>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="editedAssociation.email"
                label="E-post"
                type="email"
                hint="Föreningens officiella e-postadress"
                outlined
                dense
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="editedAssociation.phone"
                label="Telefon"
                hint="Föreningens kontaktnummer"
                outlined
                dense
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="editedAssociation.website"
                label="Hemsida"
                hint="T.ex. 'www.billerby.se'"
                outlined
                dense
              ></v-text-field>
            </v-col>
          </v-row>
          
          <v-row>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="editedAssociation.address"
                label="Adress"
                hint="Gatuadress"
                outlined
                dense
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="editedAssociation.postalCode"
                label="Postnummer"
                hint="T.ex. '12345'"
                outlined
                dense
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="editedAssociation.city"
                label="Ort"
                hint="T.ex. 'Billerby'"
                outlined
                dense
              ></v-text-field>
            </v-col>
          </v-row>
          
          <v-divider class="my-4"></v-divider>
          
          <v-row>
            <v-spacer></v-spacer>
            <v-btn
              color="grey lighten-1"
              text
              @click="cancelEditing"
              class="mr-4"
            >
              Avbryt
            </v-btn>
            <v-btn
              color="var(--color-primary-light)"
              :disabled="!valid"
              @click="saveAssociation"
              class="primary-action-btn"
            >
              <v-icon left>mdi-content-save</v-icon>
              Spara
            </v-btn>
          </v-row>
        </v-form>
        
        <div v-else>
          <v-row>
            <v-col cols="12" md="6">
              <div class="detail-label">Föreningens namn</div>
              <div class="detail-value">{{ association.name || 'Ej angivet' }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="detail-label">Organisationsnummer</div>
              <div class="detail-value">{{ association.organizationNumber || 'Ej angivet' }}</div>
            </v-col>
          </v-row>
          
          <v-row>
            <v-col cols="12" md="4">
              <div class="detail-label">E-post</div>
              <div class="detail-value">
                <a v-if="association.email" :href="`mailto:${association.email}`">{{ association.email }}</a>
                <span v-else>Ej angivet</span>
              </div>
            </v-col>
            <v-col cols="12" md="4">
              <div class="detail-label">Telefon</div>
              <div class="detail-value">
                <a v-if="association.phone" :href="`tel:${association.phone}`">{{ association.phone }}</a>
                <span v-else>Ej angivet</span>
              </div>
            </v-col>
            <v-col cols="12" md="4">
              <div class="detail-label">Hemsida</div>
              <div class="detail-value">
                <a v-if="association.website" :href="ensureUrlProtocol(association.website)" target="_blank">{{ association.website }}</a>
                <span v-else>Ej angivet</span>
              </div>
            </v-col>
          </v-row>
          
          <v-row>
            <v-col cols="12">
              <div class="detail-label">Adress</div>
              <div class="detail-value">
                <template v-if="hasAddress">
                  {{ association.address || '' }}
                  <template v-if="association.postalCode || association.city">
                    <br>{{ association.postalCode || '' }} {{ association.city || '' }}
                  </template>
                </template>
                <span v-else>Ej angivet</span>
              </div>
            </v-col>
          </v-row>
        </div>
      </v-card-text>
    </v-card>
    
    <v-card class="mb-golden elevation-2">
      <v-card-title class="d-flex align-center">
        <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-cash-register</v-icon>
        <span class="text-h5">Faktureringsinformation</span>
        <v-spacer></v-spacer>
        <v-btn
          v-if="!editingBilling"
          class="action-btn edit-btn"
          @click="startEditingBilling"
        >
          <v-icon class="action-icon">mdi-pencil</v-icon>
        </v-btn>
      </v-card-title>
      <v-divider></v-divider>
      
      <v-card-text class="pa-golden">
        <div v-if="loading" class="text-center py-4">
          <v-progress-circular indeterminate color="var(--color-primary-light)" size="64"></v-progress-circular>
        </div>
        
        <v-form v-else-if="editingBilling" ref="billingForm" v-model="validBilling" lazy-validation>
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="editedAssociation.bankgiroNumber"
                label="Bankgironummer"
                hint="T.ex. '123-4567'"
                outlined
                dense
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="editedAssociation.plusgiroNumber"
                label="Plusgironummer"
                hint="T.ex. '12 34 56-7'"
                outlined
                dense
              ></v-text-field>
            </v-col>
          </v-row>
          
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="editedAssociation.reminderDays"
                label="Antal dagar till påminnelse"
                type="number"
                hint="Antalet dagar efter förfallodatum innan påminnelse skickas"
                outlined
                dense
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="editedAssociation.reminderFee"
                label="Påminnelseavgift"
                type="number"
                prefix="SEK"
                hint="Avgift som tillkommer vid påminnelse"
                outlined
                dense
              ></v-text-field>
            </v-col>
          </v-row>
          
          <v-row>
            <v-col cols="12">
              <v-textarea
                v-model="editedAssociation.invoiceText"
                label="Standardtext på fakturor"
                hint="Text som visas på alla fakturor"
                outlined
                rows="4"
              ></v-textarea>
            </v-col>
          </v-row>
          
          <v-divider class="my-4"></v-divider>
          
          <v-row>
            <v-spacer></v-spacer>
            <v-btn
              color="grey lighten-1"
              text
              @click="cancelEditingBilling"
              class="mr-4"
            >
              Avbryt
            </v-btn>
            <v-btn
              color="var(--color-primary-light)"
              :disabled="!validBilling"
              @click="saveBillingInfo"
              class="primary-action-btn"
            >
              <v-icon left>mdi-content-save</v-icon>
              Spara
            </v-btn>
          </v-row>
        </v-form>
        
        <div v-else>
          <v-row>
            <v-col cols="12" md="6">
              <div class="detail-label">Bankgironummer</div>
              <div class="detail-value">{{ association.bankgiroNumber || 'Ej angivet' }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="detail-label">Plusgironummer</div>
              <div class="detail-value">{{ association.plusgiroNumber || 'Ej angivet' }}</div>
            </v-col>
          </v-row>
          
          <v-row>
            <v-col cols="12" md="6">
              <div class="detail-label">Antal dagar till påminnelse</div>
              <div class="detail-value">{{ association.reminderDays || 'Ej angivet' }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="detail-label">Påminnelseavgift</div>
              <div class="detail-value">{{ formatCurrency(association.reminderFee) }}</div>
            </v-col>
          </v-row>
          
          <v-row v-if="association.invoiceText">
            <v-col cols="12">
              <div class="detail-label">Standardtext på fakturor</div>
              <div class="detail-value invoice-text">
                {{ association.invoiceText }}
              </div>
            </v-col>
          </v-row>
          
          <v-row v-else>
            <v-col cols="12">
              <div class="detail-text-empty text-center py-4">
                <v-icon size="48" color="grey lighten-1">mdi-text-box-outline</v-icon>
                <p class="mt-2">Ingen standardtext för fakturor har angetts ännu.</p>
              </div>
            </v-col>
          </v-row>
        </div>
      </v-card-text>
    </v-card>
    
    <!-- Snackbar för feedback -->
    <v-snackbar
      v-model="snackbar"
      :color="snackbarColor"
      timeout="5000"
      bottom
      right
    >
      {{ snackbarMessage }}
      <template v-slot:action="{ attrs }">
        <v-btn
          text
          v-bind="attrs"
          @click="snackbar = false"
        >
          Stäng
        </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
import associationService from '../services/association.service';

export default {
  name: 'AssociationView',
  
  data() {
    return {
      loading: false,
      association: {
        id: null,
        name: '',
        organizationNumber: '',
        bankgiroNumber: '',
        plusgiroNumber: '',
        email: '',
        phone: '',
        address: '',
        postalCode: '',
        city: '',
        website: '',
        invoiceText: '',
        reminderDays: 14,
        reminderFee: 60
      },
      editing: false,
      editingBilling: false,
      editedAssociation: {},
      valid: true,
      validBilling: true,
      
      // Snackbar
      snackbar: false,
      snackbarMessage: '',
      snackbarColor: 'success'
    };
  },
  
  computed: {
    hasAddress() {
      return this.association.address || this.association.postalCode || this.association.city;
    }
  },
  
  created() {
    this.fetchAssociation();
  },
  
  methods: {
    async fetchAssociation() {
      this.loading = true;
      
      try {
        const response = await associationService.getAssociation();
        this.association = response.data;
      } catch (error) {
        console.error('Fel vid hämtning av föreningsuppgifter:', error);
        this.showSnackbar('Kunde inte hämta föreningsuppgifter', 'error');
      } finally {
        this.loading = false;
      }
    },
    
    startEditing() {
      this.editedAssociation = { ...this.association };
      this.editing = true;
    },
    
    cancelEditing() {
      this.editing = false;
      if (this.$refs.form) {
        this.$refs.form.reset();
      }
    },
    
    startEditingBilling() {
      this.editedAssociation = { ...this.association };
      this.editingBilling = true;
    },
    
    cancelEditingBilling() {
      this.editingBilling = false;
      if (this.$refs.billingForm) {
        this.$refs.billingForm.reset();
      }
    },
    
    async saveAssociation() {
      if (this.$refs.form && this.$refs.form.validate()) {
        this.loading = true;
        
        try {
          let response;
          
          if (this.association.id) {
            response = await associationService.updateAssociation(this.editedAssociation);
          } else {
            response = await associationService.createAssociation(this.editedAssociation);
          }
          
          this.association = response.data;
          this.editing = false;
          this.showSnackbar('Föreningsuppgifter har sparats', 'success');
        } catch (error) {
          console.error('Fel vid sparande av föreningsuppgifter:', error);
          this.showSnackbar('Kunde inte spara föreningsuppgifter', 'error');
        } finally {
          this.loading = false;
        }
      }
    },
    
    async saveBillingInfo() {
      if (this.$refs.billingForm && this.$refs.billingForm.validate()) {
        this.loading = true;
        
        try {
          let response;
          
          if (this.association.id) {
            response = await associationService.updateAssociation(this.editedAssociation);
          } else {
            response = await associationService.createAssociation(this.editedAssociation);
          }
          
          this.association = response.data;
          this.editingBilling = false;
          this.showSnackbar('Faktureringsinformation har sparats', 'success');
        } catch (error) {
          console.error('Fel vid sparande av faktureringsinformation:', error);
          this.showSnackbar('Kunde inte spara faktureringsinformation', 'error');
        } finally {
          this.loading = false;
        }
      }
    },
    
    ensureUrlProtocol(url) {
      if (!url) return '';
      
      // Check if the URL has a protocol
      if (!/^https?:\/\//i.test(url)) {
        // If not, add https://
        return 'https://' + url;
      }
      
      return url;
    },
    
    formatCurrency(value) {
      if (!value) return '0 kr';
      
      return new Intl.NumberFormat('sv-SE', {
        style: 'currency',
        currency: 'SEK',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }).format(value);
    },
    
    showSnackbar(message, color = 'success') {
      this.snackbarMessage = message;
      this.snackbarColor = color;
      this.snackbar = true;
    }
  }
};
</script>

<style scoped>
.page-header {
  margin-bottom: var(--spacing-md);
}

/* Åtgärdsknappar i header */
.action-btn {
  width: 42px;
  height: 42px;
  min-width: 0;
  border-radius: 21px !important;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 3px 5px rgba(0, 0, 0, 0.1) !important;
  background-color: var(--color-primary-light) !important;
  color: white !important;
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0) 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.action-btn:hover::before {
  opacity: 1;
}

.action-btn .action-icon {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.action-btn:hover .action-icon {
  transform: scale(1.15);
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15) !important;
}

.action-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
}

/* Primära åtgärdsknappar */
.primary-action-btn {
  height: 42px;
  border-radius: var(--button-border-radius) !important;
  padding: 0 24px !important;
  box-shadow: 0 3px 5px rgba(0, 0, 0, 0.1) !important;
  font-weight: 500 !important;
  text-transform: none !important;
  letter-spacing: 0 !important;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease !important;
}

.primary-action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0) 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.primary-action-btn:hover::before {
  opacity: 1;
}

.primary-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15) !important;
}

.primary-action-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
}

/* Detaljstilar */
.detail-label {
  font-size: 0.9rem;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.6);
  margin-bottom: var(--spacing-xs);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-value {
  font-size: 1.1rem;
  font-weight: 400;
  margin-bottom: var(--spacing-md);
}

.detail-value a {
  color: var(--color-secondary-dark);
  text-decoration: none;
}

.detail-value a:hover {
  text-decoration: underline;
}

.invoice-text {
  white-space: pre-line;
  background-color: rgba(0, 0, 0, 0.03);
  padding: var(--spacing-sm);
  border-radius: var(--card-border-radius);
  font-family: 'Roboto Mono', monospace;
  font-size: 0.9rem;
}

.detail-text-empty {
  color: rgba(0, 0, 0, 0.4);
}
</style>
