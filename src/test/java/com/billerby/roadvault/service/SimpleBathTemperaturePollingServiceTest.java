package com.billerby.roadvault.service;

import com.billerby.roadvault.config.BathTemperatureConfig;
import com.billerby.roadvault.model.BathTemperature;
import com.billerby.roadvault.repository.BathTemperatureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration test for SimpleBathTemperaturePollingService.
 */
public class SimpleBathTemperaturePollingServiceTest {

    @Mock
    private BathTemperatureRepository bathTemperatureRepository;
    
    @Mock
    private BathTemperatureConfig bathTemperatureConfig;
    
    @Mock
    private RestTemplate restTemplate;
    
    private SimpleBathTemperaturePollingService pollingService;
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        // Use a spy on the real ObjectMapper
        ObjectMapper spyObjectMapper = spy(objectMapper);
        
        // Initialize service with mocks
        pollingService = new SimpleBathTemperaturePollingService(
                bathTemperatureRepository,
                bathTemperatureConfig,
                spyObjectMapper);
        
        // Set the RestTemplate field in the service using reflection
        try {
            java.lang.reflect.Field restTemplateField = SimpleBathTemperaturePollingService.class.getDeclaredField("restTemplate");
            restTemplateField.setAccessible(true);
            restTemplateField.set(pollingService, restTemplate);
        } catch (Exception e) {
            fail("Failed to set RestTemplate field: " + e.getMessage());
        }
    }
    
    @Test
    public void testProcessingResponseFromJsonFile() throws IOException {
        // Arrange
        String jsonResponse = readTestResourceFile("ttn-response.json");
        
        // Mock the REST call
        when(bathTemperatureConfig.getApiUrl()).thenReturn("https://test-api-url");
        when(bathTemperatureConfig.getToken()).thenReturn("test-token");
        
        // Mock REST response
        ResponseEntity<String> mockResponse = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);
        
        // Mock repository to return false for exists check (to ensure save is called)
        when(bathTemperatureRepository.existsByDeviceIdAndReceivedAt(anyString(), any(Instant.class)))
                .thenReturn(false);
        
        // Mock repository save method
        when(bathTemperatureRepository.save(any(BathTemperature.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        pollingService.pollTemperatureData();
        
        // Assert
        // Verify REST call was made
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
        
        // Capture the BathTemperature object saved to repository
        ArgumentCaptor<BathTemperature> bathTempCaptor = ArgumentCaptor.forClass(BathTemperature.class);
        verify(bathTemperatureRepository).save(bathTempCaptor.capture());
        
        // Verify the captured BathTemperature has correct values
        BathTemperature savedBathTemp = bathTempCaptor.getValue();
        assertNotNull(savedBathTemp, "Saved BathTemperature should not be null");
        assertEquals("eui-a840411f8182f655", savedBathTemp.getDeviceId(), "Device ID should match");
        assertEquals(16.5, savedBathTemp.getTemperature(), 0.0001, "Temperature should match");
        assertEquals(100, savedBathTemp.getBatteryPercentage(), "Battery percentage should match");
        assertEquals(new java.math.BigDecimal("58.08212796179405"), savedBathTemp.getLatitude(), "Latitude should match");
        assertEquals(new java.math.BigDecimal("11.65585845708847"), savedBathTemp.getLongitude(), "Longitude should match");
    }
    
    /**
     * Helper method to read a test resource file into a string.
     */
    private String readTestResourceFile(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource(fileName);
        return Files.readString(Paths.get(resource.getURI()));
    }
}
