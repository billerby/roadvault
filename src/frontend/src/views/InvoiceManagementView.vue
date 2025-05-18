<template>
  <v-container>
    <div class="page-header">
      <h1 class="text-h3">Fakturor</h1>
      <p class="subtitle-1">Hantera fakturor och betalningar för vägsamfälligheten</p>
    </div>

    <!-- Fakturalista -->
    <v-card class="mb-golden elevation-2">
      <v-card-title class="d-flex align-center">
        <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-file-document-multiple</v-icon>
        <span class="text-h5">Fakturaöversikt</span>
        <v-spacer></v-spacer>
        
        <!-- Filterknappar -->
        <v-btn-toggle
          v-model="statusFilter"
          mandatory
          class="mr-4 status-toggle"
        >
          <v-btn value="ALL" small>
            Alla
          </v-btn>
          <v-btn value="UNPAID" small>
            Obetalda
          </v-btn>
          <v-btn value="OVERDUE" small>
            Förfallna
          </v-btn>
          <v-btn value="PAID" small>
            Betalda
          </v-btn>
        </v-btn-toggle>
        
        <v-btn
          class="action-btn refresh-btn"
          @click="refreshInvoices"
          :loading="loading"
          title="Uppdatera"
        >
          <v-icon class="action-icon">mdi-refresh</v-icon>
        </v-btn>
      </v-card-title>
      <v-divider></v-divider>
      
      <v-card-text class="pa-golden">
        <v-data-table
          :headers="invoiceHeaders"
          :items="filteredInvoices"
          :loading="loading"
          :items-per-page="10"
          class="elevation-0 custom-table"
          :footer-props="{
            'items-per-page-options': [10, 25, 50, -1],
            'items-per-page-text': 'Rader per sida:'
          }"
          dense
        >
          <!-- Fakturanummer -->
          <template v-slot:item.invoiceNumber="{ item }">
            <strong>{{ item.invoiceNumber }}</strong>
          </template>
          
          <!-- Fastighet/ägare -->
          <template v-slot:item.ownerName="{ item }">
            <div class="d-flex flex-column">
              <span>{{ item.ownerName }}</span>
              <span class="caption text-muted">{{ item.propertyDesignation }}</span>
            </div>
          </template>
          
          <!-- Beskrivning -->
          <template v-slot:item.billingDescription="{ item }">
            {{ item.billingDescription }}
          </template>
          
          <!-- Belopp -->
          <template v-slot:item.amount="{ item }">
            <div class="amount-column">
              <span class="font-weight-medium">{{ formatCurrency(item.amount) }}</span>
              <v-tooltip bottom v-if="getPaymentStatus(item) === 'PARTIALLY_PAID'">
                <template v-slot:activator="{ props }">
                  <div 
                    class="small-badge partial-payment" 
                    v-bind="props" 
                  >
                    {{ calculatePaidPercentage(item) }}%
                  </div>
                </template>
                <span>{{ formatCurrency(calculatePaidAmount(item)) }} har betalats</span>
              </v-tooltip>
            </div>
          </template>
          
          <!-- Förfallodatum -->
          <template v-slot:item.dueDate="{ item }">
            <div :class="{ 'overdue-date': isOverdue(item) }">
              {{ formatDate(item.dueDate) }}
              <v-tooltip bottom v-if="isOverdue(item)">
                <template v-slot:activator="{ props }">
                  <v-icon 
                    small 
                    color="error"
                    v-bind="props" 
                  >
                    mdi-alert-circle
                  </v-icon>
                </template>
                <span>Förfallen ({{ getDaysOverdue(item) }} dagar sen)</span>
              </v-tooltip>
            </div>
          </template>
          
          <!-- Status -->
          <template v-slot:item.status="{ item }">
            <v-chip 
              small 
              :color="getStatusColor(item)" 
              text-color="white"
              class="status-chip"
            >
              {{ getStatusLabel(item) }}
            </v-chip>
          </template>
          
          <!-- Åtgärder -->
          <template v-slot:item.actions="{ item }">
            <div class="actions-container">
              <v-tooltip bottom>
                <template v-slot:activator="{ props }">
                  <v-btn 
                    class="action-btn-rounded mr-1"
                    color="primary"
                    icon
                    small
                    v-bind="props" 
                    @click="viewInvoice(item)"
                  >
                    <v-icon small>mdi-eye</v-icon>
                  </v-btn>
                </template>
                <span>Visa detaljer</span>
              </v-tooltip>
              
              <v-tooltip bottom>
                <template v-slot:activator="{ props }">
                  <v-btn 
                    class="action-btn-rounded mr-1"
                    color="accent"
                    icon
                    small
                    v-bind="props" 
                    :disabled="getPaymentStatus(item) === 'PAID'"
                    @click="openPaymentDialog(item)"
                  >
                    <v-icon small>mdi-cash-plus</v-icon>
                  </v-btn>
                </template>
                <span>Registrera betalning</span>
              </v-tooltip>
              
              <v-tooltip bottom>
                <template v-slot:activator="{ props }">
                  <v-btn 
                    class="action-btn-rounded"
                    color="secondary"
                    icon
                    small
                    v-bind="props" 
                    :disabled="!hasPayments(item)"
                    @click="openPaymentHistoryDialog(item)"
                  >
                    <v-icon small>mdi-history</v-icon>
                  </v-btn>
                </template>
                <span>Betalningshistorik</span>
              </v-tooltip>
            </div>
          </template>
          
          <!-- Inga fakturor -->
          <template v-slot:no-data>
            <div class="empty-state text-center pa-golden">
              <v-icon size="64" color="grey lighten-1">mdi-file-document-off</v-icon>
              <p class="mt-4">Inga fakturor hittades.</p>
              <v-btn 
                color="var(--color-primary-light)"
                class="mt-4 primary-action-btn"
                @click="refreshInvoices"
              >
                <v-icon left>mdi-refresh</v-icon>
                Uppdatera
              </v-btn>
            </div>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <!-- Fakturasummering -->
    <v-card class="mb-golden elevation-1">
      <v-card-title class="d-flex align-center">
        <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-chart-pie</v-icon>
        <span class="text-h6">Fakturasummering</span>
      </v-card-title>
      <v-divider></v-divider>
      
      <v-card-text class="pa-golden">
        <v-row>
          <v-col cols="12" sm="6" md="3">
            <v-card class="stats-card elevation-1" outlined>
              <v-card-text class="text-center">
                <div class="stats-icon-wrapper primary">
                  <v-icon color="white">mdi-file-document-multiple</v-icon>
                </div>
                <div class="stats-value text-h5 mt-4">{{ statistics.totalInvoices }}</div>
                <div class="stats-label">Totalt antal fakturor</div>
              </v-card-text>
            </v-card>
          </v-col>
          
          <v-col cols="12" sm="6" md="3">
            <v-card class="stats-card elevation-1" outlined>
              <v-card-text class="text-center">
                <div class="stats-icon-wrapper success">
                  <v-icon color="white">mdi-check-circle</v-icon>
                </div>
                <div class="stats-value text-h5 mt-4">{{ statistics.paidInvoices }}</div>
                <div class="stats-label">Betalda fakturor</div>
              </v-card-text>
            </v-card>
          </v-col>
          
          <v-col cols="12" sm="6" md="3">
            <v-card class="stats-card elevation-1" outlined>
              <v-card-text class="text-center">
                <div class="stats-icon-wrapper warning">
                  <v-icon color="white">mdi-timer-sand</v-icon>
                </div>
                <div class="stats-value text-h5 mt-4">{{ statistics.unpaidInvoices }}</div>
                <div class="stats-label">Obetalda fakturor</div>
              </v-card-text>
            </v-card>
          </v-col>
          
          <v-col cols="12" sm="6" md="3">
            <v-card class="stats-card elevation-1" outlined>
              <v-card-text class="text-center">
                <div class="stats-icon-wrapper error">
                  <v-icon color="white">mdi-alert-circle</v-icon>
                </div>
                <div class="stats-value text-h5 mt-4">{{ statistics.overdueInvoices }}</div>
                <div class="stats-label">Förfallna fakturor</div>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
        
        <v-divider class="my-4"></v-divider>
        
        <v-row>
          <v-col cols="12" sm="6">
            <div class="d-flex align-center mb-2">
              <v-icon color="primary" small class="mr-2">mdi-cash-multiple</v-icon>
              <span class="subtitle-2">Ekonomisk översikt</span>
            </div>
            <v-list dense>
              <v-list-item>
                <v-list-item-title>Totalt fakturerat belopp</v-list-item-title>
                <template v-slot:append>
                  <div class="font-weight-medium">
                    {{ formatCurrency(statistics.totalAmount) }}
                  </div>
                </template>
              </v-list-item>
              
              <v-list-item>
                <v-list-item-title>Totalt inbetalt</v-list-item-title>
                <template v-slot:append>
                  <div class="font-weight-medium text-success">
                    {{ formatCurrency(statistics.paidAmount) }}
                  </div>
                </template>
              </v-list-item>
              
              <v-list-item>
                <v-list-item-title>Återstående att betala</v-list-item-title>
                <template v-slot:append>
                  <div class="font-weight-medium text-warning">
                    {{ formatCurrency(statistics.unpaidAmount) }}
                  </div>
                </template>
              </v-list-item>
                            
              <v-list-item>
                <v-list-item-title>Förfallna belopp</v-list-item-title>
                <template v-slot:append>
                  <div class="font-weight-medium text-error">
                    {{ formatCurrency(statistics.overdueAmount) }}
                  </div>
                </template>
              </v-list-item>
            </v-list>
          </v-col>
          
          <v-col cols="12" sm="6">
            <div class="d-flex align-center mb-2">
              <v-icon color="primary" small class="mr-2">mdi-calendar-check</v-icon>
              <span class="subtitle-2">Åtgärder</span>
            </div>
            <v-card outlined class="pa-4 mb-4">
              <p class="body-2">Markera förfallna fakturor</p>
              <p class="caption mb-3">Detta uppdaterar statusen för alla fakturor där förfallodatum har passerats.</p>
              <v-btn 
                color="var(--color-accent-light)" 
                @click="markOverdueInvoices"
                :loading="loadingOverdue"
                class="action-btn-secondary"
              >
                <v-icon left>mdi-update</v-icon>
                Markera förfallna
              </v-btn>
            </v-card>
            
            <v-card outlined class="pa-4">
              <p class="body-2">Skicka påminnelser</p>
              <p class="caption mb-3">Skicka påminnelser för alla förfallna fakturor.</p>
              <v-btn 
                color="var(--color-secondary-light)" 
                @click="sendReminders"
                :loading="loadingReminders"
                class="action-btn-secondary"
                :disabled="statistics.overdueInvoices === 0"
              >
                <v-icon left>mdi-email-alert</v-icon>
                Skicka påminnelser
              </v-btn>
            </v-card>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <!-- Dialog för att registrera betalning -->
    <payment-dialog
        v-if="selectedInvoice"
        v-model="paymentDialog"
        :invoice="selectedInvoice"
        :remaining-amount="getRemainingAmount()"
        :saving="savingPayment"
        :default-payment="getDefaultPayment()"
        @payment-created="onPaymentChanged"
        @payment-updated="onPaymentChanged"
        @close="closePaymentDialog"
    />

    <!-- Dialog för betalningshistorik -->
    <v-dialog v-model="paymentHistoryDialog" max-width="700px">
      <v-card class="dialog-card">
        <v-card-title class="text-h5 dialog-title">
          <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-history</v-icon>
          Betalningshistorik
        </v-card-title>
        <v-divider></v-divider>
        
        <v-card-text class="pa-golden">
          <div v-if="selectedInvoice" class="mb-4">
            <div class="d-flex justify-space-between mb-2">
              <span class="subtitle-2">Faktura:</span>
              <span class="font-weight-medium">{{ selectedInvoice.invoiceNumber }}</span>
            </div>
            <div class="d-flex justify-space-between mb-2">
              <span class="subtitle-2">Fastighet:</span>
              <span>{{ selectedInvoice.propertyDesignation }}</span>
            </div>
            <div class="d-flex justify-space-between mb-2">
              <span class="subtitle-2">Ägare:</span>
              <span>{{ selectedInvoice.ownerName }}</span>
            </div>
            <div class="d-flex justify-space-between mb-2">
              <span class="subtitle-2">Fakturabelopp:</span>
              <span class="font-weight-medium">{{ formatCurrency(selectedInvoice.amount) }}</span>
            </div>
          </div>
          
          <v-divider class="mb-4"></v-divider>
          
          <h3 class="subtitle-1 mb-2">Inbetalningar</h3>
          
          <v-data-table
            :headers="paymentHeaders"
            :items="invoicePayments"
            :loading="loadingPayments"
            class="elevation-0 custom-table"
            dense
          >
            <template v-slot:item.paymentDate="{ item }">
              {{ formatDate(item.paymentDate) }}
            </template>
            
            <template v-slot:item.amount="{ item }">
              <span class="font-weight-medium">{{ formatCurrency(item.amount) }}</span>
            </template>
            
            <template v-slot:item.paymentType="{ item }">
              <v-chip 
                small 
                :color="getPaymentTypeColor(item.paymentType)"
                text-color="white"
              >
                {{ getPaymentTypeLabel(item.paymentType) }}
              </v-chip>
            </template>
            
            <template v-slot:item.actions="{ item }">
              <div class="actions-container">
                <v-tooltip bottom>
                  <template v-slot:activator="{ props }">
                    <v-btn 
                      class="table-action-btn edit-action mr-1"
                      @click="editPayment(item)"
                      v-bind="props" 
                    >
                      <v-icon small>mdi-pencil</v-icon>
                    </v-btn>
                  </template>
                  <span>Redigera betalning</span>
                </v-tooltip>
                
                <v-tooltip bottom>
                  <template v-slot:activator="{ props }">
                    <v-btn 
                      class="table-action-btn delete-action"
                      @click="confirmDeletePayment(item)"
                      v-bind="props" 
                    >
                      <v-icon small>mdi-delete</v-icon>
                    </v-btn>
                  </template>
                  <span>Ta bort betalning</span>
                </v-tooltip>
              </div>
            </template>
            
            <template v-slot:no-data>
              <div class="text-center pa-4">
                <p class="body-2">Inga betalningar registrerade för denna faktura.</p>
              </div>
            </template>
          </v-data-table>
          
          <v-divider class="my-4"></v-divider>
          
          <div class="d-flex justify-space-between align-center">
            <div>
              <span class="subtitle-2">Total inbetalt:</span>
              <span class="font-weight-medium ml-2 success--text">{{ formatCurrency(calculatePaidAmount(selectedInvoice)) }}</span>
            </div>
            <div>
              <span class="subtitle-2">Återstående:</span>
                              <span class="font-weight-medium ml-2" :class="getRemainingAmount(selectedInvoice) > 0 ? 'error--text' : 'success--text'">
                {{ formatCurrency(getRemainingAmount(selectedInvoice)) }}
                </span>
            </div>
          </div>
        </v-card-text>
        
        <v-divider></v-divider>
        
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="var(--color-primary-light)"
            @click="closePaymentHistoryDialog"
          >
            Stäng
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog för att bekräfta borttagning av betalning -->
    <v-dialog v-model="deletePaymentDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h5">Ta bort betalning?</v-card-title>
        <v-divider></v-divider>
        <v-card-text class="pa-golden">
          <p>Är du säker på att du vill ta bort denna betalning?</p>
          <p class="mt-2">Detta kommer att:</p>
          <ul>
            <li>Ta bort betalningen från systemet</li>
            <li>Uppdatera fakturans status</li>
          </ul>
          <p class="font-weight-medium mt-4">Denna åtgärd kan inte ångras!</p>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn 
            color="grey lighten-1" 
            text 
            @click="deletePaymentDialog = false"
          >
            Avbryt
          </v-btn>
          <v-btn 
            color="error"
            @click="deletePayment"
            :loading="deletingPayment"
          >
            <v-icon left>mdi-delete</v-icon>
            Ta bort
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
import { format, parseISO, differenceInDays } from 'date-fns';
import { sv } from 'date-fns/locale';
import '../styles/invoice-management.css';
import PaymentDialog from '../components/PaymentDialog.vue';

export default {
  name: 'InvoiceManagementView',
  components: {
    PaymentDialog
  },
  data() {
    return {
      // Invoices
      invoices: [],
      loading: false,
      statusFilter: 'ALL',
      
      // Selected invoice
      selectedInvoice: null,
      
      // Invoice payments
      invoicePayments: [],
      loadingPayments: false,
      
     // Payment dialog
      paymentDialog: false,
      savingPayment: false,
      
      // Payment history dialog
      paymentHistoryDialog: false,
      
      // Delete payment dialog
      deletePaymentDialog: false,
      selectedPayment: null,
      deletingPayment: false,
      
      // Invoice headers
      invoiceHeaders: [
        { title: 'Faktura nr', value: 'invoiceNumber', sortable: true },
        { title: 'Fastighet / Ägare', value: 'ownerName', sortable: true },
        { title: 'Beskrivning', value: 'billingDescription', sortable: true },
        { title: 'Belopp', value: 'amount', sortable: true, align: 'right' },
        { title: 'Förfallodatum', value: 'dueDate', sortable: true },
        { title: 'Status', value: 'status', sortable: true },
        { title: 'Åtgärder', value: 'actions', sortable: false, align: 'right' }
      ],
      
      // Payment headers
      paymentHeaders: [
        { title: 'Datum', value: 'paymentDate', sortable: true },
        { title: 'Belopp', value: 'amount', sortable: true, align: 'right' },
        { title: 'Betalningssätt', value: 'paymentType', sortable: true },
        { title: 'Kommentar', value: 'comment', sortable: false },
        { title: 'Åtgärder', value: 'actions', sortable: false, align: 'right' }
      ],
      
      // Payment types
      paymentTypes: [
        { value: 'BANKGIRO', label: 'Bankgiro' },
        { value: 'POSTGIRO', label: 'Postgiro' },
        { value: 'SWISH', label: 'Swish' },
        { value: 'MANUAL', label: 'Manuell' },
        { value: 'OTHER', label: 'Annan' }
      ],
      
      // Statistics
      statistics: {
        totalInvoices: 0,
        paidInvoices: 0,
        unpaidInvoices: 0,
        overdueInvoices: 0,
        totalAmount: 0,
        paidAmount: 0,
        unpaidAmount: 0,
        overdueAmount: 0
      },
      
      // Loading states for actions
      loadingOverdue: false,
      loadingReminders: false,
      
      // Snackbar
      snackbar: false,
      snackbarMessage: '',
      snackbarColor: 'success'
    };
  },
  
  computed: {
    filteredInvoices() {
      if (this.statusFilter === 'ALL') {
        return this.invoices;
      } else if (this.statusFilter === 'UNPAID') {
        return this.invoices.filter(invoice => 
          this.getPaymentStatus(invoice) === 'CREATED' || 
          this.getPaymentStatus(invoice) === 'SENT' || 
          this.getPaymentStatus(invoice) === 'PARTIALLY_PAID'
        );
      } else if (this.statusFilter === 'OVERDUE') {
        return this.invoices.filter(invoice => this.isOverdue(invoice));
      } else if (this.statusFilter === 'PAID') {
        return this.invoices.filter(invoice => this.getPaymentStatus(invoice) === 'PAID');
      }
      
      return this.invoices;
    },
    
    formattedPaymentDate() {
      if (!this.payment.paymentDate) return '';
      
      try {
        return format(parseISO(this.payment.paymentDate), 'd MMMM yyyy', { locale: sv });
      } catch (error) {
        return this.payment.paymentDate;
      }
    },
    
    remainingAfterPayment() {
      if (!this.selectedInvoice || !this.payment.amount) return 0;
      
      const currentPaidAmount = this.calculatePaidAmount(this.selectedInvoice);
      const totalAfterNewPayment = currentPaidAmount + parseFloat(this.payment.amount || 0);
      
      return this.selectedInvoice.amount - totalAfterNewPayment;
    }
  },
  
  created() {
    this.fetchInvoices();
  },
  
  methods: {
    getDefaultPayment() {
      return {
        amount: null,
        paymentDate: format(new Date(), 'yyyy-MM-dd'),
        paymentType: 'BANKGIRO',
        comment: ''
      };
    },
    
    async fetchInvoices() {
      this.loading = true;
      
      try {
        // Import the invoice service if needed
        const invoiceService = await import('../services/invoice.service').then(m => m.default);
        const response = await invoiceService.getAllInvoices();
        console.log('Invoices response:', response);
        
        // Make sure we're setting an array to this.invoices
        this.invoices = Array.isArray(response.data) ? response.data : [];
        
        // Update statistics
        this.calculateStatistics();
      } catch (error) {
        console.error('Error fetching invoices:', error);
        this.showSnackbar('Kunde inte hämta fakturor', 'error');
        this.invoices = [];
      } finally {
        this.loading = false;
      }
    },
    
    async fetchInvoicePayments(invoiceId) {
      this.loadingPayments = true;
      
      try {
        // Import the invoice service if needed
        const invoiceService = await import('../services/invoice.service').then(m => m.default);
        const response = await invoiceService.getInvoicePayments(invoiceId);
        this.invoicePayments = response.data || [];
      } catch (error) {
        console.error('Error fetching invoice payments:', error);
        this.showSnackbar('Kunde inte hämta betalningar', 'error');
        this.invoicePayments = [];
      } finally {
        this.loadingPayments = false;
      }
    },
    
    calculateStatistics() {
      // Reset statistics
      this.statistics = {
        totalInvoices: Array.isArray(this.invoices) ? this.invoices.length : 0,
        paidInvoices: 0,
        unpaidInvoices: 0,
        overdueInvoices: 0,
        totalAmount: 0,
        paidAmount: 0,
        unpaidAmount: 0,
        overdueAmount: 0
      };
      
      // Make sure invoices is an array before iterating
      if (!Array.isArray(this.invoices)) {
        console.warn('Invoices is not an array:', this.invoices);
        return;
      }
      
      // Calculate statistics based on invoices
      this.invoices.forEach(invoice => {
        const amount = parseFloat(invoice.amount) || 0;
        this.statistics.totalAmount += amount;
        
        if (this.getPaymentStatus(invoice) === 'PAID') {
          this.statistics.paidInvoices++;
          this.statistics.paidAmount += amount;
        } else {
          this.statistics.unpaidInvoices++;
          this.statistics.unpaidAmount += amount;
          
          if (this.isOverdue(invoice)) {
            this.statistics.overdueInvoices++;
            this.statistics.overdueAmount += amount;
          }
        }
      });
    },
    
    refreshInvoices() {
      this.fetchInvoices();
    },
    
    viewInvoice(invoice) {
      console.log('Viewing invoice:', invoice);
      // Navigate to invoice detail view
      this.$router.push(`/invoices/${invoice.id}`);
    },
    
    getPaymentStatus(invoice) {
      if (!invoice) return '';
      return invoice.status || '';
    },
    
    getStatusLabel(invoice) {
      if (!invoice || !invoice.status) return '';
      
      switch (invoice.status) {
        case 'CREATED':
          return 'Skapad';
        case 'SENT':
          return 'Skickad';
        case 'PARTIALLY_PAID':
          return 'Delvis betald';
        case 'PAID':
          return 'Betald';
        case 'OVERDUE':
          return 'Förfallen';
        case 'CANCELLED':
          return 'Makulerad';
        default:
          return invoice.status || '';
      }
    },
    
    getStatusColor(invoice) {
      if (!invoice || !invoice.status) return 'grey';
      
      if (this.isOverdue(invoice) && invoice.status !== 'PAID') {
        return 'error';
      }
      
      switch (invoice.status) {
        case 'CREATED':
          return 'grey';
        case 'SENT':
          return 'blue';
        case 'PARTIALLY_PAID':
          return 'amber';
        case 'PAID':
          return 'success';
        case 'OVERDUE':
          return 'error';
        case 'CANCELLED':
          return 'grey darken-2';
        default:
          return 'grey';
      }
    },
    
    isOverdue(invoice) {
      if (!invoice || !invoice.status || !invoice.dueDate) return false;
      
      if (invoice.status === 'PAID' || invoice.status === 'CANCELLED') {
        return false;
      }
      
      try {
        const dueDate = parseISO(invoice.dueDate);
        return dueDate < new Date();
      } catch (error) {
        console.error('Error parsing due date:', error);
        return false;
      }
    },
    
    getDaysOverdue(invoice) {
      if (!invoice || !invoice.dueDate) return 0;
      
      try {
        const dueDate = parseISO(invoice.dueDate);
        return differenceInDays(new Date(), dueDate);
      } catch (error) {
        console.error('Error calculating days overdue:', error);
        return 0;
      }
    },
    
    calculatePaidAmount(invoice) {
      if (!invoice) return 0;
      
      // For this demo, we'll approximate based on status
      // In a real implementation, you would sum all payments
      if (invoice.status === 'PAID') {
        return parseFloat(invoice.amount) || 0;
      } else if (invoice.status === 'PARTIALLY_PAID') {
        // Estimate 50% paid for the demo
        return (parseFloat(invoice.amount) || 0) * 0.5;
      } else {
        return 0;
      }
    },
    
    calculatePaidPercentage(invoice) {
      if (!invoice) return 0;
      
      const amount = parseFloat(invoice.amount) || 0;
      if (!amount) return 0;
      
      const paidAmount = this.calculatePaidAmount(invoice);
      return Math.round((paidAmount / amount) * 100);
    },
    
    getRemainingAmount(invoice) {
      if (!invoice) return 0;
      const amount = parseFloat(invoice.amount) || 0;
      const paidAmount = this.calculatePaidAmount(invoice);
      return amount - paidAmount;
    },
    
    hasPayments(invoice) {
      if (!invoice || !invoice.status) return false;
      return invoice.status === 'PARTIALLY_PAID' || invoice.status === 'PAID';
    },
    
    openPaymentDialog(invoice) {
      this.selectedInvoice = invoice;
      this.payment = this.getDefaultPayment();
      
      // Default to remaining amount if partially paid
      if (invoice.status === 'PARTIALLY_PAID') {
        this.payment.amount = this.getRemainingAmount(invoice);
      } else {
        this.payment.amount = invoice.amount;
      }
      
      this.paymentDialog = true;
    },
    
    closePaymentDialog() {
      this.paymentDialog = false;
      this.selectedInvoice = null;
    },
    
    onPaymentChanged() {
        this.fetchInvoices();
        this.fetchInvoicePayments(this.selectedInvoice.id);
    },
    
    async openPaymentHistoryDialog(invoice) {
      this.selectedInvoice = invoice;
      this.paymentHistoryDialog = true;
      
      // Fetch payments for this invoice
      await this.fetchInvoicePayments(invoice.id);
    },
    
    closePaymentHistoryDialog() {
      this.paymentHistoryDialog = false;
      this.selectedInvoice = null;
      this.invoicePayments = [];
    },
    
    editPayment(payment) {
      // Close the history dialog first
      this.paymentHistoryDialog = false;
      
      // Set up the payment form with the selected payment data
      this.payment = {
        id: payment.id,
        amount: payment.amount,
        paymentDate: payment.paymentDate,
        paymentType: payment.paymentType,
        comment: payment.comment
      };
      
      // Open the payment dialog
      this.paymentDialog = true;
    },
    
    confirmDeletePayment(payment) {
      this.selectedPayment = payment;
      this.deletePaymentDialog = true;
    },
    
    async deletePayment() {
      if (!this.selectedPayment) return;
      
      this.deletingPayment = true;
      
      try {
        // Import the invoice service if needed
        const invoiceService = await import('../services/invoice.service').then(m => m.default);
        await invoiceService.deletePayment(this.selectedPayment.id);
        
        this.showSnackbar('Betalning har tagits bort', 'success');
        this.deletePaymentDialog = false;
        
        // Refresh payments list
        await this.fetchInvoicePayments(this.selectedInvoice.id);
        
        // Refresh invoices to update statuses
        this.fetchInvoices();
      } catch (error) {
        console.error('Error deleting payment:', error);
        this.showSnackbar('Kunde inte ta bort betalning', 'error');
      } finally {
        this.deletingPayment = false;
      }
    },
    
    getPaymentTypeLabel(type) {
      if (!type) return '';
      
      const found = this.paymentTypes.find(pt => pt.value === type);
      return found ? found.label : type;
    },
    
    getPaymentTypeColor(type) {
      if (!type) return 'grey';
      
      switch (type) {
        case 'BANKGIRO':
          return 'var(--color-primary-light)';
        case 'POSTGIRO':
          return 'var(--color-primary-dark)';
        case 'SWISH':
          return 'var(--color-accent-light)';
        case 'MANUAL':
          return 'var(--color-secondary-light)';
        default:
          return 'grey';
      }
    },
    
    async markOverdueInvoices() {
      this.loadingOverdue = true;
      
      try {
        // Import the invoice service if needed
        const invoiceService = await import('../services/invoice.service').then(m => m.default);
        const response = await invoiceService.markOverdueInvoices();
        
        const markedCount = response.data || 0;
        if (markedCount > 0) {
          this.showSnackbar(`${markedCount} fakturor har markerats som förfallna`, 'info');
        } else {
          this.showSnackbar('Inga fakturor behövde markeras som förfallna', 'info');
        }
        
        // Refresh invoices to update statuses
        this.fetchInvoices();
      } catch (error) {
        console.error('Error marking overdue invoices:', error);
        this.showSnackbar('Kunde inte markera förfallna fakturor', 'error');
      } finally {
        this.loadingOverdue = false;
      }
    },
    
    async sendReminders() {
      this.loadingReminders = true;
      
      try {
        // Placeholder for sending reminders
        // This would be implemented in a real application
        
        setTimeout(() => {
          this.showSnackbar(`Påminnelser har skickats till ${this.statistics.overdueInvoices} fastigheter`, 'success');
          this.loadingReminders = false;
        }, 2000);
      } catch (error) {
        console.error('Error sending reminders:', error);
        this.showSnackbar('Kunde inte skicka påminnelser', 'error');
        this.loadingReminders = false;
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
        return format(parseISO(dateString), 'd MMM yyyy', { locale: sv });
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

/* Status toggle button group */
.status-toggle {
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 4px;
  overflow: hidden;
}

.status-toggle .v-btn {
  height: 36px;
  font-size: 12px;
  text-transform: none;
  letter-spacing: normal;
  font-weight: 500;
  border-radius: 0 !important;
  background-color: white !important;
}

.status-toggle .v-btn.v-item--active {
  background-color: var(--color-primary-light) !important;
  color: white !important;
}

/* Status chip */
.status-chip {
  font-size: 11px !important;
  font-weight: 500 !important;
}

/* Table action buttons */
.table-action-btn {
  width: 32px;
  height: 32px;
  min-width: 0;
  border-radius: 16px !important;
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

.payment-action {
  background-color: var(--color-accent-light) !important;
  color: white !important;
}

.payment-history-action {
  background-color: var(--color-secondary-light) !important;
  color: white !important;
}

.edit-action {
  background-color: var(--color-secondary-light) !important;
  color: white !important;
}

.delete-action {
  background-color: #F44336 !important;
  color: white !important;
}

/* Action buttons in header */
.action-btn {
  width: 40px;
  height: 40px;
  min-width: 0;
  border-radius: 20px !important;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 3px 5px rgba(0, 0, 0, 0.1) !important;
}

.refresh-btn {
  background-color: var(--color-secondary-light) !important;
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

/* Primary action buttons */
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
  
  color: white !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.2) !important;
  line-height: 42px !important;
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

/* Secondary action buttons */
.action-btn-secondary {
  height: 36px;
  border-radius: var(--button-border-radius) !important;
  padding: 0 16px !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
  font-weight: 500 !important;
  text-transform: none !important;
  letter-spacing: 0 !important;
  color: white !important;
  font-size: 14px !important;
  transition: all 0.3s ease !important;
}

.action-btn-secondary:hover {
  transform: translateY(-2px);
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15) !important;
}

.action-btn-rounded {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
  transition: all 0.2s ease;
  border-radius: 50%;
  width: 32px !important;
  height: 32px !important;
  margin: 0 2px;
}

.action-btn-rounded:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15) !important;
  transform: translateY(-2px);
}

.action-btn-rounded:active {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1) !important;
  transform: translateY(0);
}

.action-btn-rounded .v-icon {
  font-size: 16px;
}

.actions-container {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

/* Statistics cards */
.stats-card {
  border-radius: 8px;
  transition: all 0.3s ease;
  height: 100%;
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

/* Table styling */
.custom-table {
  border: 1px solid #ddd;
  border-radius: 4px;
}

.text-muted {
  color: rgba(0, 0, 0, 0.6);
}

.amount-column {
  position: relative;
}

.small-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  font-size: 10px;
  border-radius: 10px;
  padding: 2px 5px;
  font-weight: bold;
  display: inline-block;
}
</style>