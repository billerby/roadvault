<template>
  <v-container>
    <div class="page-header">
      <h1 class="text-h3">Utdebiteringar</h1>
      <p class="subtitle-1">Här kan du skapa och hantera utdebiteringar för föreningen</p>
    </div>
<!-- Lista över befintliga utdebiteringar -->
<v-card class="mb-golden elevation-2">
  <v-card-title class="d-flex align-center">
    <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-cash-multiple</v-icon>
    <span class="text-h5">Befintliga utdebiteringar</span>
    <v-spacer></v-spacer>
    <v-btn
      class="action-btn add-btn"
      @click="openNewBillingDialog"
    >
      <v-icon class="action-icon">mdi-plus</v-icon>
    </v-btn>
  </v-card-title>
  <v-divider></v-divider>
  
  <v-card-text class="pa-golden">
    <v-data-table
      :headers="billingHeaders"
      :items="billings"
      :loading="loading"
      :items-per-page="10"
      class="elevation-0 custom-table"
    >
      <template v-slot:item.year="{ item }">
        <strong>{{ item.year }}</strong>
      </template>
      
      <template v-slot:item.type="{ item }">
        <v-chip 
          small 
          :color="getBillingTypeColor(item.type)" 
          text-color="white"
        >
          {{ getBillingTypeLabel(item.type) }}
        </v-chip>
      </template>
      
      <template v-slot:item.totalAmount="{ item }">
        {{ formatCurrency(item.totalAmount) }}
      </template>
      
      <template v-slot:item.invoiceCount="{ item }">
        <v-chip small :color="item.invoiceCount ? 'success' : 'grey'">
          {{ item.invoiceCount || 0 }}
        </v-chip>
      </template>
      
      <template v-slot:item.dueDate="{ item }">
        {{ formatDate(item.dueDate) }}
      </template>
      
      <template v-slot:item.actions="{ item }">
        <div class="actions-container">
          <v-btn 
            class="table-action-btn view-action mr-1"
            @click="viewBilling(item)"
            title="Visa detaljer"
          >
            <v-icon small>mdi-eye</v-icon>
          </v-btn>
          
          <v-btn 
            class="table-action-btn invoice-action mr-1"
            @click="generateInvoices(item)"
            title="Generera fakturor"
            v-if="item.invoiceCount === 0"
          >
            <v-icon small>mdi-file-document-multiple</v-icon>
          </v-btn>
          
          <v-btn 
            class="table-action-btn invoice-action mr-1"
            @click="viewInvoices(item)"
            title="Visa fakturor"
            v-else
          >
            <v-icon small>mdi-file-document-outline</v-icon>
          </v-btn>
          
          <v-btn 
            class="table-action-btn edit-action"
            @click="editBilling(item)"
            title="Redigera"
            v-if="item.invoiceCount === 0"
          >
            <v-icon small>mdi-pencil</v-icon>
          </v-btn>
        </div>
      </template>
      
      <template v-slot:no-data>
        <div class="empty-state text-center pa-golden">
          <v-icon size="64" color="grey lighten-1">mdi-cash-remove</v-icon>
          <p class="mt-4">Inga utdebiteringar har skapats än.</p>
          <v-btn 
            color="var(--color-primary-light)"
            class="mt-4 primary-action-btn"
            @click="openNewBillingDialog"
          >
            <v-icon left>mdi-plus</v-icon>
            Skapa ny utdebitering
          </v-btn>
        </div>
      </template>
    </v-data-table>
  </v-card-text>
</v-card>

<!-- Förklaring om utdebiteringar och delaktighetstal -->
<v-card class="mb-golden elevation-1">
  <v-card-title class="d-flex align-center">
    <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-information-outline</v-icon>
    <span class="text-h6">Om utdebiteringar</span>
  </v-card-title>
  <v-divider></v-divider>
  <v-card-text class="pa-golden">
    <p>
      Utdebiteringar skapas baserat på delaktighetstal. 
      Systemet beräknar varje fastighets andel av den totala kostnaden enligt formeln:
    </p>
    <div class="formula-container text-center my-4">
      <p class="formula">
        <span>Fakturabelopp</span> = 
        <span class="fraction">
          <span class="numerator">Delaktighetstal för fastigheten</span>
          <span class="divisor"></span>
          <span class="denominator">Summa av alla delaktighetstal</span>
        </span>
        × <span>Totalt belopp att utdebitera</span>
      </p>
    </div>
    <p>
      När du skapar en utdebitering blir den först sparad utan fakturor. 
      Du kan sedan generera fakturor för alla fastigheter baserat på deras delaktighetstal.
    </p>
  </v-card-text>
</v-card>

<!-- Dialog för att skapa/redigera utdebitering -->
<v-dialog v-model="billingDialog" max-width="700px">
  <v-card class="dialog-card">
    <v-card-title class="text-h5 dialog-title">
      <v-icon color="var(--color-primary-dark)" class="mr-2">
        {{ editedBilling.id ? 'mdi-pencil' : 'mdi-cash-plus' }}
      </v-icon>
      {{ editedBilling.id ? 'Redigera utdebitering' : 'Skapa ny utdebitering' }}
    </v-card-title>
    <v-divider></v-divider>
    
    <v-card-text class="pa-golden">
      <v-form ref="form" v-model="valid" lazy-validation>
        <v-row>
          <v-col cols="12" sm="6">
            <v-text-field
              v-model="editedBilling.year"
              label="År"
              type="number"
              required
              :rules="[v => !!v || 'År är obligatoriskt']"
              hint="Räkenskapsår för utdebiteringen"
              outlined
              dense
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="6">
          <v-select
            v-model="editedBilling.type"
            label="Typ"
            :items="billingTypes"
            item-title="label"
            item-value="value"
            required
            :rules="[v => !!v || 'Typ är obligatoriskt']"
            hint="Välj typ av utdebitering"
            outlined
            dense
          ></v-select>
          </v-col>
        </v-row>
        
        <v-row>
          <v-col cols="12" sm="6">
            <v-text-field
              v-model="editedBilling.totalAmount"
              label="Totalt belopp"
              type="number"
              required
              :rules="[v => !!v || 'Belopp är obligatoriskt']"
              prefix="SEK"
              hint="Totalt belopp att utdebitera"
              outlined
              dense
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="6">
            <v-text-field
              v-model="editedBilling.extraCharge"
              label="Extra debitering"
              type="number"
              prefix="SEK"
              hint="Eventuell extra debitering (t.ex. förseningsavgift)"
              outlined
              dense
            ></v-text-field>
          </v-col>
        </v-row>
        
        <v-row>
          <v-col cols="12">
            <v-text-field
              v-model="editedBilling.description"
              label="Beskrivning"
              required
              :rules="[v => !!v || 'Beskrivning är obligatoriskt']"
              hint="T.ex. 'Utdebitering av ordinarie årsavgift 2025'"
              outlined
              dense
            ></v-text-field>
          </v-col>
        </v-row>
        
        <v-row>
          <v-col cols="12" sm="6">
            <v-menu
              v-model="issueDateMenu"
              :close-on-content-click="false"
              transition="scale-transition"
              offset-y
              min-width="290px"
            >
              <template v-slot:activator="{ on, attrs }">
                <v-text-field
                  v-model="formattedIssueDate"
                  label="Utskriftsdatum"
                  readonly
                  outlined
                  dense
                  v-bind="attrs"
                  v-on="on"
                  required
                  :rules="[v => !!v || 'Utskriftsdatum är obligatoriskt']"
                ></v-text-field>
              </template>
              <v-date-picker
                v-model="editedBilling.issueDate"
                @input="issueDateMenu = false"
                locale="sv-SE"
              ></v-date-picker>
            </v-menu>
          </v-col>
          <v-col cols="12" sm="6">
            <v-menu
              v-model="dueDateMenu"
              :close-on-content-click="false"
              transition="scale-transition"
              offset-y
              min-width="290px"
            >
              <template v-slot:activator="{ on, attrs }">
                <v-text-field
                  v-model="formattedDueDate"
                  label="Förfallodatum"
                  readonly
                  outlined
                  dense
                  v-bind="attrs"
                  v-on="on"
                  required
                  :rules="[v => !!v || 'Förfallodatum är obligatoriskt']"
                ></v-text-field>
              </template>
              <v-date-picker
                v-model="editedBilling.dueDate"
                @input="dueDateMenu = false"
                locale="sv-SE"
              ></v-date-picker>
            </v-menu>
          </v-col>
        </v-row>
        
        <v-row class="mt-4">
          <v-col cols="12">
            <v-checkbox
              v-model="generateInvoicesDirectly"
              label="Generera fakturor direkt efter sparande"
            ></v-checkbox>
          </v-col>
        </v-row>
        
        <v-divider class="my-4"></v-divider>
        
        <v-card class="mb-4 grey lighten-5">
          <v-card-text>
            <div class="d-flex align-center mb-2">
              <v-icon color="primary" small class="mr-2">mdi-information</v-icon>
              <span class="subtitle-2">Sammanfattning</span>
            </div>
            <p class="body-2 mb-1">Totalbelopp att utdebitera: <strong>{{ formatCurrency(editedBilling.totalAmount) }}</strong></p>
            <p class="body-2">Antal fastigheter för fakturering: <strong>{{ propertyCount }}</strong></p>
          </v-card-text>
        </v-card>
        
        <v-row>
          <v-spacer></v-spacer>
          <v-btn
            color="grey lighten-1"
            text
            @click="closeBillingDialog"
            class="mr-4"
          >
            Avbryt
          </v-btn>
          <v-btn
            color="var(--color-primary-light)"
            :disabled="!valid"
            @click="saveBilling"
            class="primary-action-btn"
          >
            <v-icon left>mdi-content-save</v-icon>
            Spara
          </v-btn>
        </v-row>
      </v-form>
    </v-card-text>
  </v-card>
</v-dialog>

<!-- Bekräftelse för att generera fakturor -->
<v-dialog v-model="confirmDialog" max-width="500px">
  <v-card>
    <v-card-title class="text-h5">Bekräfta åtgärd</v-card-title>
    <v-divider></v-divider>
    <v-card-text class="pa-golden">
      <p>Är du säker på att du vill generera fakturor för denna utdebitering?</p>
      <p class="mt-2">Detta kommer att:</p>
      <ul>
        <li>Skapa {{ propertyCount }} fakturor - en för varje fastighet</li>
        <li>Beräkna belopp baserat på delaktighetstal</li>
        <li>Generera fakturanummer och OCR-nummer</li>
      </ul>
      <p class="font-weight-medium mt-4">Denna åtgärd kan inte ångras!</p>
    </v-card-text>
    <v-divider></v-divider>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn 
        color="grey lighten-1" 
        text 
        @click="confirmDialog = false"
      >
        Avbryt
      </v-btn>
      <v-btn 
        color="var(--color-primary-light)"
        @click="confirmGenerateInvoices"
        class="primary-action-btn"
      >
        <v-icon left>mdi-check</v-icon>
        Generera fakturor
      </v-btn>
    </v-card-actions>
  </v-card>
</v-dialog>

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
import billingService from '../services/billing.service';
import propertyService from '../services/property.service';
import { format, parseISO } from 'date-fns';
import { sv } from 'date-fns/locale';

export default {
  name: 'BillingView',
  
  data() {
    return {
      billings: [],
      loading: false,
      billingDialog: false,
      confirmDialog: false,
      selectedBilling: null,
      
      // Form
      valid: true,
      editedBilling: {
        id: null,
        year: new Date().getFullYear(),
        description: '',
        totalAmount: null,
        extraCharge: 0,
        issueDate: format(new Date(), 'yyyy-MM-dd'),
        dueDate: format(new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd'),
        type: 'ANNUAL_FEE',
        invoiceCount: 0
      },
      defaultBilling: {
        id: null,
        year: new Date().getFullYear(),
        description: '',
        totalAmount: null,
        extraCharge: 0,
        issueDate: format(new Date(), 'yyyy-MM-dd'),
        dueDate: format(new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd'),
        type: 'ANNUAL_FEE',
        invoiceCount: 0
      },
      
      // Date pickers
      issueDateMenu: false,
      dueDateMenu: false,
      
      // Billing type options
      billingTypes: [
        { value: 'ANNUAL_FEE', label: 'Årsavgift' },
        { value: 'EXTRA_CHARGE', label: 'Extra uttaxering' },
        { value: 'OTHER', label: 'Annat' }
      ],
      
      // Billing headers
      billingHeaders: [
        { text: 'År', value: 'year', sortable: true },
        { text: 'Beskrivning', value: 'description', sortable: true },
        { text: 'Typ', value: 'type', sortable: true },
        { text: 'Belopp', value: 'totalAmount', sortable: true },
        { text: 'Fakturor', value: 'invoiceCount', sortable: true },
        { text: 'Förfallodatum', value: 'dueDate', sortable: true },
        { text: 'Åtgärder', value: 'actions', sortable: false, align: 'right' }
      ],
      
      // Property count for billing
      propertyCount: 0,
      
      // Option to generate invoices directly
      generateInvoicesDirectly: false,
      
      // Snackbar
      snackbar: false,
      snackbarMessage: '',
      snackbarColor: 'success'
    };
  },
  
  computed: {
    formattedIssueDate() {
      return this.formatDateForDisplay(this.editedBilling.issueDate);
    },
    
    formattedDueDate() {
      return this.formatDateForDisplay(this.editedBilling.dueDate);
    }
  },
  
  created() {
    this.fetchBillings();
    this.fetchPropertyCount();
  },
  
  methods: {
    async fetchBillings() {
      this.loading = true;
      
      try {
        const response = await billingService.getAllBillings();
        this.billings = response.data;
      } catch (error) {
        console.error('Fel vid hämtning av utdebiteringar:', error);
        this.showSnackbar('Kunde inte hämta utdebiteringar', 'error');
      } finally {
        this.loading = false;
      }
    },
    
    async fetchPropertyCount() {
      try {
        const response = await propertyService.getAllProperties();
        this.propertyCount = response.data.length;
      } catch (error) {
        console.error('Fel vid hämtning av fastigheter:', error);
      }
    },
    
    openNewBillingDialog() {
      this.editedBilling = { ...this.defaultBilling };
      // Sätt standardår till innevarande år
      this.editedBilling.year = new Date().getFullYear();
      // Sätt standardbeskrivning
      this.editedBilling.description = `Utdebitering av ordinarie årsavgift ${this.editedBilling.year}`;
      this.billingDialog = true;
    },
    
    editBilling(billing) {
      this.editedBilling = { ...billing };
      this.billingDialog = true;
    },
    
    closeBillingDialog() {
      this.billingDialog = false;
      this.$refs.form.reset();
    },
    
    async saveBilling() {
      if (this.$refs.form.validate()) {
        this.loading = true;
        
        try {
          let response;
          
          if (this.editedBilling.id) {
            // Uppdatera befintlig utdebitering
            response = await billingService.updateBilling(
              this.editedBilling.id, 
              this.editedBilling
            );
            this.showSnackbar('Utdebiteringen har uppdaterats', 'success');
          } else {
            // Skapa ny utdebitering
            response = await billingService.createBilling(
              this.editedBilling, 
              this.generateInvoicesDirectly
            );
            this.showSnackbar('Ny utdebitering har skapats', 'success');
            
            // Om vi genererar fakturor direkt, visa också ett meddelande om det
            if (this.generateInvoicesDirectly) {
              this.showSnackbar(`${this.propertyCount} fakturor har genererats`, 'success');
            }
          }
          
          // Uppdatera listan
          await this.fetchBillings();
          this.billingDialog = false;
          
          // Om vi genererade fakturor direkt, navigera till fakturaöversikt
          if (this.generateInvoicesDirectly && response.data.id) {
            // Navigera till fakturor för denna utdebitering
            this.$router.push(`/billings/${response.data.id}/invoices`);
          }
        } catch (error) {
          console.error('Fel vid sparande av utdebitering:', error);
          this.showSnackbar('Kunde inte spara utdebiteringen', 'error');
        } finally {
          this.loading = false;
        }
      }
    },
    
    viewBilling(billing) {
      // Navigera till detaljvy för utdebitering
      this.$router.push(`/billings/${billing.id}`);
    },
    
    viewInvoices(billing) {
      // Navigera till fakturor för denna utdebitering
      this.$router.push(`/billings/${billing.id}/invoices`);
    },
    
    generateInvoices(billing) {
      this.selectedBilling = billing;
      this.confirmDialog = true;
    },
    
    async confirmGenerateInvoices() {
      if (!this.selectedBilling) return;
      
      this.loading = true;
      
      try {
        await billingService.generateInvoices(this.selectedBilling.id);
        this.showSnackbar(`${this.propertyCount} fakturor har genererats`, 'success');
        
        // Uppdatera listan
        await this.fetchBillings();
        
        // Navigera till fakturor för denna utdebitering
        this.$router.push(`/billings/${this.selectedBilling.id}/invoices`);
      } catch (error) {
        console.error('Fel vid generering av fakturor:', error);
        this.showSnackbar('Kunde inte generera fakturor', 'error');
      } finally {
        this.loading = false;
        this.confirmDialog = false;
      }
    },
    
    getBillingTypeLabel(type) {
      const foundType = this.billingTypes.find(t => t.value === type);
      return foundType ? foundType.label : type;
    },
    
    getBillingTypeColor(type) {
      switch (type) {
        case 'ANNUAL_FEE':
          return 'var(--color-primary-light)';
        case 'EXTRA_CHARGE':
          return 'var(--color-accent-dark)';
        default:
          return 'var(--color-secondary-light)';
      }
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
    
    formatDate(dateString) {
      if (!dateString) return '';
      
      try {
        return format(parseISO(dateString), 'd MMM yyyy', { locale: sv });
      } catch (error) {
        return dateString;
      }
    },
    
    formatDateForDisplay(dateString) {
      if (!dateString) return '';
      
      try {
        return format(parseISO(dateString), 'd MMMM yyyy', { locale: sv });
      } catch (error) {
        return dateString;
      }
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

/* Tabellknappstilar */
.table-action-btn {
  width: 36px;
  height: 36px;
  min-width: 0;
  border-radius: 18px !important;
  margin: 0 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
  position: relative;
  overflow: hidden;
}

.table-action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0) 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.table-action-btn:hover::before {
  opacity: 1;
}

.table-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15) !important;
}

.table-action-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1) !important;
}

.table-action-btn .v-icon {
  font-size: 16px;
  transition: transform 0.2s ease;
}

.table-action-btn:hover .v-icon {
  transform: scale(1.15);
}

.view-action {
  background-color: var(--color-primary-light) !important;
  color: white !important;
}

.edit-action {
  background-color: var(--color-secondary-light) !important;
  color: white !important;
}

.invoice-action {
  background-color: var(--color-accent-light) !important;
  color: white !important;
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
  
}
</style>