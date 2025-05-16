package com.billerby.roadvault.service;

import org.springframework.stereotype.Service;

/**
 * Service for generating OCR numbers according to Swedish OCR standard.
 */
@Service
public class OcrService {

    /**
     * Generates an OCR reference number for an invoice.
     *
     * @param year       The billing year (last two digits will be used)
     * @param propertyId The ID of the property
     * @param invoiceNumber The invoice number in the sequence
     * @return A complete OCR number with check digit
     */
    public String generateOcr(int year, Long propertyId, int invoiceNumber) {
        // Format reference number: YY (year) + PPPP (property ID) + NN (invoice number)
        String yearPart = String.format("%02d", year % 100); // Take only last two digits of the year
        String propertyPart = String.format("%04d", propertyId); // Property ID with leading zeros
        String invoicePart = String.format("%02d", invoiceNumber); // Invoice number with leading zeros
        
        String reference = yearPart + propertyPart + invoicePart;
        
        // Calculate check digit
        int checkDigit = calculateCheckDigit(reference);
        
        // Return complete OCR number
        return reference + checkDigit;
    }
    
    /**
     * Calculates the check digit for an OCR reference number using the modulus 10 method.
     *
     * @param number The reference number to calculate the check digit for
     * @return The check digit (0-9)
     */
    public int calculateCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;
        
        // Process digits from right to left
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(number.charAt(i));
            
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            
            sum += n;
            alternate = !alternate;
        }
        
        // Calculate check digit: (10 - (sum % 10)) % 10
        return (10 - (sum % 10)) % 10;
    }
    
    /**
     * Validates an OCR number.
     *
     * @param ocrNumber The complete OCR number including check digit
     * @return true if the OCR number is valid, false otherwise
     */
    public boolean validateOcr(String ocrNumber) {
        if (ocrNumber == null || ocrNumber.length() < 2) {
            return false;
        }
        
        String referenceNumber = ocrNumber.substring(0, ocrNumber.length() - 1);
        int providedCheckDigit = Character.getNumericValue(ocrNumber.charAt(ocrNumber.length() - 1));
        int calculatedCheckDigit = calculateCheckDigit(referenceNumber);
        
        return providedCheckDigit == calculatedCheckDigit;
    }
    
    /**
     * Extracts year from an OCR number.
     *
     * @param ocrNumber The complete OCR number
     * @return The extracted year (2-digit)
     */
    public String extractYearFromOcr(String ocrNumber) {
        if (ocrNumber == null || ocrNumber.length() < 2) {
            return "";
        }
        
        return ocrNumber.substring(0, 2);
    }
    
    /**
     * Extracts property ID from an OCR number.
     *
     * @param ocrNumber The complete OCR number
     * @return The extracted property ID
     */
    public Long extractPropertyIdFromOcr(String ocrNumber) {
        if (ocrNumber == null || ocrNumber.length() < 6) {
            return null;
        }
        
        return Long.parseLong(ocrNumber.substring(2, 6));
    }
    
    /**
     * Extracts invoice number from an OCR number.
     *
     * @param ocrNumber The complete OCR number
     * @return The extracted invoice number
     */
    public Integer extractInvoiceNumberFromOcr(String ocrNumber) {
        if (ocrNumber == null || ocrNumber.length() < 8) {
            return null;
        }
        
        return Integer.parseInt(ocrNumber.substring(6, 8));
    }
}
