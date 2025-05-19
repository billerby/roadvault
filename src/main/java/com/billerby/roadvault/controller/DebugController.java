package com.billerby.roadvault.controller;

import com.billerby.roadvault.config.BathTemperatureConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Temporary controller for debugging TTN API responses.
 * This will help us understand the actual structure of the API response.
 */
@RestController
@RequestMapping("/api/v1/debug")
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);
    private final BathTemperatureConfig bathTemperatureConfig;
    private final RestTemplate restTemplate;

    @Autowired
    public DebugController(BathTemperatureConfig bathTemperatureConfig) {
        this.bathTemperatureConfig = bathTemperatureConfig;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Debug endpoint to fetch and display the raw TTN API response.
     * This will help us understand the JSON structure.
     * 
     * @return Raw JSON response from TTN API
     */
    @GetMapping("/ttn-api")
    @PreAuthorize("hasRole('ADMIN')")
    public String debugTtnApi() {
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
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("API Response: {}", response.getBody());
                return response.getBody();
            } else {
                logger.warn("Failed to get temperature data. Status: {}", response.getStatusCode());
                return "Failed to get data: " + response.getStatusCode();
            }
        } catch (Exception e) {
            logger.error("Error fetching TTN API data", e);
            return "Error: " + e.getMessage();
        }
    }
}
