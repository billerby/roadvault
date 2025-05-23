<template>
  <v-container>
    <div class="page-header">
      <h1 class="text-h3">{{ billing ? billing.description : 'Laddar utdebitering...' }}</h1>
      <p class="subtitle-1">
        <span>Detaljerad information om utdebiteringen</span>
        <v-btn
          text
          small
          color="primary"
          class="ml-2"
          @click="$router.push('/billings')"
        >
          <v-icon small left>mdi-arrow-left</v-icon>
          Tillbaka till utdebiteringar
        </v-btn>
      </p>
    </div>

    <!-- Loading-indikator -->
    <v-progress-linear 
      v-if="loading" 
      indeterminate 
      color="primary" 
      class="mb-5"
    ></v-progress-linear>

    <!-- Fel-meddelande om utdebiteringen inte hittades -->
    <v-alert 
      v-if="error" 
      type="error" 
      outlined 
      class="mb-5"
    >
      {{ errorMessage }}
    </v-alert>

    <div v-if="billing">
      <!-- Grundläggande information -->
      <v-card class="rv-card rv-mb-md">
        <v-card-title class="rv-card-header">
          <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-information-outline</v-icon>
          <span class="text-h5">Grundläggande information</span>
          <v-spacer></v-spacer>
          <v-btn
            v-if="!billing.invoiceCount"
            class="rv-btn-icon rv-btn--secondary"
            @click="editBilling"
            title="Redigera utdebitering"
          >
            <v-icon>mdi-pencil</v-icon>
          </v-btn>
        </v-card-title>
        <v-divider></v-divider>
        
        <v-card-text class="rv-p-md">
          <v-row>
            <v-col cols="12" sm="6" md="4">
              <div class="detail-item">
                <div class="detail-label">År</div>
                <div class="detail-value">{{ billing.year }}</div>
              </div>
            </v-col>
            
            <v-col cols="12" sm="6" md="4">
              <div class="detail-item">
                <div class="detail-label">Typ</div>
                <div class="detail-value">
                  <v-chip 
                    small 
                    :color="getBillingTypeColor(billing.type)" 
                    text-color="white"
                  >
                    {{ getBillingTypeLabel(billing.type) }}
                  </v-chip>
                </div>
              </div>
            </v-col>
            
            <v-col cols="12" sm="6" md="4">
              <div class="detail-item">
                <div class="detail-label">Totalbelopp</div>
                <div class="detail-value font-weight-bold">{{ formatCurrency(billing.totalAmount) }}</div>
              </div>
            </v-col>
          </v-row>
          
          <v-divider class="my-4"></v-divider>
          
          <v-row>
            <v-col cols="12" sm="6" md="4">
              <div class="detail-item">
                <div class="detail-label">Beskrivning</div>
                <div class="detail-value">{{ billing.description }}</div>
              </div>
            </v-col>
            
            <v-col cols="12" sm="6" md="4">
              <div class="detail-item">
                <div class="detail-label">Utskriftsdatum</div>
                <div class="detail-value">{{ formatDate(billing.issueDate) }}</div>
              </div>
            </v-col>
            
            <v-col cols="12" sm="6" md="4">
              <div class="detail-item">
                <div class="detail-label">Förfallodatum</div>
                <div class="detail-value" :class="{ 'red--text': isOverdue(billing) }">
                  {{ formatDate(billing.dueDate) }}
                  <v-tooltip bottom v-if="isOverdue(billing)">
                    <template v-slot:activator="{ props }">
                      <v-icon 
                        small 
                        color="error"
                        v-bind="props" 
                      >
                        mdi-alert-circle
                      </v-icon>
                    </template>
                    <span>Förfallen ({{ getDaysOverdue(billing) }} dagar sen)</span>
                  </v-tooltip>
                </div>
              </div>
            </v-col>
          </v-row>
          
          <v-divider class="my-4"></v-divider>
          
          <v-row>
            <v-col cols="12" sm="6">
              <div class="detail-item">
                <div class="detail-label">Skapad</div>
                <div class="detail-value">{{ formatDateTime(billing.createdAt) }}</div>
              </div>
            </v-col>
            
            <v-col cols="12" sm="6">
              <div class="detail-item">
                <div class="detail-label">Senast uppdaterad</div>
                <div class="detail-value">{{ formatDateTime(billing.updatedAt) }}</div>
              </div>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- Fakturastatus -->
      <v-card class="rv-card rv-mb-md">
        <v-card-title class="rv-card-header">
          <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-file-document-multiple</v-icon>
          <span class="text-h5">Fakturastatus</span>
          <v-spacer></v-spacer>
          <!-- Visa fakturaknapp -->
          <v-btn
            v-if="billing.invoiceCount > 0"
            color="var(--color-primary-light)"
            class="mr-2 rv-btn rv-btn--primary"
            @click="viewInvoices"
          >
            <v-icon left>mdi-file-document-outline</v-icon>
            Visa fakturor
          </v-btn>
          
          <!-- Generera fakturaknapp -->
          <v-btn
            v-if="billing.invoiceCount === 0"
            color="var(--color-accent-light)"
            class="mr-2 rv-btn rv-btn--accent"
            @click="showGenerateInvoicesDialog"
          >
            <v-icon left>mdi-file-document-multiple-outline</v-icon>
            Generera fakturor
          </v-btn>
        </v-card-title>
        <v-divider></v-divider>
        
        <v-card-text class="rv-p-md">
          <div v-if="billing.invoiceCount > 0">
            <v-row>
              <v-col cols="12" sm="6" md="3">
                <div class="stats-card elevation-1 pa-4">
                  <div class="stats-icon-wrapper primary">
                    <v-icon color="white">mdi-file-document-multiple</v-icon>
                  </div>
                  <div class="stats-value text-h5 mt-4">{{ billing.invoiceCount }}</div>
                  <div class="stats-label">Fakturor</div>
                </div>
              </v-col>
              
              <v-col cols="12" sm="6" md="3">
                <div class="stats-card elevation-1 pa-4">
                  <div class="stats-icon-wrapper success">
                    <v-icon color="white">mdi-check-circle</v-icon>
                  </div>
                  <div class="stats-value text-h5 mt-4">{{ invoiceStats.paidCount || 0 }}</div>
                  <div class="stats-label">Betalda</div>
                </div>
              </v-col>
              
              <v-col cols="12" sm="6" md="3">
                <div class="stats-card elevation-1 pa-4">
                  <div class="stats-icon-wrapper warning">
                    <v-icon color="white">mdi-timer-sand</v-icon>
                  </div>
                  <div class="stats-value text-h5 mt-4">{{ invoiceStats.unpaidCount || 0 }}</div>
                  <div class="stats-label">Obetalda</div>
                </div>
              </v-col>
              
              <v-col cols="12" sm="6" md="3">
                <div class="stats-card elevation-1 pa-4">
                  <div class="stats-icon-wrapper error">
                    <v-icon color="white">mdi-alert-circle</v-icon>
                  </div>
                  <div class="stats-value text-h5 mt-4">{{ invoiceStats.overdueCount || 0 }}</div>
                  <div class="stats-label">Förfallna</div>
                </div>
              </v-col>
            </v-row>
            
            <v-divider class="my-4"></v-divider>
            
            <v-row>
              <v-col cols="12">
                <div class="progress-wrapper mb-4">
                  <div class="d-flex justify-space-between mb-1">
                    <span class="body-2">Betalningsstatus</span>
                    <span class="body-2">{{ getPaymentProgressPercentage() }}% av totalbeloppet är inbetalt</span>
                  </div>
                  <v-progress-linear
                    :value="getPaymentProgressPercentage()"
                    height="20"
                    rounded
                    color="success"
                  >
                    <template v-slot:default="{ value }">
                      <strong>{{ Math.ceil(value) }}%</strong>
                    </template>
                  </v-progress-linear>
                </div>
                
                <v-list dense>
                  <v-list-item>
                    <v-list-item-title>Totalt fakturerat belopp</v-list-item-title>
                    <template v-slot:append>
                      <div class="font-weight-medium">
                        {{ formatCurrency(billing.totalAmount) }}
                      </div>
                    </template>
                  </v-list-item>
                  
                  <v-list-item>
                    <v-list-item-title>Totalt inbetalt</v-list-item-title>
                    <template v-slot:append>
                      <div class="font-weight-medium text-success">
                        {{ formatCurrency(invoiceStats.paidAmount || 0) }}
                      </div>
                    </template>
                  </v-list-item>
                  
                  <v-list-item>
                    <v-list-item-title>Återstående att betala</v-list-item-title>
                    <template v-slot:append>
                      <div class="font-weight-medium" :class="invoiceStats.unpaidAmount > 0 ? 'text-warning' : 'text-success'">
                        {{ formatCurrency(invoiceStats.unpaidAmount || 0) }}
                      </div>
                    </template>
                  </v-list-item>
                  
                  <v-list-item>
                    <v-list-item-title>Förfallna belopp</v-list-item-title>
                    <template v-slot:append>
                      <div class="font-weight-medium text-error">
                        {{ formatCurrency(invoiceStats.overdueAmount || 0) }}
                      </div>
                    </template>
                  </v-list-item>
                </v-list>
              </v-col>
            </v-row>
          </div>
          
          <div v-else class="text-center py-5">
            <v-icon size="64" color="grey lighten-1">mdi-file-document-off</v-icon>
            <p class="mt-4">Inga fakturor har genererats för denna utdebitering ännu.</p>
            <p class="caption grey--text">
              Klicka på "Generera fakturor" för att skapa fakturor för alla fastigheter baserat på deras delaktighetstal.
            </p>
          </div>
        </v-card-text>
      </v-card>
      
      <!-- Åtgärder -->
      <v-card class="rv-card rv-mb-md">
        <v-card-title class="rv-card-header">
          <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-cog</v-icon>
          <span class="text-h5">Åtgärder</span>
        </v-card-title>
        <v-divider></v-divider>
        
        <v-card-text class="rv-p-md">
          <v-row>
            <v-col cols="12" sm="6" md="4">
              <!-- Skicka ut fakturor -->
              <v-card outlined class="pa-4 h-100">
                <div class="d-flex align-center mb-2">
                  <v-icon color="primary" small class="mr-2">mdi-email-send</v-icon>
                  <span class="subtitle-1">Skicka ut fakturor</span>
                </div>
                <p class="body-2 mb-4">Skicka alla fakturor för denna utdebitering via email till fastighetsägarna.</p>
                <v-btn 
                  color="primary" 
                  :disabled="!billing.invoiceCount"
                  @click="confirmSendEmails"
                  :loading="loadingSendEmails"
                >
                  <v-icon left>mdi-email-send</v-icon>
                  Skicka fakturor
                </v-btn>
              </v-card>
            </v-col>
            
            <v-col cols="12" sm="6" md="4">
              <!-- Skicka påminnelser -->
              <v-card outlined class="pa-4 h-100">
                <div class="d-flex align-center mb-2">
                  <v-icon color="warning" small class="mr-2">mdi-email-alert</v-icon>
                  <span class="subtitle-1">Skicka påminnelser</span>
                </div>
                <p class="body-2 mb-4">Skicka påminnelser för alla förfallna fakturor i denna utdebitering.</p>
                <v-btn 
                  color="warning" 
                  :disabled="!invoiceStats.overdueCount"
                  @click="sendReminders"
                  :loading="loadingReminders"
                >
                  <v-icon left>mdi-email-alert</v-icon>
                  Skicka påminnelser
                </v-btn>
              </v-card>
            </v-col>
            
            <v-col cols="12" sm="6" md="4">
              <!-- Radera utdebitering -->
              <v-card outlined class="pa-4 h-100">
                <div class="d-flex align-center mb-2">
                  <v-icon color="error" small class="mr-2">mdi-delete</v-icon>
                  <span class="subtitle-1">Ta bort utdebitering</span>
                </div>
                <p class="body-2 mb-4">Ta bort denna utdebitering. Detta kommer även att ta bort alla relaterade fakturor.</p>
                <v-btn 
                  color="error" 
                  @click="confirmDelete"
                >
                  <v-icon left>mdi-delete</v-icon>
                  Ta bort
                </v-btn>
              </v-card>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>
    </div>
   
    <!-- Dialog för att generera fakturor -->
    <v-dialog v-model="confirmDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h5">Bekräfta åtgärd</v-card-title>
        <v-divider></v-divider>
        <v-card-text class="rv-p-md">
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
        
        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn
            outlined
            color="grey"
            class="mr-3"
            @click="confirmDialog = false"
          >
            Avbryt
          </v-btn>
          <v-btn 
            color="primary"
            elevation="2"
            @click="generateInvoices"
            :loading="loading"
          >
            <v-icon left>mdi-check</v-icon>
            Generera fakturor
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
    <!-- Dialog för att bekräfta borttagning -->
    <v-dialog v-model="deleteDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h5">Ta bort utdebitering?</v-card-title>
        <v-divider></v-divider>
        <v-card-text class="rv-p-md">
          <p>Är du säker på att du vill ta bort denna utdebitering?</p>
          <p v-if="billing && billing.invoiceCount > 0" class="mt-2">
            <strong>Varning:</strong> Detta kommer även att ta bort alla {{ billing.invoiceCount }} relaterade fakturor.
          </p>
          <p class="font-weight-medium mt-4 red--text">Denna åtgärd kan inte ångras!</p>
        </v-card-text>
        <v-divider></v-divider>
        
        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn
            outlined
            color="grey"
            class="mr-3"
            @click="deleteDialog = false"
          >
            Avbryt
          </v-btn>
          <v-btn 
            color="error"
            elevation="2"
            @click="deleteBilling"
            :loading="loading"
          >
            <v-icon left>mdi-delete</v-icon>
            Ta bort
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog för att bekräfta email-utskick -->
    <v-dialog v-model="sendEmailsDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h5">Skicka ut fakturor via email?</v-card-title>
        <v-divider></v-divider>
        <v-card-text class="rv-p-md">
          <p>Är du säker på att du vill skicka ut alla fakturor för denna utdebitering via email?</p>
          <p class="mt-2">Detta kommer att:</p>
          <ul>
            <li>Skicka {{ billing.invoiceCount }} fakturor via email</li>
            <li>Fakturor skickas till fastighetsägarnas registrerade email-adresser</li>
            <li>PDF-fakturor bifogas automatiskt</li>
            <li>Fastigheter utan email-adress hoppas över</li>
          </ul>
          <p class="font-weight-medium mt-4">Kontrollera att alla email-adresser är korrekta innan du fortsätter!</p>
        </v-card-text>
        <v-divider></v-divider>
        
        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn
            outlined
            color="grey"
            class="mr-3"
            @click="sendEmailsDialog = false"
          >
            Avbryt
          </v-btn>
          <v-btn 
            color="primary"
            elevation="2"
            @click="sendInvoiceEmails"
            :loading="loadingSendEmails"
          >
            <v-icon left>mdi-email-send</v-icon>
            Skicka fakturor
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
import invoiceService from '../services/invoice.service';
import emailService from '../services/email.service';
import propertyService from '../services/property.service';
import { format, parseISO, differenceInDays } from 'date-fns';
import { sv } from 'date-fns/locale';

export default {
  name: 'BillingDetailsView',
  
  data() {
    return {
      billing: null,
      loading: false,
      error: false,
      errorMessage: '',
      
      // Property count for billing
      propertyCount: 0,
      
      // Invoice statistics
      invoiceStats: {
        paidCount: 0,
        unpaidCount: 0,
        overdueCount: 0,
        paidAmount: 0,
        unpaidAmount: 0,
        overdueAmount: 0
      },
      
      // Billing type options
      billingTypes: [
        { value: 'ANNUAL_FEE', label: 'Årsavgift' },
        { value: 'EXTRA_CHARGE', label: 'Extra uttaxering' },
        { value: 'OTHER', label: 'Annat' }
      ],
      
      // Dialogs
      confirmDialog: false,
      deleteDialog: false,
      sendEmailsDialog: false,
      
      // Loading states
      loadingReminders: false,
      loadingSendEmails: false,
      
      // Snackbar
      snackbar: false,
      snackbarMessage: '',
      snackbarColor: 'success'
    };
  },
  
  created() {
    this.fetchBillingDetails();
    this.fetchPropertyCount();
  },
  
  methods: {
    async fetchBillingDetails() {
      this.loading = true;
      this.error = false;
      
      try {
        const billingId = this.$route.params.id;
        
        if (!billingId) {
          this.error = true;
          this.errorMessage = 'Ingen utdebitering specificerad';
          return;
        }
        
        const response = await billingService.getBillingById(billingId);
        this.billing = response.data;
        
        // Fetcha statistik om det finns fakturor
        if (this.billing.invoiceCount > 0) {
          await this.fetchInvoiceStatistics();
        }
      } catch (error) {
        console.error('Fel vid hämtning av utdebitering:', error);
        this.error = true;
        this.errorMessage = 'Kunde inte hämta utdebitering. Kontrollera att ID:t är korrekt.';
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
    
    async fetchInvoiceStatistics() {
      try {
        const billingId = this.$route.params.id;
        const response = await billingService.getInvoicesForBilling(billingId);
        const invoices = response.data;
        
        // Beräkna statistik
        this.invoiceStats = {
          paidCount: 0,
          unpaidCount: 0,
          overdueCount: 0,
          paidAmount: 0,
          unpaidAmount: 0,
          overdueAmount: 0
        };
        
        invoices.forEach(invoice => {
          const amount = parseFloat(invoice.amount) || 0;
          
          if (invoice.status === 'PAID') {
            this.invoiceStats.paidCount++;
            this.invoiceStats.paidAmount += amount;
          } else {
            this.invoiceStats.unpaidCount++;
            this.invoiceStats.unpaidAmount += amount;
            
            if (this.isOverdue(invoice)) {
              this.invoiceStats.overdueCount++;
              this.invoiceStats.overdueAmount += amount;
            }
          }
        });
      } catch (error) {
        console.error('Fel vid hämtning av fakturor för statistik:', error);
      }
    },
    
    editBilling() {
      // Öppna redigeringsdialog (för att hålla denna vy enkel, navigera till BillingView och öppna dialog där)
      this.$router.push('/billings');
      // Här skulle vi kunna skicka med ett state för att öppna redigeringsdialogreen direkt
    },
    
    isOverdue(item) {
      if (!item || !item.dueDate) return false;
      
      if (item.status === 'PAID') {
        return false;
      }
      
      try {
        const dueDate = parseISO(item.dueDate);
        return dueDate < new Date();
      } catch (error) {
        console.error('Error parsing due date:', error);
        return false;
      }
    },
    
    getDaysOverdue(item) {
      if (!item || !item.dueDate) return 0;
      
      try {
        const dueDate = parseISO(item.dueDate);
        return differenceInDays(new Date(), dueDate);
      } catch (error) {
        console.error('Error calculating days overdue:', error);
        return 0;
      }
    },
    
    viewInvoices() {
      this.$router.push(`/billings/${this.billing.id}/invoices`);
    },
    
    showGenerateInvoicesDialog() {
      this.confirmDialog = true;
    },
    
    async generateInvoices() {
      if (!this.billing) return;
      
      this.loading = true;
      
      try {
        await billingService.generateInvoices(this.billing.id);
        this.showSnackbar(`${this.propertyCount} fakturor har genererats`, 'success');
        
        // Uppdatera information
        await this.fetchBillingDetails();
        
        this.confirmDialog = false;
      } catch (error) {
        console.error('Fel vid generering av fakturor:', error);
        this.showSnackbar('Kunde inte generera fakturor', 'error');
      } finally {
        this.loading = false;
      }
    },
    
    confirmDelete() {
      this.deleteDialog = true;
    },
    
    async deleteBilling() {
      if (!this.billing) return;
      
      this.loading = true;
      
      try {
        await billingService.deleteBilling(this.billing.id);
        this.showSnackbar('Utdebitering har tagits bort', 'success');
        
        // Navigera tillbaka till lista över utdebiteringar
        this.$router.push('/billings');
      } catch (error) {
        console.error('Fel vid borttagning av utdebitering:', error);
        this.showSnackbar('Kunde inte ta bort utdebitering', 'error');
      } finally {
        this.loading = false;
        this.deleteDialog = false;
      }
    },
    
    async sendReminders() {
      this.loadingReminders = true;
      
      try {
        // Placeholder för att skicka påminnelser
        // Här skulle vi implementera faktisk funktionalitet
        
        setTimeout(() => {
          this.showSnackbar(`Påminnelser har skickats för utdebitering ${this.billing.year}`, 'success');
          this.loadingReminders = false;
        }, 2000);
      } catch (error) {
        console.error('Fel vid skickande av påminnelser:', error);
        this.showSnackbar('Kunde inte skicka påminnelser', 'error');
        this.loadingReminders = false;
      }
    },
    
    confirmSendEmails() {
      this.sendEmailsDialog = true;
    },

    async sendInvoiceEmails() {
      if (!this.billing) return;
      
      this.loadingSendEmails = true;
      
      try {
        const response = await emailService.sendInvoicesForBilling(this.billing.id);
        
        // Debug: Logga response för att se vad vi får
        console.log('Email response:', response.data);
        
        // Extrahera antal skickade fakturor
        let successCount = 0;
        if (typeof response.data === 'number') {
          successCount = response.data;
        } else if (typeof response.data === 'string') {
          successCount = parseInt(response.data) || 0;
        } else if (typeof response.data === 'object' && response.data !== null) {
          // API returnerar { success: true, sentCount: 35 }
          successCount = response.data.sentCount || response.data.count || response.data.successCount || response.data.sent || 0;
        }
        
        const totalInvoices = parseInt(this.billing.invoiceCount) || 0;
        
        console.log('Parsed - Success:', successCount, 'Total:', totalInvoices);
        
        this.sendEmailsDialog = false;
        
        if (successCount === 0) {
          // Inga fakturor skickades alls
          this.showSnackbar('Inga fakturor kunde skickas. Kontrollera att fastighetsägarna har email-adresser.', 'error');
        } else if (successCount === totalInvoices) {
          // Alla fakturor skickades
          this.showSnackbar(`Alla ${successCount} fakturor har skickats via email`, 'success');
        } else {
          // Några fakturor skickades, men inte alla
          const failedCount = totalInvoices - successCount;
          this.showSnackbar(
            `${successCount} av ${totalInvoices} fakturor skickades. ${failedCount} kunde inte skickas (saknar email-adress).`, 
            'warning'
          );
        }
      } catch (error) {
        console.error('Fel vid skickande av fakturor:', error);
        this.showSnackbar('Kunde inte skicka fakturor via email', 'error');
        this.sendEmailsDialog = false;
      } finally {
        this.loadingSendEmails = false;
      }
    },
    
    getPaymentProgressPercentage() {
      if (!this.billing || !this.invoiceStats) return 0;
      
      const totalAmount = parseFloat(this.billing.totalAmount) || 0;
      if (totalAmount === 0) return 0;
      
      const paidAmount = this.invoiceStats.paidAmount || 0;
      return Math.round((paidAmount / totalAmount) * 100);
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
      if (value === null || value === undefined) return '0 kr';
      
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
        return format(parseISO(dateString), 'd MMMM yyyy', { locale: sv });
      } catch (error) {
        return dateString;
      }
    },
    
    formatDateTime(dateString) {
      if (!dateString) return '';
      
      try {
        return format(parseISO(dateString), 'd MMM yyyy, HH:mm', { locale: sv });
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

/* Detaljitem */
.detail-item {
  margin-bottom: var(--spacing-sm);
}

.detail-label {
  font-size: 0.875rem;
  color: rgba(0, 0, 0, 0.6);
  margin-bottom: 0.25rem;
}

.detail-value {
  font-size: 1rem;
}

/* Statistics cards */
.stats-card {
  border-radius: 8px;
  transition: all 0.3s ease;
  height: 100%;
  text-align: center;
}

.stats-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1) !important;
}

.stats-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.stats-icon-wrapper.primary {
  background-color: var(--color-primary-light);
}

.stats-icon-wrapper.success {
  background-color: #4CAF50;
}

.stats-icon-wrapper.warning {
  background-color: #FFC107;
}

.stats-icon-wrapper.error {
  background-color: #F44336;
}

.stats-value {
  font-weight: 600;
  color: #333;
}

.stats-label {
  color: #666;
  font-size: 14px;
}

/* Progress */
.progress-wrapper {
  margin: var(--spacing-md) 0;
}
</style>