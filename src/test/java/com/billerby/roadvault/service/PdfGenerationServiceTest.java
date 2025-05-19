package com.billerby.roadvault.service;

import com.billerby.roadvault.dto.AssociationDTO;
import com.billerby.roadvault.dto.BillingDTO;
import com.billerby.roadvault.dto.InvoiceDTO;
import com.billerby.roadvault.dto.OwnerDTO;
import freemarker.template.Configuration;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PdfGenerationServiceTest {

    @Mock
    private InvoiceService invoiceService;
    
    @Mock
    private AssociationService associationService;
    
    @Spy
    private Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
    
    @InjectMocks
    private PdfGenerationService pdfGenerationService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() throws Exception {
        // Configure FreeMarker to use the classpath for templates
        freemarkerConfig.setClassLoaderForTemplateLoading(
            Thread.currentThread().getContextClassLoader(), "templates");
        freemarkerConfig.setDefaultEncoding("UTF-8");
        
        // Setup FopFactory using the non-deprecated approach
        FopFactoryBuilder builder = new FopFactoryBuilder(tempDir.toUri());
        FopFactory fopFactory = builder.build();
        ReflectionTestUtils.setField(pdfGenerationService, "fopFactory", fopFactory);
        
        // Set up mock responses
        InvoiceDTO invoiceDTO = createMockInvoiceDTO();
        BillingDTO billingDTO = createMockBillingDTO();
        AssociationDTO associationDTO = createMockAssociationDTO();
        OwnerDTO ownerDTO = createMockOwnerDTO();
        
        when(invoiceService.getInvoiceDTOById(anyLong())).thenReturn(invoiceDTO);
        when(invoiceService.getBillingForInvoice(anyLong())).thenReturn(billingDTO);
        when(invoiceService.getOwnerForInvoice(anyLong())).thenReturn(ownerDTO);
        when(associationService.getAssociation()).thenReturn(associationDTO);
    }
    
    @Test
    public void testGenerateAndSavePdf() throws Exception {
        // Generate the PDF using the real template
        byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(1L);
        
        // Assert that we got some content
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF should have content");
        
        // Create directory for test output if it doesn't exist
        Path testOutputDir = Paths.get("target/test-output");
        if (!Files.exists(testOutputDir)) {
            Files.createDirectories(testOutputDir);
        }
        
        // Save the PDF to disk for visual inspection
        Path pdfFile = testOutputDir.resolve("test-invoice.pdf");
        try (FileOutputStream fos = new FileOutputStream(pdfFile.toFile())) {
            fos.write(pdfBytes);
        }
        
        System.out.println("PDF saved to: " + pdfFile.toAbsolutePath());
        
        // Verify the file exists
        File savedFile = pdfFile.toFile();
        assertTrue(savedFile.exists(), "PDF file should exist");
        assertTrue(savedFile.length() > 0, "PDF file should have content");
        
        // No need to verify template methods since we're using the real template
        // Just verify the service methods were called
        verify(invoiceService).getInvoiceDTOById(1L);
        verify(invoiceService).getBillingForInvoice(1L);
        verify(invoiceService).getOwnerForInvoice(1L);
        verify(associationService).getAssociation();
    }
    
    // Helper methods to create mock DTOs
    
    private InvoiceDTO createMockInvoiceDTO() {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(1L);
        dto.setBillingId(1L);
        dto.setBillingDescription("Årsavgift 2025");
        dto.setPropertyId(1L);
        dto.setPropertyDesignation("Mjörn Apelgården 1:15");
        dto.setOwnerName("Arne Jørgen Olafsenn");
        dto.setAmount(new BigDecimal("1416.00"));
        dto.setDueDate(LocalDate.of(2025, 6, 18));
        dto.setInvoiceNumber("2025-2");
        dto.setOcrNumber("250002029");
        dto.setStatus("CREATED");
        return dto;
    }
    
    private BillingDTO createMockBillingDTO() {
        BillingDTO dto = new BillingDTO();
        dto.setId(1L);
        dto.setYear(2025);
        dto.setDescription("Årsavgift 2025");
        dto.setTotalAmount(new BigDecimal("43750.00"));
        dto.setIssueDate(LocalDate.of(2025, 5, 1));
        dto.setDueDate(LocalDate.of(2025, 6, 30));
        dto.setType("ANNUAL_FEE");
        dto.setInvoiceCount(35);
        return dto;
    }
    
    private AssociationDTO createMockAssociationDTO() {
        AssociationDTO dto = new AssociationDTO();
        dto.setId(1L);
        dto.setName("Apelgårdens vägsamfällighet");
        dto.setOrganizationNumber("716408-8002");
        dto.setBankgiroNumber(null);
        dto.setPlusgiroNumber("76454-8");
        dto.setEmail("charlotte.hemvik@outlook.com");
        dto.setPhone("0707-773010");
        dto.setWebsite("https://apelgarden.billerby.com");
        dto.setInvoiceText("Betala senast på förfallodagen. Ange OCR-nummer vid betalning.");
        dto.setReminderDays(14);
        dto.setReminderFee(new BigDecimal("50.00"));
        return dto;
    }
    
    private OwnerDTO createMockOwnerDTO() {
        OwnerDTO dto = new OwnerDTO();
        dto.setId(1L);
        dto.setName("Arne Jørgen Olafsenv");
        dto.setEmail("ajolafsen@gmail.com");
        return dto;
    }
}
