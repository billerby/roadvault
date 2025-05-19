<!-- src/frontend/src/views/AssociationView.vue -->
<template>
  <v-container>
    <div class="page-header">
      <h1 class="text-h3">Föreningsuppgifter</h1>
      <p class="subtitle-1">Administrera vägföreningens inställningar</p>
    </div>
    
    <v-card class="rv-card rv-mb-md">
      <v-card-title class="rv-card-header">
        <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-domain</v-icon>
        <span class="text-h5">Grunduppgifter</span>
        <v-spacer></v-spacer>
        <v-btn
          v-if="!editing"
          class="rv-btn-icon rv-btn--primary"
          @click="startEditing"
        >
          <v-icon class="action-icon">mdi-pencil</v-icon>
        </v-btn>
      </v-card-title>
      <v-divider></v-divider>
      
      <v-card-text class="rv-p-md">
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
              class="rv-btn rv-btn--primary"
            >
              <v-icon left>mdi-content-save</v-icon>
              Spara
            </v-btn>
          </v-row>
        </v-form>
        
        <div v-else>
          <v-row>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">Föreningens namn</div>
              <div class="rv-detail-value">{{ association.name || 'Ej angivet' }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">Organisationsnummer</div>
              <div class="rv-detail-value">{{ association.organizationNumber || 'Ej angivet' }}</div>
            </v-col>
          </v-row>
          
          <v-row>
            <v-col cols="12" md="4">
              <div class="rv-detail-label">E-post</div>
              <div class="rv-detail-value">
                <a v-if="association.email" :href="`mailto:${association.email}`">{{ association.email }}</a>
                <span v-else>Ej angivet</span>
              </div>
            </v-col>
            <v-col cols="12" md="4">
              <div class="rv-detail-label">Telefon</div>
              <div class="rv-detail-value">
                <a v-if="association.phone" :href="`tel:${association.phone}`">{{ association.phone }}</a>
                <span v-else>Ej angivet</span>
              </div>
            </v-col>
            <v-col cols="12" md="4">
              <div class="rv-detail-label">Hemsida</div>
              <div class="rv-detail-value">
                <a v-if="association.website" :href="ensureUrlProtocol(association.website)" target="_blank">{{ association.website }}</a>
                <span v-else>Ej angivet</span>
              </div>
            </v-col>
          </v-row>
          
          <v-row>
            <v-col cols="12">
              <div class="rv-detail-label">Adress</div>
              <div class="rv-detail-value">
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
    
    <v-card class="rv-card rv-mb-md">
      <v-card-title class="rv-card-header">
        <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-cash-register</v-icon>
        <span class="text-h5">Faktureringsinformation</span>
        <v-spacer></v-spacer>
        <v-btn
          v-if="!editingBilling"
          class="rv-btn-icon rv-btn--primary"
          @click="startEditingBilling"
        >
          <v-icon class="action-icon">mdi-pencil</v-icon>
        </v-btn>
      </v-card-title>
      <v-divider></v-divider>
      
      <v-card-text class="rv-p-md">
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
              class="rv-btn rv-btn--primary"
            >
              <v-icon left>mdi-content-save</v-icon>
              Spara
            </v-btn>
          </v-row>
        </v-form>
        
        <div v-else>
          <v-row>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">Bankgironummer</div>
              <div class="rv-detail-value">{{ association.bankgiroNumber || 'Ej angivet' }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">Plusgironummer</div>
              <div class="rv-detail-value">{{ association.plusgiroNumber || 'Ej angivet' }}</div>
            </v-col>
          </v-row>
          
          <v-row>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">Antal dagar till påminnelse</div>
              <div class="rv-detail-value">{{ association.reminderDays || 'Ej angivet' }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="rv-detail-label">Påminnelseavgift</div>
              <div class="rv-detail-value">{{ formatCurrency(association.reminderFee) }}</div>
            </v-col>
          </v-row>
          
          <v-row v-if="association.invoiceText">
            <v-col cols="12">
              <div class="rv-detail-label">Standardtext på fakturor</div>
              <div class="rv-detail-value invoice-text">
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
