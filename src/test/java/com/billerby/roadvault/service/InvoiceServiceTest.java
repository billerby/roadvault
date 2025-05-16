package com.billerby.roadvault.service;

import com.billerby.roadvault.model.Billing;
import com.billerby.roadvault.model.Invoice;
import com.billerby.roadvault.model.Owner;
import com.billerby.roadvault.model.Property;
import com.billerby.roadvault.repository.BillingRepository;
import com.billerby.roadvault.repository.InvoiceRepository;
import com.billerby.roadvault.repository.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test class for invoice calculations based on Excel formula:
 * Årsavgift = (Delaktighetstal / Summa delaktighetstal) * Total belopp att utdebitera
 */
public class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private BillingRepository billingRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private OcrService ocrService;

    @InjectMocks
    private InvoiceService invoiceService;

    // Test data
    private List<Property> testProperties;
    private Billing testBilling;
    private BigDecimal totalShareRatio;
    private Map<String, BigDecimal> expectedAmounts; // Property designation -> Expected Amount
    private static final BigDecimal TOTAL_AMOUNT = new BigDecimal("17000");
    private static final BigDecimal TOTAL_SHARE_RATIO = new BigDecimal("101.710");

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up test data based on the Excel example
        setupTestData();

        // Set up mocks
        when(propertyRepository.findAll()).thenReturn(testProperties);
        when(billingRepository.findById(1L)).thenReturn(Optional.of(testBilling));
        when(invoiceRepository.findMaxInvoiceNumberByYear(any())).thenReturn(0);
        when(ocrService.generateOcr(any(Integer.class), any(Long.class), any(Integer.class))).thenReturn("123456789");
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));
    }

    /**
     * Test the calculation logic for individual properties using the Excel formula.
     */
    @Test
    public void testCalculationMatchesExcelFormula() {
        // For each property in our test data
        for (Property property : testProperties) {
            // Get expected amount from our map
            BigDecimal expected = expectedAmounts.get(property.getPropertyDesignation());
            
            // Calculate using Excel formula: (shareRatio / totalShareRatio) * totalAmount
            BigDecimal calculatedExcel = property.getShareRatio()
                    .divide(TOTAL_SHARE_RATIO, 10, RoundingMode.HALF_UP)
                    .multiply(TOTAL_AMOUNT)
                    .setScale(0, RoundingMode.HALF_UP);
            
            // Check if Excel formula calculation is close to the expected amount (allow 1 kr difference)
            BigDecimal diff = expected.subtract(calculatedExcel).abs();
            assertTrue(diff.compareTo(new BigDecimal("1")) <= 0, 
                    String.format("Excel formula for property %s with ratio %s gives %s, expected %s (diff: %s)", 
                            property.getPropertyDesignation(), property.getShareRatio(), 
                            calculatedExcel, expected, diff));
            
            // Calculate using our service
            BigDecimal calculatedService = invoiceService.calculateInvoiceAmount(
                    TOTAL_AMOUNT,
                    property.getShareRatio(),
                    TOTAL_SHARE_RATIO,
                    null // No extra charge in the Excel example
            );
            
            // Round to nearest integer for comparison
            calculatedService = calculatedService.setScale(0, RoundingMode.HALF_UP);
            
            // Check if our service calculation is close to the expected amount (allow 1 kr difference)
            diff = expected.subtract(calculatedService).abs();
            assertTrue(diff.compareTo(new BigDecimal("1")) <= 0, 
                    String.format("Service calculation for property %s gives %s, expected %s (diff: %s)", 
                            property.getPropertyDesignation(), calculatedService, expected, diff));
        }
    }

    /**
     * Test the generation of invoices for all properties.
     */
    @Test
    public void testGenerateInvoicesForBilling() {
        // Act
        List<Invoice> invoices = invoiceService.generateInvoicesForBilling(1L);
        
        // Assert
        assertEquals(testProperties.size(), invoices.size(), "Should generate one invoice per property");
        
        // Check if total amount adds up (within small rounding margin)
        BigDecimal totalInvoiced = invoices.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal diff = testBilling.getTotalAmount().subtract(totalInvoiced).abs();
        assertTrue(diff.compareTo(new BigDecimal("5")) < 0, 
                "Total invoiced amount should approximately match billing amount");
    }

    /**
     * Test specific property calculations to match Excel data.
     */
    @Test
    public void testSpecificPropertyCalculations() {
        // Test a few specific properties with expected values

        // Test Liljeros (1:5) with share ratio 0.593
        testExcelCalculation("1:5", new BigDecimal("0.593"), new BigDecimal("99"));
        
        // Test Hemvik (1:18) with share ratio 2.732
        testExcelCalculation("1:18", new BigDecimal("2.732"), new BigDecimal("457"));
        
        // Test Billerby (1:40) with share ratio 3.640
        testExcelCalculation("1:40", new BigDecimal("3.640"), new BigDecimal("608"));
    }

    /**
     * Helper method to test a specific property's Excel calculation formula.
     */
    private void testExcelCalculation(String designation, BigDecimal shareRatio, BigDecimal expected) {
        // Calculate using Excel formula: (shareRatio / totalShareRatio) * totalAmount
        BigDecimal calculatedExcel = shareRatio
                .divide(TOTAL_SHARE_RATIO, 10, RoundingMode.HALF_UP)
                .multiply(TOTAL_AMOUNT)
                .setScale(0, RoundingMode.HALF_UP);
        
        // Allow 1 kr difference due to rounding
        BigDecimal diff = expected.subtract(calculatedExcel).abs();
        assertTrue(diff.compareTo(new BigDecimal("1")) <= 0, 
                String.format("Excel formula for property %s with ratio %s gives %s, expected %s (diff: %s)", 
                        designation, shareRatio, calculatedExcel, expected, diff));
    }

    /**
     * Set up test data from the Excel example.
     */
    private void setupTestData() {
        // Create billing
        testBilling = new Billing();
        testBilling.setId(1L);
        testBilling.setYear(2023);
        testBilling.setDescription("Utdebitering av ordinarie årsavgift 2023");
        testBilling.setTotalAmount(TOTAL_AMOUNT);
        testBilling.setIssueDate(LocalDate.of(2023, 8, 1));
        testBilling.setDueDate(LocalDate.of(2023, 8, 31));
        testBilling.setType(Billing.BillingType.ANNUAL_FEE);

        // Create property list and expected amounts
        testProperties = new ArrayList<>();
        expectedAmounts = new HashMap<>();
        totalShareRatio = BigDecimal.ZERO;

        // Add all properties from Excel data with delaktighetstal, not fastighetsbeteckning
        addProperty("1:5", "R Liljeros", "0.593", "99");
        addProperty("1:15", "AJ Olafsen", "1.600", "267");
        addProperty("1:18", "L Hemvik", "2.732", "457");
        addProperty("1:21", "L&C Mangelus", "3.204", "536");
        addProperty("1:51", "P&T Johansson", "0.593", "99");
        addProperty("1:28", "A Collin", "3.640", "608");
        addProperty("1:32", "M. Fors", "3.640", "608");
        addProperty("1:35", "C Hellström", "3.640", "608");
        addProperty("1:50", "P Arnell", "1.355", "227");
        addProperty("1:16", "E Holmberg", "1.355", "227");
        addProperty("1:19", "K Malm Tronstad", "1.355", "227");
        addProperty("1:22", "B Wastler", "3.640", "608");
        addProperty("1:29", "L Lefdal", "3.640", "608");
        addProperty("1:33", "I Olemyr", "3.640", "608");
        addProperty("1:36", "J Lundberg", "3.640", "608");
        addProperty("1:14", "Palm", "1.355", "227");
        addProperty("1:17", "L Bruce", "1.355", "227");
        addProperty("1:20", "G Mellgren", "3.640", "608");
        addProperty("1:24", "B Enlund", "0.593", "99");
        addProperty("1:30", "K Månsson", "3.640", "608");
        addProperty("1:34", "B Liljekvist", "3.640", "608");
        addProperty("1:37", "A Östberg", "3.640", "608");
        addProperty("1:38", "P Thorén", "3.640", "608");
        addProperty("1:40", "E Billerby", "3.640", "608");
        addProperty("1:41", "J Bengtsson", "3.640", "608");
        addProperty("1:44", "B Fak", "3.640", "608");
        addProperty("1:47", "P Eskilson", "3.640", "608");
        addProperty("1:39", "S Walldal", "3.640", "608");
        addProperty("1:42", "P Wemminger", "3.640", "608");
        addProperty("1:45", "P Svending", "3.640", "608");
        addProperty("1:52", "I Eriksson", "1.888", "316");
        addProperty("1:43", "M Törnbom", "3.640", "608");
        addProperty("1:46", "E Norberg", "3.640", "608");
        addProperty("1:27", "Bronäs", "3.640", "608");
        addProperty("1:26", "A Drefeldt", "3.640", "608");
    }

    /**
     * Helper method to add a property to the test data.
     */
    private void addProperty(String designation, String ownerName, String shareRatio, String expectedAmount) {
        long id = testProperties.size() + 1L;
        
        Owner owner = new Owner();
        owner.setId(id);
        owner.setName(ownerName);
        
        Property property = new Property();
        property.setId(id);
        property.setPropertyDesignation(designation);
        property.setShareRatio(new BigDecimal(shareRatio));
        property.setOwner(owner);
        
        testProperties.add(property);
        expectedAmounts.put(designation, new BigDecimal(expectedAmount));
        
        // Update total share ratio
        totalShareRatio = totalShareRatio.add(property.getShareRatio());
    }
}
