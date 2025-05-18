<template>
  <v-dialog v-model="dialogVisible" max-width="600px">
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
              <span class="font-weight-medium error--text">{{ formatCurrency(getRemainingAmount) }}</span>
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
              <v-date-input
                v-model="paymentDate"
                :display-format="formatDateISO"
                label="Betalningsdatum"
                placeholder="åååå-mm-dd"
                locale="sv-SE"
                required
                :rules="[v => !!v || 'Betalningsdatum är obligatoriskt']"
                validate-on="blur"
                color="primary"
                density="comfortable"
                clearable
                outlined
              >
                <template v-slot:append>
                  <v-btn
                    icon
                    variant="text"
                    color="primary"
                    size="small"
                    @click="setPaymentDateToday"
                    title="Sätt dagens datum"
                  >
                    <v-icon>mdi-calendar-today</v-icon>
                  </v-btn>
                </template>
              </v-date-input>
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
          @click="closeDialog"
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
</template>

<script setup>
import { ref, computed, watch, toRefs, onMounted, shallowRef } from 'vue';
import { useDate } from 'vuetify';
import invoiceService from '../services/invoice.service';
import { VDateInput } from 'vuetify/labs/VDateInput';

// Initialize date adapter
const adapter = useDate();

const props = defineProps({
  // Whether the dialog is visible
  visible: {
    type: Boolean,
    default: false
  },
  
  // Invoice to register payment for
  invoice: {
    type: Object,
    required: true
  },
  
  // Payment data if editing an existing payment
  editingPayment: {
    type: Object,
    default: null
  },
  
  // Total amount already paid for this invoice (excluding edited payment)
  paidAmount: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits(['update:visible', 'close', 'payment-created', 'payment-updated', 'error']);

// Refs and state
const paymentForm = ref(null);
const validPaymentForm = ref(true);
const savingPayment = ref(false);
const paymentDate = shallowRef(adapter.date(new Date()));

// Payment data
const payment = ref({
  amount: null,
  paymentDate: adapter.toISO(adapter.date(new Date())), // Initially set to today's date in ISO format
  paymentType: 'BANKGIRO',
  comment: ''
});

// Payment types
const paymentTypes = [
  { value: 'BANKGIRO', label: 'Bankgiro' },
  { value: 'POSTGIRO', label: 'Postgiro' },
  { value: 'SWISH', label: 'Swish' },
  { value: 'MANUAL', label: 'Manuell' },
  { value: 'OTHER', label: 'Annan' }
];

// Destructure props for reactivity
const { visible, invoice, editingPayment, paidAmount } = toRefs(props);

// Control dialog visibility with prop
const dialogVisible = computed({
  get: () => visible.value,
  set: (value) => {
    if (!value) {
      emit('update:visible', false);
    }
  }
});

// Calculate remaining amount to be paid for this invoice
const getRemainingAmount = computed(() => {
  if (!invoice.value) return 0;
  
  const amount = parseFloat(invoice.value.amount) || 0;
  const paid = paidAmount.value;
  
  // If editing a payment, exclude it from the paid amount
  if (editingPayment.value) {
    const editingAmount = parseFloat(editingPayment.value.amount) || 0;
    return amount - paid + editingAmount;
  }
  
  return amount - paid;
});

// Calculate remaining amount after this payment
const remainingAfterPayment = computed(() => {
  if (!invoice.value || !payment.value.amount) return 0;
  
  const currentPaidAmount = paidAmount.value;
  const paymentAmount = parseFloat(payment.value.amount) || 0;
  
  // If editing a payment, exclude it from the paid amount
  if (editingPayment.value) {
    const editingAmount = parseFloat(editingPayment.value.amount) || 0;
    return invoice.value.amount - (currentPaidAmount - editingAmount + paymentAmount);
  }
  
  return invoice.value.amount - (currentPaidAmount + paymentAmount);
});

// Format the date to ISO format for display
function formatDateISO(date) {
  if (!date) return '';
  return adapter.toISO(date);
}

// Watch for changes to initialize payment
watch(visible, (newValue) => {
  if (newValue) {
    initializePayment();
  }
});

watch(editingPayment, (newValue) => {
  if (newValue) {
    initializePayment();
  }
});

// Watch for changes in the date
watch(paymentDate, (newValue) => {
  if (newValue) {
    // Update the ISO formatted date in payment
    payment.value.paymentDate = adapter.toISO(newValue);
  }
});

// Initialize component
onMounted(() => {
  initializePayment();
});

// Set payment date to today
function setPaymentDateToday() {
  paymentDate.value = adapter.date(new Date());
}

// Initialize payment data
function initializePayment() {
  // If editing an existing payment
  if (editingPayment.value) {
    payment.value = {
      id: editingPayment.value.id,
      amount: editingPayment.value.amount,
      paymentDate: editingPayment.value.paymentDate, // Keep the original ISO string
      paymentType: editingPayment.value.paymentType,
      comment: editingPayment.value.comment
    };
    
    // Parse the ISO date for the date picker
    if (editingPayment.value.paymentDate) {
      paymentDate.value = adapter.parseISO(editingPayment.value.paymentDate);
    } else {
      paymentDate.value = adapter.date(new Date());
    }
  } else {
    // Set today's date
    paymentDate.value = adapter.date(new Date());
    
    // New payment
    payment.value = {
      amount: null,
      paymentDate: adapter.toISO(paymentDate.value),
      paymentType: 'BANKGIRO',
      comment: ''
    };
    
    // Default to remaining amount
    if (invoice.value) {
      if (invoice.value.status === 'PARTIALLY_PAID') {
        payment.value.amount = getRemainingAmount.value;
      } else {
        payment.value.amount = invoice.value.amount;
      }
    }
  }
}

// Close dialog
function closeDialog() {
  if (paymentForm.value) {
    paymentForm.value.reset();
  }
  
  initializePayment();
  emit('update:visible', false);
  emit('close');
}

// Save payment
async function savePayment() {
  if (paymentForm.value && !paymentForm.value.validate()) return;
  
  savingPayment.value = true;
  
  try {
    const paymentData = {
      amount: parseFloat(payment.value.amount),
      paymentDate: payment.value.paymentDate, // Should be in ISO format
      paymentType: payment.value.paymentType,
      comment: payment.value.comment
    };
    
    let response;
    
    // Update existing payment or create new one
    if (editingPayment.value) {
      response = await invoiceService.updatePayment(editingPayment.value.id, paymentData);
      emit('payment-updated', response.data);
    } else {
      response = await invoiceService.registerPayment(invoice.value.id, paymentData);
      emit('payment-created', response.data);
    }
    
    closeDialog();
  } catch (error) {
    console.error('Error saving payment:', error);
    emit('error', error);
  } finally {
    savingPayment.value = false;
  }
}

// Format currency for display
function formatCurrency(value) {
  if (value === null || value === undefined) return '0 kr';
  
  return new Intl.NumberFormat('sv-SE', {
    style: 'currency',
    currency: 'SEK',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(value);
}
</script>

<style scoped>
.pa-golden {
  padding: 16px;
}

.dialog-title {
  background-color: #f5f5f5;
}

/* Custom styling for the date input to reinforce ISO format */
:deep(.v-field__input) {
  font-family: monospace; /* Helps align the dates better */
}

/* Styling for the placeholder to make the format more obvious */
:deep(.v-field__input::placeholder) {
  opacity: 0.7;
  letter-spacing: 0.5px;
}
</style>
