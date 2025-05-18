<template>
  <v-container>
    <div class="d-flex align-center mb-6">
      <v-btn
        variant="text"
        color="primary"
        prepend-icon="mdi-arrow-left"
        class="mr-4"
        @click="$router.push('/invoices')"
      >
        Tillbaka till fakturor
      </v-btn>
      <h1 class="text-h4">Fakturadetaljer</h1>
    </div>

    <!-- Loader -->
    <v-progress-linear
      v-if="loading"
      indeterminate
      color="primary"
      class="mb-4"
    ></v-progress-linear>

    <v-row v-if="!loading && invoice">
      <!-- Invoice Details Section -->
      <v-col cols="12" md="6">
        <v-card class="mb-4 elevation-2" min-height="100%">
          <v-card-title class="d-flex align-center">
            <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-file-document</v-icon>
            <span class="text-h5">Fakturainformation</span>
            <v-chip
              class="ml-4"
              :color="getStatusColor(invoice)"
              text-color="white"
              size="small"
            >
              {{ getStatusLabel(invoice) }}
            </v-chip>
          </v-card-title>
          <v-divider></v-divider>
          
          <v-card-text class="pa-golden">
            <!-- Invoice Header Information -->
            <div class="invoice-header mb-4">
              <div class="d-flex flex-column">
                <div class="d-flex justify-space-between align-center">
                  <span class="invoice-number text-h5">{{ invoice.invoiceNumber }}</span>
                  <v-btn
                    v-if="invoice.status !== 'PAID' && invoice.status !== 'CANCELLED'"
                    color="success"
                    variant="tonal"
                    size="small"
                    prepend-icon="mdi-cash-plus"
                    @click="openPaymentDialog"
                  >
                    Registrera betalning
                  </v-btn>
                </div>
                <span class="subtitle-1 text-medium-emphasis">{{ invoice.billingDescription }}</span>
              </div>
            </div>

            <!-- Invoice Key Details -->
            <v-list>
              <v-list-item>
                <template v-slot:prepend>
                  <v-icon color="primary">mdi-home</v-icon>
                </template>
                <v-list-item-title>Fastighet</v-list-item-title>
                <template v-slot:append>
                  <span class="text-subtitle-1">{{ invoice.propertyDesignation }}</span>
                </template>
              </v-list-item>

              <v-list-item>
                <template v-slot:prepend>
                  <v-icon color="primary">mdi-account</v-icon>
                </template>
                <v-list-item-title>Ägare</v-list-item-title>
                <template v-slot:append>
                  <span class="text-subtitle-1">{{ invoice.ownerName }}</span>
                </template>
              </v-list-item>

              <v-list-item>
                <template v-slot:prepend>
                  <v-icon color="primary">mdi-cash-multiple</v-icon>
                </template>
                <v-list-item-title>Belopp</v-list-item-title>
                <template v-slot:append>
                  <span class="text-subtitle-1 font-weight-bold">{{ formatCurrency(invoice.amount) }}</span>
                </template>
              </v-list-item>

              <v-list-item>
                <template v-slot:prepend>
                  <v-icon color="primary">mdi-calendar</v-icon>
                </template>
                <v-list-item-title>Fakturadatum</v-list-item-title>
                <template v-slot:append>
                  <span class="text-subtitle-1">{{ formatDate(invoice.invoiceDate) }}</span>
                </template>
              </v-list-item>

              <v-list-item>
                <template v-slot:prepend>
                  <v-icon :color="isOverdue(invoice) ? 'error' : 'primary'">mdi-calendar-clock</v-icon>
                </template>
                <v-list-item-title>Förfallodatum</v-list-item-title>
                <template v-slot:append>
                  <div :class="{ 'text-error': isOverdue(invoice) }">
                    <span class="text-subtitle-1">{{ formatDate(invoice.dueDate) }}</span>
                    <v-tooltip v-if="isOverdue(invoice)">
                      <template v-slot:activator="{ props }">
                        <v-icon
                          small
                          color="error"
                          class="ml-2"
                          v-bind="props"
                        >
                          mdi-alert-circle
                        </v-icon>
                      </template>
                      <span>Förfallen ({{ getDaysOverdue(invoice) }} dagar sen)</span>
                    </v-tooltip>
                  </div>
                </template>
              </v-list-item>

              <v-list-item v-if="invoice.ocrNumber">
                <template v-slot:prepend>
                  <v-icon color="primary">mdi-barcode</v-icon>
                </template>
                <v-list-item-title>OCR-nummer</v-list-item-title>
                <template v-slot:append>
                  <span class="text-subtitle-1">{{ invoice.ocrNumber }}</span>
                </template>
              </v-list-item>

              <v-list-item v-if="invoice.billingId">
                <template v-slot:prepend>
                  <v-icon color="primary">mdi-file-document-multiple</v-icon>
                </template>
                <v-list-item-title>Utdebitering</v-list-item-title>
                <template v-slot:append>
                  <span class="text-subtitle-1">{{ invoice.billingReference || invoice.billingId }}</span>
                </template>
              </v-list-item>
            </v-list>

            <!-- Payment Status -->
            <v-divider class="my-4"></v-divider>
            <h3 class="subtitle-1 mb-3">Betalningsstatus</h3>

            <div class="payment-status-box pa-4 mb-4" :class="getPaymentStatusClass()">
              <div class="d-flex justify-space-between align-center">
                <div>
                  <div class="font-weight-medium">{{ getPaymentStatusText() }}</div>
                  <div class="text-caption">
                    {{ getPaymentStatusSubtext() }}
                  </div>
                </div>
                <div class="payment-amount-info text-right">
                  <div v-if="invoice.status === 'PARTIALLY_PAID' || invoice.status === 'PAID'">
                    <div class="font-weight-medium">{{ formatCurrency(calculatePaidAmount()) }}</div>
                    <div class="text-caption">Inbetalt</div>
                  </div>
                  <div v-if="invoice.status === 'PARTIALLY_PAID'">
                    <div class="font-weight-medium text-error">{{ formatCurrency(getRemainingAmount()) }}</div>
                    <div class="text-caption">Återstår</div>
                  </div>
                </div>
              </div>

              <v-progress-linear
                v-if="invoice.status !== 'CANCELLED'"
                :model-value="calculatePaymentPercentage()"
                height="10"
                rounded
                :color="getPaymentProgressColor()"
                class="mt-3"
              ></v-progress-linear>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <!-- Payments and Actions Section -->
      <v-col cols="12" md="6">
        <v-card class="mb-4 elevation-2" min-height="100%">
          <v-card-title class="d-flex align-center">
            <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-history</v-icon>
            <span class="text-h5">Betalningshistorik</span>
            <v-spacer></v-spacer>
            <v-btn
              v-if="invoice.status !== 'PAID' && invoice.status !== 'CANCELLED'"
              color="var(--color-primary-light)"
              variant="tonal"
              prepend-icon="mdi-cash-plus"
              @click="openPaymentDialog"
            >
              Registrera betalning
            </v-btn>
          </v-card-title>
          <v-divider></v-divider>

          <v-card-text class="pa-golden">
            <v-data-table
              :headers="paymentHeaders"
              :items="payments"
              :loading="loadingPayments"
              no-data-text="Inga betalningar har registrerats"
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
                        icon
                        density="comfortable"
                        size="small"
                        color="secondary"
                        @click="editPayment(item)"
                        v-bind="props" 
                      >
                        <v-icon size="small">mdi-pencil</v-icon>
                      </v-btn>
                    </template>
                    <span>Redigera betalning</span>
                  </v-tooltip>
                  
                  <v-tooltip bottom>
                    <template v-slot:activator="{ props }">
                      <v-btn 
                        class="table-action-btn"
                        icon
                        density="comfortable"
                        size="small"
                        color="error"
                        @click="confirmDeletePayment(item)"
                        v-bind="props" 
                      >
                        <v-icon size="small">mdi-delete</v-icon>
                      </v-btn>
                    </template>
                    <span>Ta bort betalning</span>
                  </v-tooltip>
                </div>
              </template>
            </v-data-table>

            <!-- Available Actions -->
            <v-divider class="my-4"></v-divider>
            <h3 class="subtitle-1 mb-3">Åtgärder</h3>

            <div class="d-flex flex-wrap">
              <v-btn
                color="var(--color-primary-light)"
                variant="outlined"
                class="me-2 mb-2"
                prepend-icon="mdi-email"
                @click="sendInvoice"
                :loading="sendingEmail"
              >
                Skicka faktura
              </v-btn>

              <v-btn
                color="var(--color-secondary-light)"
                variant="outlined"
                class="me-2 mb-2"
                prepend-icon="mdi-download"
                @click="downloadInvoice"
                :loading="downloading"
              >
                Ladda ner PDF
              </v-btn>

              <v-btn
                v-if="invoice.status !== 'CANCELLED'"
                color="var(--color-accent-light)"
                variant="outlined"
                class="me-2 mb-2"
                prepend-icon="mdi-email-alert"
                @click="sendReminder"
                :loading="sendingReminder"
                :disabled="!isOverdue(invoice)"
              >
                Skicka påminnelse
              </v-btn>

              <v-btn
                v-if="invoice.status !== 'CANCELLED'"
                color="error"
                variant="outlined"
                class="mb-2"
                prepend-icon="mdi-cancel"
                @click="confirmCancelInvoice"
              >
                Makulera faktura
              </v-btn>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <!-- PDF Viewer Section -->
      <v-col cols="12">
        <v-card class="elevation-2">
          <v-card-title class="d-flex align-center">
            <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-file-pdf-box</v-icon>
            <span class="text-h5">Faktura PDF</span>
            <v-spacer></v-spacer>
            <v-btn
              color="var(--color-primary-light)"
              variant="tonal"
              prepend-icon="mdi-refresh"
              @click="refreshPdfView"
            >
              Uppdatera
            </v-btn>
          </v-card-title>
          <v-divider></v-divider>
          
          <v-card-text class="pa-golden">
            <div v-if="!pdfUrl" class="pdf-placeholder">
              <div class="d-flex flex-column align-center justify-center py-8">
                <v-icon size="64" color="grey lighten-1" class="mb-4">mdi-file-pdf-box</v-icon>
                <h3 class="text-h6 text-center">PDF förhandsvisning inte tillgänglig</h3>
                <p class="text-subtitle-1 text-center text-medium-emphasis mb-4">
                  PDF förhandsvisning kommer att vara tillgänglig i en framtida version
                </p>
                <v-btn
                  color="var(--color-primary-light)"
                  variant="tonal"
                  prepend-icon="mdi-download"
                  @click="downloadInvoice"
                  :loading="downloading"
                >
                  Ladda ner faktura PDF
                </v-btn>
              </div>
            </div>
            <div v-else class="pdf-container">
              <!-- PDF Viewer will be implemented here -->
              <div class="pdf-viewer-placeholder">
                <pdf-viewer 
                  :src="pdfUrl" 
                  :page="1"
                  style="height: 600px; width: 100%;"
                ></pdf-viewer>
              </div>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Empty State -->
    <v-card v-if="!loading && !invoice" class="empty-state pa-golden">
      <div class="text-center py-8">
        <v-icon size="64" color="grey lighten-1" class="mb-4">mdi-file-document-off</v-icon>
        <h3 class="text-h5 mb-2">Fakturan kunde inte hittas</h3>
        <p class="text-subtitle-1 text-medium-emphasis mb-4">
          Fakturan du söker finns inte eller har tagits bort
        </p>
        <v-btn
          color="var(--color-primary-light)"
          prepend-icon="mdi-file-document-multiple"
          @click="$router.push('/invoices')"
        >
          Visa alla fakturor
        </v-btn>
      </div>
    </v-card>

    <!-- Dialog for Payment Registration -->
    <v-dialog v-model="paymentDialog" max-width="600px">
      <v-card class="dialog-card">
        <v-card-title class="text-h5 dialog-title">
          <v-icon color="var(--color-primary-dark)" class="mr-2">mdi-cash-plus</v-icon>
          Registrera betalning
        </v-card-title>
        <v-divider></v-divider>
        
        <v-card-text class="pa-golden">
          <v-form ref="paymentForm" v-model="validPaymentForm" lazy-validation>
            <div v-if="invoice" class="mb-4">
              <div class="d-flex justify-space-between mb-2">
                <span class="subtitle-2">Faktura:</span>
                <span class="font-weight-medium">{{ invoice.invoiceNumber }}</span>
              </div>
              <div class="d-flex justify-space-between mb-2">
                <span class="subtitle-2">Fastighet:</span>
                <span>{{ invoice.propertyDesignation }}</span>
              </div>
              <div class="d-flex justify-space-between mb-2">
                <span class="subtitle-2">Ägare:</span>
                <span>{{ invoice.ownerName }}</span>
              </div>
              <div class="d-flex justify-space-between mb-2">
                <span class="subtitle-2">Fakturabelopp:</span>
                <span class="font-weight-medium">{{ formatCurrency(invoice.amount) }}</span>
              </div>
              <div class="d-flex justify-space-between mb-2">
                <span class="subtitle-2">Återstående att betala:</span>
                <span class="font-weight-medium error--text">{{ formatCurrency(getRemainingAmount()) }}</span>
              </div>
            </div>
            
            <v-divider class="mb-4"></v-divider>
            
            <v-row>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="payment.amount"
                  label="Belopp"
                  type="number"
                  required
                  :rules="[
                    v => !!v || 'Belopp är obligatoriskt',
                    v => v > 0 || 'Belopp måste vara större än 0',
                  ]"
                  prefix="SEK"
                  hint="Betalt belopp"
                  outlined
                  dense
                ></v-text-field>
              </v-col>
              
              <v-col cols="12" sm="6">
                <v-menu
                  v-model="paymentDateMenu"
                  :close-on-content-click="false"
                  transition="scale-transition"
                  offset-y
                  min-width="290px"
                >
                  <template v-slot:activator="{ props }">
                    <v-text-field
                      v-model="formattedPaymentDate"
                      label="Betalningsdatum"
                      readonly
                      outlined
                      dense
                      v-bind="props"
                      required
                      :rules="[v => !!v || 'Betalningsdatum är obligatoriskt']"
                    ></v-text-field>
                  </template>
                  <v-date-picker
                    v-model="payment.paymentDate"
                    @input="paymentDateMenu = false"
                    locale="sv-SE"
                  ></v-date-picker>
                </v-menu>
              </v-col>
            </v-row>
            
            <v-row>
              <v-col cols="12">
                <v-select
                  v-model="payment.paymentType"
                  label="Betalningssätt"
                  :items="paymentTypes"
                  item-title="label"
                  item-value="value"
                  required
                  :rules="[v => !!v || 'Betalningssätt är obligatoriskt']"
                  hint="Välj hur betalningen gjordes"
                  outlined
                  dense
                ></v-select>
              </v-col>
            </v-row>
            
            <v-row>
              <v-col cols="12">
                <v-textarea
                  v-model="payment.comment"
                  label="Kommentar"
                  hint="Valfri kommentar om betalningen"
                  outlined
                  dense
                  rows="3"
                ></v-textarea>
              </v-col>
            </v-row>
            
            <v-alert
              v-if="remainingAfterPayment < 0"
              type="warning"
              outlined
              dense
              class="mt-4"
            >
              Inbetalningen är {{ formatCurrency(Math.abs(remainingAfterPayment)) }} högre än fakturabeloppet.
            </v-alert>
          </v-form>
        </v-card-text>
        
        <v-divider></v-divider>
        
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            variant="outlined"
            color="grey-darken-1"
            @click="closePaymentDialog"
            class="me-4"
          >
            Avbryt
          </v-btn>
          <v-btn
            color="primary"
            variant="elevated"
            :disabled="!validPaymentForm"
            @click="savePayment"
            :loading="savingPayment"
            elevation="2"
          >
            <v-icon start>mdi-content-save</v-icon>
            Registrera betalning
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog for Delete Payment Confirmation -->
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

    <!-- Dialog for Cancel Invoice Confirmation -->
    <v-dialog v-model="cancelInvoiceDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h5">Makulera faktura?</v-card-title>
        <v-divider></v-divider>
        <v-card-text class="pa-golden">
          <p>Är du säker på att du vill makulera denna faktura?</p>
          <p class="mt-2">Detta kommer att:</p>
          <ul>
            <li>Märka fakturan som makulerad</li>
            <li>Förhindra att betalningar kan registreras</li>
          </ul>
          <v-alert
            v-if="payments && payments.length > 0"
            type="warning"
            outlined
            dense
            class="mt-4"
          >
            Denna faktura har redan betalningar registrerade. Vid makulering kommer inte betalningarna att tas bort.
          </v-alert>
          
          <v-text-field
            v-model="cancelReason"
            label="Anledning till makulering"
            hint="Frivillig kommentar om varför fakturan makuleras"
            outlined
            dense
            class="mt-4"
          ></v-text-field>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn 
            color="grey lighten-1" 
            text 
            @click="cancelInvoiceDialog = false"
          >
            Avbryt
          </v-btn>
          <v-btn 
            color="error"
            @click="cancelInvoice"
            :loading="cancellingInvoice"
          >
            <v-icon left>mdi-cancel</v-icon>
            Makulera faktura
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar for Notifications -->
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
import { defineComponent } from 'vue';
import { format, parseISO, differenceInDays } from 'date-fns';
import { sv } from 'date-fns/locale';
import invoiceService from '../services/invoice.service';

// Import PdfViewer placeholder component - this will be replaced later with a real PDF viewer component
const PdfViewer = {
  props: ['src', 'page'],
  template: `
    <div class="pdf-viewer-placeholder">
      <p class="text-center">PDF Viewer kommer att implementeras här</p>
    </div>
  `
};

export default defineComponent({
  name: 'InvoiceDetailView',
  components: {
    PdfViewer
  },

  data() {
    return {
      // Invoice data
      invoice: null,
      loading: true,
      invoiceId: null,
      
      // Payments data
      payments: [],
      loadingPayments: false,
      
      // PDF viewer
      pdfUrl: null,
      pdfLoading: false,
      
      // Action states
      downloading: false,
      sendingEmail: false,
      sendingReminder: false,
      cancellingInvoice: false,
      
      // Payment dialog
      paymentDialog: false,
      paymentDateMenu: false,
      validPaymentForm: true,
      savingPayment: false,
      payment: this.getDefaultPayment(),
      
      // Delete payment dialog
      deletePaymentDialog: false,
      selectedPayment: null,
      deletingPayment: false,
      
      // Cancel invoice dialog
      cancelInvoiceDialog: false,
      cancelReason: '',
      
      // Payment headers for the table
      paymentHeaders: [
        { title: 'Datum', key: 'paymentDate', sortable: true },
        { title: 'Belopp', key: 'amount', sortable: true, align: 'right' },
        { title: 'Betalningssätt', key: 'paymentType', sortable: true },
        { title: 'Kommentar', key: 'comment', sortable: false },
        { title: 'Åtgärder', key: 'actions', sortable: false, align: 'right' }
      ],
      
      // Payment types
      paymentTypes: [
        { value: 'BANKGIRO', label: 'Bankgiro' },
        { value: 'POSTGIRO', label: 'Postgiro' },
        { value: 'SWISH', label: 'Swish' },
        { value: 'MANUAL', label: 'Manuell' },
        { value: 'OTHER', label: 'Annan' }
      ],
      
      // Snackbar
      snackbar: false,
      snackbarMessage: '',
      snackbarColor: 'success'
    };
  },

  computed: {
    formattedPaymentDate() {
      if (!this.payment.paymentDate) return '';
      
      try {
        return format(parseISO(this.payment.paymentDate), 'd MMMM yyyy', { locale: sv });
      } catch (error) {
        return this.payment.paymentDate;
      }
    },
    
    remainingAfterPayment() {
      if (!this.invoice || !this.payment.amount) return 0;
      
      const currentPaidAmount = this.calculatePaidAmount();
      const totalAfterNewPayment = currentPaidAmount + parseFloat(this.payment.amount || 0);
      
      return this.invoice.amount - totalAfterNewPayment;
    }
  },

  created() {
    // Get the invoice ID from the route
    this.invoiceId = this.$route.params.id;
    
    if (this.invoiceId) {
      this.fetchInvoice();
    } else {
      this.loading = false;
    }
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
    
    async fetchInvoice() {
      this.loading = true;
      
      try {
        const response = await invoiceService.getInvoiceById(this.invoiceId);
        this.invoice = response.data;
        
        // Fetch payments for this invoice
        this.fetchInvoicePayments();
      } catch (error) {
        console.error('Error fetching invoice:', error);
        this.showSnackbar('Kunde inte hämta faktura', 'error');
      } finally {
        this.loading = false;
      }
    },
    
    async fetchInvoicePayments() {
      this.loadingPayments = true;
      
      try {
        const response = await invoiceService.getInvoicePayments(this.invoiceId);
        this.payments = response.data || [];
      } catch (error) {
        console.error('Error fetching invoice payments:', error);
        this.showSnackbar('Kunde inte hämta betalningar', 'error');
        this.payments = [];
      } finally {
        this.loadingPayments = false;
      }
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
          return invoice.status;
      }
    },
    
    getStatusColor(invoice) {
      if (!invoice || !invoice.status) return 'grey';
      
      if (this.isOverdue(invoice) && invoice.status !== 'PAID' && invoice.status !== 'CANCELLED') {
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
    
    getPaymentStatusClass() {
      if (!this.invoice || !this.invoice.status) return '';
      
      if (this.isOverdue(this.invoice) && this.invoice.status !== 'PAID' && this.invoice.status !== 'CANCELLED') {
        return 'overdue-status';
      }
      
      switch (this.invoice.status) {
        case 'CREATED':
          return 'created-status';
        case 'SENT':
          return 'sent-status';
        case 'PARTIALLY_PAID':
          return 'partial-status';
        case 'PAID':
          return 'paid-status';
        case 'CANCELLED':
          return 'cancelled-status';
        default:
          return '';
      }
    },
    
    getPaymentStatusText() {
      if (!this.invoice || !this.invoice.status) return '';
      
      if (this.isOverdue(this.invoice) && this.invoice.status !== 'PAID' && this.invoice.status !== 'CANCELLED') {
        return 'Fakturan är förfallen';
      }
      
      switch (this.invoice.status) {
        case 'CREATED':
          return 'Fakturan är skapad';
        case 'SENT':
          return 'Fakturan är skickad';
        case 'PARTIALLY_PAID':
          return 'Fakturan är delvis betald';
        case 'PAID':
          return 'Fakturan är betald';
        case 'CANCELLED':
          return 'Fakturan är makulerad';
        default:
          return '';
      }
    },
    
    getPaymentStatusSubtext() {
      if (!this.invoice || !this.invoice.status) return '';
      
      if (this.isOverdue(this.invoice) && this.invoice.status !== 'PAID' && this.invoice.status !== 'CANCELLED') {
        return `Förfallen sedan ${this.getDaysOverdue(this.invoice)} dagar`;
      }
      
      switch (this.invoice.status) {
        case 'CREATED':
          return 'Fakturan har inte skickats än';
        case 'SENT':
          return `Förfaller om ${this.getDaysUntilDue(this.invoice)} dagar`;
        case 'PARTIALLY_PAID':
          return `${this.calculatePaymentPercentage()}% av beloppet har betalats`;
        case 'PAID':
          return 'Fullständigt betald';
        case 'CANCELLED':
          return 'Fakturan är inte längre giltig';
        default:
          return '';
      }
    },
    
    getPaymentProgressColor() {
      if (!this.invoice || !this.invoice.status) return 'grey';
      
      switch (this.invoice.status) {
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
        default:
          return 'grey';
      }
    },
    
    isOverdue(invoice) {
      if (!invoice || !invoice.dueDate) return false;
      
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
    
    getDaysUntilDue(invoice) {
      if (!invoice || !invoice.dueDate) return 0;
      
      try {
        const dueDate = parseISO(invoice.dueDate);
        const today = new Date();
        
        if (dueDate < today) {
          return 0;
        }
        
        return differenceInDays(dueDate, today);
      } catch (error) {
        console.error('Error calculating days until due:', error);
        return 0;
      }
    },
    
    calculatePaidAmount() {
      if (!this.invoice) return 0;
      
      if (this.payments.length === 0) {
        return 0;
      }
      
      return this.payments.reduce((total, payment) => total + parseFloat(payment.amount || 0), 0);
    },
    
    getRemainingAmount() {
      if (!this.invoice) return 0;
      
      const amount = parseFloat(this.invoice.amount) || 0;
      const paidAmount = this.calculatePaidAmount();
      
      return amount - paidAmount;
    },
    
    calculatePaymentPercentage() {
      if (!this.invoice) return 0;
      
      const amount = parseFloat(this.invoice.amount) || 0;
      if (amount === 0) return 0;
      
      const paidAmount = this.calculatePaidAmount();
      return Math.round((paidAmount / amount) * 100);
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
    
    openPaymentDialog() {
      this.payment = this.getDefaultPayment();
      
      // Default to remaining amount
      if (this.invoice.status === 'PARTIALLY_PAID') {
        this.payment.amount = this.getRemainingAmount();
      } else {
        this.payment.amount = this.invoice.amount;
      }
      
      this.paymentDialog = true;
    },
    
    closePaymentDialog() {
      this.paymentDialog = false;
      this.payment = this.getDefaultPayment();
      
      if (this.$refs.paymentForm) {
        this.$refs.paymentForm.reset();
      }
    },
    
    async savePayment() {
      if (!this.$refs.paymentForm.validate()) return;
      
      this.savingPayment = true;
      
      try {
        await invoiceService.registerPayment(this.invoice.id, {
          amount: parseFloat(this.payment.amount),
          paymentDate: this.payment.paymentDate,
          paymentType: this.payment.paymentType,
          comment: this.payment.comment
        });
        
        this.showSnackbar('Betalning har registrerats', 'success');
        this.closePaymentDialog();
        
        // Refresh invoice and payments
        this.fetchInvoice();
      } catch (error) {
        console.error('Error registering payment:', error);
        this.showSnackbar('Kunde inte registrera betalning', 'error');
      } finally {
        this.savingPayment = false;
      }
    },
    
    editPayment(payment) {
      this.payment = {
        id: payment.id,
        amount: payment.amount,
        paymentDate: payment.paymentDate,
        paymentType: payment.paymentType,
        comment: payment.comment
      };
      
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
        await invoiceService.deletePayment(this.selectedPayment.id);
        
        this.showSnackbar('Betalning har tagits bort', 'success');
        this.deletePaymentDialog = false;
        this.selectedPayment = null;
        
        // Refresh invoice and payments
        this.fetchInvoice();
      } catch (error) {
        console.error('Error deleting payment:', error);
        this.showSnackbar('Kunde inte ta bort betalning', 'error');
      } finally {
        this.deletingPayment = false;
      }
    },
    
    confirmCancelInvoice() {
      this.cancelInvoiceDialog = true;
      this.cancelReason = '';
    },
    
    async cancelInvoice() {
      this.cancellingInvoice = true;
      
      try {
        await invoiceService.updateInvoiceStatus(this.invoice.id, 'CANCELLED');
        
        // Add cancellation comment if provided
        if (this.cancelReason) {
          // Here you would implement a method to save the cancellation reason
          // This might be via a dedicated API endpoint or comment system
        }
        
        this.showSnackbar('Fakturan har makulerats', 'success');
        this.cancelInvoiceDialog = false;
        
        // Refresh invoice
        this.fetchInvoice();
      } catch (error) {
        console.error('Error cancelling invoice:', error);
        this.showSnackbar('Kunde inte makulera faktura', 'error');
      } finally {
        this.cancellingInvoice = false;
      }
    },
    
    async downloadInvoice() {
      this.downloading = true;
      
      try {
        const response = await invoiceService.exportInvoicesToPdf([this.invoice.id]);
        
        // Create a blob from the PDF data
        const blob = new Blob([response.data], { type: 'application/pdf' });
        
        // Create a URL for the blob
        const url = window.URL.createObjectURL(blob);
        
        // Create a link element and trigger download
        const link = document.createElement('a');
        link.href = url;
        link.download = `faktura_${this.invoice.invoiceNumber}.pdf`;
        link.click();
        
        // Clean up by revoking the URL
        window.URL.revokeObjectURL(url);
        
        this.showSnackbar('Faktura PDF har laddats ner', 'success');
      } catch (error) {
        console.error('Error downloading invoice PDF:', error);
        this.showSnackbar('Kunde inte ladda ner faktura PDF', 'error');
      } finally {
        this.downloading = false;
      }
    },
    
    async sendInvoice() {
      this.sendingEmail = true;
      
      try {
        // This would typically call an API endpoint to send the invoice via email
        // For now we'll just simulate this with a delay
        setTimeout(() => {
          this.showSnackbar('Faktura har skickats', 'success');
          this.sendingEmail = false;
        }, 1500);
      } catch (error) {
        console.error('Error sending invoice:', error);
        this.showSnackbar('Kunde inte skicka faktura', 'error');
        this.sendingEmail = false;
      }
    },
    
    async sendReminder() {
      if (!this.isOverdue(this.invoice)) return;
      
      this.sendingReminder = true;
      
      try {
        // This would typically call an API endpoint to send a reminder email
        // For now we'll just simulate this with a delay
        setTimeout(() => {
          this.showSnackbar('Påminnelse har skickats', 'success');
          this.sendingReminder = false;
        }, 1500);
      } catch (error) {
        console.error('Error sending reminder:', error);
        this.showSnackbar('Kunde inte skicka påminnelse', 'error');
        this.sendingReminder = false;
      }
    },
    
    refreshPdfView() {
      // This method would normally fetch the PDF URL from the server
      // For now we'll just simulate this
      this.pdfLoading = true;
      
      setTimeout(() => {
        // In a real implementation, this would set a URL to the PDF on the server
        // For now, we'll keep it null to show the placeholder
        this.pdfUrl = null;
        this.pdfLoading = false;
        this.showSnackbar('PDF förhandsvisning är inte tillgänglig ännu', 'info');
      }, 1000);
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
});
</script>

<style scoped>
.pa-golden {
  padding: 16px;
}

.mb-golden {
  margin-bottom: 24px;
}

.invoice-header {
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}

.invoice-number {
  font-weight: 600;
}

.payment-status-box {
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  background-color: rgba(0, 0, 0, 0.02);
}

.created-status {
  border-left: 4px solid #9e9e9e;
}

.sent-status {
  border-left: 4px solid #2196F3;
}

.partial-status {
  border-left: 4px solid #FFC107;
}

.paid-status {
  border-left: 4px solid #4CAF50;
  background-color: rgba(76, 175, 80, 0.05);
}

.overdue-status {
  border-left: 4px solid #F44336;
  background-color: rgba(244, 67, 54, 0.05);
}

.cancelled-status {
  border-left: 4px solid #616161;
  background-color: rgba(97, 97, 97, 0.05);
  text-decoration: line-through;
}

.actions-container {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.table-action-btn {
  transition: all 0.2s ease;
}

.table-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.empty-state {
  border-radius: 8px;
  background-color: #f5f5f5;
}

.pdf-placeholder {
  border: 2px dashed #e0e0e0;
  border-radius: 8px;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pdf-viewer-placeholder {
  background-color: #f5f5f5;
  border-radius: 8px;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Dialog title styles */
.dialog-title {
  background-color: #f5f5f5;
}

/* Custom table styles */
.custom-table {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
}
</style>