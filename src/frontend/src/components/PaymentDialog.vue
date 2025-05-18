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
                v-model="payment.paymentDate"
                label="Betalningsdatum"
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

<script>
import { defineComponent } from 'vue';
import { format } from 'date-fns';
import invoiceService from '../services/invoice.service';
import { VDateInput } from 'vuetify/labs/VDateInput'

export default defineComponent({
  name: 'PaymentDialog',
  components: {
    VDateInput
  },
  
  props: {
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
  },
  
  data() {
    return {
      // Payment form
      validPaymentForm: true,
      savingPayment: false,
      
      // Payment data
      payment: this.getDefaultPayment(),
      
      // Payment types
      paymentTypes: [
        { value: 'BANKGIRO', label: 'Bankgiro' },
        { value: 'POSTGIRO', label: 'Postgiro' },
        { value: 'SWISH', label: 'Swish' },
        { value: 'MANUAL', label: 'Manuell' },
        { value: 'OTHER', label: 'Annan' }
      ]
    };
  },
  
  computed: {
    // Control dialog visibility with prop
    dialogVisible: {
      get() {
        return this.visible;
      },
      set(value) {
        if (!value) {
          this.$emit('update:visible', false);
        }
      }
    },
    
    // Calculate remaining amount to be paid for this invoice
    getRemainingAmount() {
      if (!this.invoice) return 0;
      
      const amount = parseFloat(this.invoice.amount) || 0;
      const paidAmount = this.paidAmount;
      
      // If editing a payment, exclude it from the paid amount
      if (this.editingPayment) {
        const editingAmount = parseFloat(this.editingPayment.amount) || 0;
        return amount - paidAmount + editingAmount;
      }
      
      return amount - paidAmount;
    },
    
    // Calculate remaining amount after this payment
    remainingAfterPayment() {
      if (!this.invoice || !this.payment.amount) return 0;
      
      const currentPaidAmount = this.paidAmount;
      const paymentAmount = parseFloat(this.payment.amount) || 0;
      
      // If editing a payment, exclude it from the paid amount
      if (this.editingPayment) {
        const editingAmount = parseFloat(this.editingPayment.amount) || 0;
        return this.invoice.amount - (currentPaidAmount - editingAmount + paymentAmount);
      }
      
      return this.invoice.amount - (currentPaidAmount + paymentAmount);
    }
  },
  
  watch: {
    // Update payment data when visible changes
    visible(newValue) {
      if (newValue) {
        this.initializePayment();
      }
    },
    
    // Update payment data when editing payment changes
    editingPayment(newValue) {
      if (newValue) {
        this.initializePayment();
      }
    }
  },
  
  mounted() {
    this.initializePayment();
  },
  
  methods: {
    // Get default payment data
    getDefaultPayment() {
      // Use ISO format YYYY-MM-DD which is compatible with v-date-input
      return {
        amount: null,
        paymentDate: new Date().toISOString().split('T')[0], 
        paymentType: 'BANKGIRO',
        comment: ''
      };
    },
    
    // Set payment date to today
    setPaymentDateToday() {
      this.payment.paymentDate = new Date().toISOString().split('T')[0];
    },
    
    // Initialize payment data
    initializePayment() {
      // If editing an existing payment
      if (this.editingPayment) {
        this.payment = {
          id: this.editingPayment.id,
          amount: this.editingPayment.amount,
          paymentDate: this.editingPayment.paymentDate,
          paymentType: this.editingPayment.paymentType,
          comment: this.editingPayment.comment
        };
      } else {
        // New payment
        this.payment = this.getDefaultPayment();
        
        // Default to remaining amount
        if (this.invoice) {
          if (this.invoice.status === 'PARTIALLY_PAID') {
            this.payment.amount = this.getRemainingAmount;
          } else {
            this.payment.amount = this.invoice.amount;
          }
        }
      }
    },
    
    // Close dialog
    closeDialog() {
      this.$refs.paymentForm.reset();
      this.payment = this.getDefaultPayment();
      this.$emit('update:visible', false);
      this.$emit('close');
    },
    
    // Save payment
    async savePayment() {
      if (!this.$refs.paymentForm.validate()) return;
      
      this.savingPayment = true;
      
      try {
        const paymentData = {
          amount: parseFloat(this.payment.amount),
          paymentDate: this.payment.paymentDate,
          paymentType: this.payment.paymentType,
          comment: this.payment.comment
        };
        
        let response;
        
        // Update existing payment or create new one
        if (this.editingPayment) {
          response = await invoiceService.updatePayment(this.editingPayment.id, paymentData);
          this.$emit('payment-updated', response.data);
        } else {
          response = await invoiceService.registerPayment(this.invoice.id, paymentData);
          this.$emit('payment-created', response.data);
        }
        
        this.closeDialog();
      } catch (error) {
        console.error('Error saving payment:', error);
        this.$emit('error', error);
      } finally {
        this.savingPayment = false;
      }
    },
    
    // Format currency for display
    formatCurrency(value) {
      if (value === null || value === undefined) return '0 kr';
      
      return new Intl.NumberFormat('sv-SE', {
        style: 'currency',
        currency: 'SEK',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }).format(value);
    }
  }
});
</script>

<style scoped>
.pa-golden {
  padding: 16px;
}

.dialog-title {
  background-color: #f5f5f5;
}
</style>
