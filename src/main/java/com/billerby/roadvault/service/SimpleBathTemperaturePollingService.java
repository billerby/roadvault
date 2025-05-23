package com.billerby.roadvault.service;

import com.billerby.roadvault.config.BathTemperatureConfig;
import com.billerby.roadvault.dto.SimpleTtnApiResponseDto;
import com.billerby.roadvault.model.BathTemperature;
import com.billerby.roadvault.repository.BathTemperatureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple implementation using the exact API structure.
 */
@Service
public class SimpleBathTemperaturePollingService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleBathTemperaturePollingService.class);
    
    private final BathTemperatureRepository bathTemperatureRepository;
    private final BathTemperatureConfig bathTemperatureConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // Health monitoring
    private final AtomicLong lastSuccessfulPoll = new AtomicLong(0);
    private final AtomicLong lastAttemptedPoll = new AtomicLong(0);
  
    @Autowired
    public SimpleBathTemperaturePollingService(
            BathTemperatureRepository bathTemperatureRepository,
            BathTemperatureConfig bathTemperatureConfig,
            @Qualifier("ttnRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.bathTemperatureRepository = bathTemperatureRepository;
        this.bathTemperatureConfig = bathTemperatureConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Poll for new temperature data asynchronously.
     * This method runs in a separate thread pool to prevent blocking the application.
     * Includes proper error handling and timeouts to prevent hanging.
     */
    @Async("scheduledTaskExecutor")
    @Scheduled(fixedRate = 15 * 60 * 1000) // 15 minutes in milliseconds
    public void pollTemperatureData() {
        lastAttemptedPoll.set(System.currentTimeMillis());
        logger.info("Polling for new temperature data from The Things Network using async implementation...");
        
        try {
            // Create headers with authorization
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", bathTemperatureConfig.getToken());
            
            // Create HTTP entity with headers
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Make API request
            ResponseEntity<String> response = restTemplate.exchange(
                    bathTemperatureConfig.getApiUrl(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            // Parse response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Log the response body for debugging at debug level
                logger.debug("API Response: {}", response.getBody());
                processApiResponse(response.getBody());
                lastSuccessfulPoll.set(System.currentTimeMillis());
            } else {
                logger.warn("Failed to get temperature data. Status: {}", response.getStatusCode());
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Network/timeout errors - these are expected occasionally
            logger.warn("TTN API network error (will retry in 15 minutes): {}", e.getMessage());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // HTTP client errors (4xx) - authentication, not found etc.
            logger.error("TTN API client error: {} - {}", e.getStatusCode(), e.getMessage());
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // HTTP server errors (5xx) - TTN service issues
            logger.warn("TTN API server error (will retry in 15 minutes): {} - {}", e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            // Any other unexpected errors
            logger.error("Unexpected error polling temperature data: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Synchronous version of pollTemperatureData for testing purposes.
     * This method does the same work as pollTemperatureData but without @Async annotation.
     */
    public void pollTemperatureDataSync() {
        logger.info("Polling for new temperature data from The Things Network (sync for testing)...");
        
        try {
            // Create headers with authorization
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", bathTemperatureConfig.getToken());
            
            // Create HTTP entity with headers
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Make API request
            ResponseEntity<String> response = restTemplate.exchange(
                    bathTemperatureConfig.getApiUrl(),
                    HttpMethod.GET,
                    entity,
                    String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Log the response body for debugging at debug level
                logger.debug("API Response: {}", response.getBody());
                processApiResponse(response.getBody());
            } else {
                logger.warn("Failed to get temperature data. Status: {}", response.getStatusCode());
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Network/timeout errors - these are expected occasionally
            logger.warn("TTN API network error (will retry in 15 minutes): {}", e.getMessage());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // HTTP client errors (4xx) - authentication, not found etc.
            logger.error("TTN API client error: {} - {}", e.getStatusCode(), e.getMessage());
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // HTTP server errors (5xx) - TTN service issues
            logger.warn("TTN API server error (will retry in 15 minutes): {} - {}", e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            // Any other unexpected errors
            logger.error("Unexpected error polling temperature data: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Process the API response and save temperature data to the database.
     * 
     * @param responseBody The response body from the API
     */
    private void processApiResponse(String responseBody) {
        try {
            // Parse JSON response
            SimpleTtnApiResponseDto apiResponse = objectMapper.readValue(responseBody, SimpleTtnApiResponseDto.class);
            
            // Check if we have a valid result
            if (apiResponse.getResult() == null) {
                logger.warn("No result found in API response");
                return;
            }
            
            SimpleTtnApiResponseDto.Result result = apiResponse.getResult();
            
            // Check for required data
            if (result.getEndDeviceIds() == null) {
                logger.warn("No end_device_ids found in result");
                return;
            }
            
            if (result.getUplinkMessage() == null) {
                logger.warn("No uplink_message found in result");
                return;
            }
            
            // Extract device ID
            String deviceId = result.getEndDeviceIds().getDeviceId();
            if (deviceId == null) {
                logger.warn("Device ID is null in the result");
                return;
            }
            
            logger.info("Processing temperature data for device: {}", deviceId);
            
            // Extract temperature from decoded payload
            Map<String, Object> decodedPayload = result.getUplinkMessage().getDecodedPayload();
            if (decodedPayload == null) {
                logger.warn("Decoded payload is null for device {}", deviceId);
                return;
            }
            
            Double temperature = null;
            
            // Try different temperature fields
            if (decodedPayload.containsKey("TempC1")) {
                temperature = parseDoubleValue(decodedPayload.get("TempC1"));
            } else if (decodedPayload.containsKey("TempC_SHT")) {
                temperature = parseDoubleValue(decodedPayload.get("TempC_SHT"));
            } else {
                logger.warn("No temperature field found in payload for device {}", deviceId);
                logger.debug("Available payload fields: {}", decodedPayload.keySet());
                return;
            }
            
            if (temperature == null) {
                logger.warn("Failed to parse temperature value for device {}", deviceId);
                return;
            }
            
            // Extract battery percentage
            Integer batteryPercentage = null;
            if (result.getUplinkMessage().getLastBatteryPercentage() != null) {
                batteryPercentage = result.getUplinkMessage().getLastBatteryPercentage().getValue();
            }
            
            // Extract location information
            java.math.BigDecimal latitude = null;
            java.math.BigDecimal longitude = null;
            java.math.BigDecimal altitude = null;
            
            // First try to get user-defined location (priority for static sensors)
            if (result.getUplinkMessage().getLocations() != null &&
                result.getUplinkMessage().getLocations().containsKey("user")) {
                SimpleTtnApiResponseDto.Location userLocation = result.getUplinkMessage().getLocations().get("user");
                if (userLocation != null) {
                    latitude = new java.math.BigDecimal(userLocation.getLatitude().toString());
                    longitude = new java.math.BigDecimal(userLocation.getLongitude().toString());
                    if (userLocation.getAltitude() != null) {
                        altitude = new java.math.BigDecimal(userLocation.getAltitude().toString());
                    }
                    logger.debug("Using user-defined location: lat={}, lon={}, alt={}", latitude, longitude, altitude);
                }
            }
            
            // If not found, fallback to gateway location from rxMetadata
            if ((latitude == null || longitude == null) && 
                result.getUplinkMessage().getRxMetadata() != null && 
                !result.getUplinkMessage().getRxMetadata().isEmpty()) {
                SimpleTtnApiResponseDto.RxMetadata metadata = result.getUplinkMessage().getRxMetadata().get(0);
                if (metadata.getLocation() != null) {
                    latitude = new java.math.BigDecimal(metadata.getLocation().getLatitude().toString());
                    longitude = new java.math.BigDecimal(metadata.getLocation().getLongitude().toString());
                    if (metadata.getLocation().getAltitude() != null) {
                        altitude = new java.math.BigDecimal(metadata.getLocation().getAltitude().toString());
                    }
                    logger.debug("Using gateway location: lat={}, lon={}, alt={}", latitude, longitude, altitude);
                }
            }
            
            // Parse received timestamp
            String receivedAtStr = result.getReceivedAt();
            if (receivedAtStr == null) {
                logger.warn("Received timestamp is null for device {}", deviceId);
                return;
            }
            
            Instant receivedAt;
            try {
                receivedAt = Instant.parse(receivedAtStr);
            } catch (Exception e) {
                logger.warn("Failed to parse received timestamp {} for device {}: {}", 
                           receivedAtStr, deviceId, e.getMessage());
                return;
            }
            
            // Check if we already have this measurement
            if (bathTemperatureRepository.existsByDeviceIdAndReceivedAt(deviceId, receivedAt)) {
                logger.info("Temperature measurement for {} at {} already exists, skipping.", deviceId, receivedAt);
                return;
            }
            
            // Store raw payload as JSON string
            String rawPayload = objectMapper.writeValueAsString(result);
            
            // Create and save the temperature record
            BathTemperature bathTemperature = new BathTemperature(
                deviceId, temperature, batteryPercentage, 
                latitude, longitude, altitude, receivedAt, rawPayload
            );
            
            BathTemperature savedTemperature = bathTemperatureRepository.save(bathTemperature);
            logger.info("Successfully saved temperature measurement: {} °C for device {} at {}", 
                       temperature, deviceId, receivedAt);
            
        } catch (Exception e) {
            logger.error("Error processing API response: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Safely parse a value to Double
     */
    private Double parseDoubleValue(Object value) {
        if (value == null) {
            return null;
        }
        
        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                return Double.parseDouble((String) value);
            } else {
                return Double.parseDouble(value.toString());
            }
        } catch (Exception e) {
            logger.warn("Failed to parse Double value from {}: {}", value, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get health status of the polling service
     * @return Map with health information
     */
    public Map<String, Object> getHealthStatus() {
        long currentTime = System.currentTimeMillis();
        long lastSuccessTime = lastSuccessfulPoll.get();
        long lastAttemptTime = lastAttemptedPoll.get();
        
        boolean isHealthy = (currentTime - lastSuccessTime) < (20 * 60 * 1000); // 20 minutes
        boolean isRunning = (currentTime - lastAttemptTime) < (20 * 60 * 1000); // 20 minutes
        
        return Map.of(
            "healthy", isHealthy,
            "running", isRunning,
            "lastSuccessfulPoll", lastSuccessTime > 0 ? Instant.ofEpochMilli(lastSuccessTime).toString() : "never",
            "lastAttemptedPoll", lastAttemptTime > 0 ? Instant.ofEpochMilli(lastAttemptTime).toString() : "never",
            "minutesSinceLastSuccess", lastSuccessTime > 0 ? (currentTime - lastSuccessTime) / (60 * 1000) : -1,
            "minutesSinceLastAttempt", lastAttemptTime > 0 ? (currentTime - lastAttemptTime) / (60 * 1000) : -1
        );
    }
}
