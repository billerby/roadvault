package com.billerby.roadvault.controller;

import com.billerby.roadvault.dto.BathTemperatureDto;
import com.billerby.roadvault.service.BathTemperatureService;
import com.billerby.roadvault.service.SimpleBathTemperaturePollingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST controller for bath temperature operations.
 */
@RestController
@RequestMapping("/api/v1/bath-temperatures")
public class BathTemperatureController {

    private static final Logger logger = LoggerFactory.getLogger(BathTemperatureController.class);
    private final BathTemperatureService bathTemperatureService;
    private final SimpleBathTemperaturePollingService simpleBathTemperaturePollingService;
    
    @Value("${roadvault.webhook.apikey}")
    private String webhookApiKey;

    @Autowired
    public BathTemperatureController(
            BathTemperatureService bathTemperatureService,
            SimpleBathTemperaturePollingService simpleBathTemperaturePollingService) {
        this.bathTemperatureService = bathTemperatureService;
        this.simpleBathTemperaturePollingService = simpleBathTemperaturePollingService;
    }

    /**
     * Webhook endpoint for The Things Network to send temperature data.
     * Simple API key validation for basic security.
     * 
     * @param rawPayload The raw JSON payload from TTN webhook
     * @param apiKey The API key from header for authentication
     * @return ResponseEntity with the processing result
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> processWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Downlink-Apikey", required = false) String apiKey) {
        
        logger.info("Received webhook from TTN");
        
        // Simple API key validation
        if (apiKey == null || !webhookApiKey.equals(apiKey)) {
            logger.warn("Invalid or missing API key in webhook request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Invalid or missing API key"
            ));
        }
        
        try {
            // Use the same processing logic as the polling service
            simpleBathTemperaturePollingService.processWebhookPayload(rawPayload);
            
            logger.info("Webhook processed successfully");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Webhook processed successfully"
            ));
        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", "Error processing webhook: " + e.getMessage()
            ));
        }
    }

    /**
     * Test endpoint for webhook functionality using actual TTN payload format.
     * This endpoint simulates a real webhook by accepting TTN-formatted JSON
     * and processing it through the webhook logic.
     * 
     * @param testPayload Optional test payload. If not provided, uses a sample payload.
     * @return ResponseEntity with the test result
     */
    @PostMapping("/webhook/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testWebhookWithSampleData(
            @RequestBody(required = false) String testPayload) {
        logger.info("Testing webhook functionality with sample TTN data");
        
        try {
            String payloadToTest;
            
            if (testPayload != null && !testPayload.trim().isEmpty()) {
                // Use provided payload
                payloadToTest = testPayload;
                logger.info("Using provided test payload for webhook test");
            } else {
                // Use a sample TTN webhook payload for testing
                payloadToTest = getSampleTtnWebhookPayload();
                logger.info("Using default sample payload for webhook test");
            }
            
            // Process the payload through the webhook logic
            simpleBathTemperaturePollingService.processWebhookPayload(payloadToTest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Webhook test completed successfully",
                "payloadSource", testPayload != null ? "provided" : "sample"
            ));
        } catch (Exception e) {
            logger.error("Error testing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error testing webhook: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get a sample TTN webhook payload for testing.
     * This mimics the structure that TTN would send to our webhook endpoint.
     * 
     * @return Sample JSON payload as string
     */
    private String getSampleTtnWebhookPayload() {
        return """
            {
              "result": {
                "end_device_ids": {
                  "device_id": "eui-a840411f8182f655",
                  "application_ids": {
                    "application_id": "badtemperatur-application"
                  },
                  "dev_eui": "A840411F8182F655",
                  "dev_addr": "260BD9A5"
                },
                "received_at": "%s",
                "uplink_message": {
                  "f_port": 2,
                  "f_cnt": 999999,
                  "frm_payload": "TEST_PAYLOAD",
                  "decoded_payload": {
                    "ADC_CH0V": 0.306,
                    "BatV": 3.359,
                    "Digital_IStatus": "L",
                    "Door_status": "OPEN",
                    "EXTI_Trigger": "FALSE",
                    "Hum_SHT": 6553.5,
                    "TempC1": 99.9,
                    "TempC_SHT": -0.1,
                    "Work_mode": "IIC"
                  },
                  "rx_metadata": [
                    {
                      "gateway_ids": {
                        "gateway_id": "test-gateway",
                        "eui": "TEST123456789ABC"
                      },
                      "time": "%s",
                      "timestamp": 4241189236,
                      "rssi": -114,
                      "channel_rssi": -114,
                      "snr": -6.8,
                      "location": {
                        "latitude": 58.0809480895067,
                        "longitude": 11.655348837375643,
                        "altitude": 12,
                        "source": "SOURCE_REGISTRY"
                      }
                    }
                  ],
                  "locations": {
                    "user": {
                      "latitude": 58.08212796179405,
                      "longitude": 11.65585845708847,
                      "source": "SOURCE_REGISTRY"
                    }
                  },
                  "last_battery_percentage": {
                    "f_cnt": 100201,
                    "value": 100,
                    "received_at": "%s"
                  }
                }
              }
            }
            """.formatted(
                java.time.Instant.now().toString(),
                java.time.Instant.now().toString(),
                java.time.Instant.now().toString()
            );
    }

    /**
     * Get all temperature records for a specific device
     * 
     * @param deviceId The device ID
     * @return List of BathTemperatureDto
     */
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<BathTemperatureDto>> getTemperaturesForDevice(@PathVariable String deviceId) {
        List<BathTemperatureDto> temperatures = bathTemperatureService.getTemperaturesForDevice(deviceId);
        return ResponseEntity.ok(temperatures);
    }
    
    /**
     * Manually trigger a temperature data poll from The Things Network.
     * This endpoint requires ADMIN role and is useful for testing or immediate updates.
     * 
     * @return Message indicating that the poll has been triggered
     */
    @PostMapping("/poll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> pollTemperatureData() {
        logger.info("Manually triggering temperature data poll");
        
        // Try the simple service first, as it's most likely to work
        try {
            simpleBathTemperaturePollingService.pollTemperatureData();
            return ResponseEntity.ok("Temperature data poll triggered successfully with simple service");
        } catch (Exception e) {
            logger.warn("Error using simple polling service: {}", e.getMessage());
            
            // Fall back to other services
            return pollWithAllServices();
        }
    }

    /**
     * Get temperature records for a specific time range
     * 
     * @param start Start date (ISO format)
     * @param end End date (ISO format)
     * @return List of BathTemperatureDto
     */
    @GetMapping("/time-range")
    public ResponseEntity<List<BathTemperatureDto>> getTemperaturesInTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        List<BathTemperatureDto> temperatures = bathTemperatureService.getTemperaturesInTimeRange(start, end);
        return ResponseEntity.ok(temperatures);
    }

    /**
     * Get the latest temperature for a specific device
     * 
     * @param deviceId The device ID
     * @return BathTemperatureDto
     */
    @GetMapping("/latest/device/{deviceId}")
    public ResponseEntity<BathTemperatureDto> getLatestTemperatureForDevice(@PathVariable String deviceId) {
        BathTemperatureDto temperature = bathTemperatureService.getLatestTemperatureForDevice(deviceId);
        
        if (temperature == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(temperature);
    }

    /**
     * Get the latest temperature regardless of device
     * 
     * @return BathTemperatureDto
     */
    @GetMapping("/latest")
    public ResponseEntity<BathTemperatureDto> getLatestTemperature() {
        BathTemperatureDto temperature = bathTemperatureService.getLatestTemperature();
        
        if (temperature == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(temperature);
    }

    /**
     * Get daily average temperatures for a specific device within a time range
     * 
     * @param deviceId The device ID
     * @param start Start date (ISO format)
     * @param end End date (ISO format)
     * @return Map of date string to average temperature
     */
    @GetMapping("/daily-averages/device/{deviceId}")
    public ResponseEntity<Map<String, Double>> getDailyAverageTemperatures(
            @PathVariable String deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        Map<String, Double> averages = bathTemperatureService.getDailyAverageTemperatures(deviceId, start, end);
        return ResponseEntity.ok(averages);
    }

    /**
     * Get hourly average temperatures for a specific device within a time range
     * 
     * @param deviceId The device ID
     * @param start Start date (ISO format)
     * @param end End date (ISO format)
     * @return Map of hour string to average temperature
     */
    @GetMapping("/hourly-averages/device/{deviceId}")
    public ResponseEntity<Map<String, Double>> getHourlyAverageTemperatures(
            @PathVariable String deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        Map<String, Double> averages = bathTemperatureService.getHourlyAverageTemperatures(deviceId, start, end);
        return ResponseEntity.ok(averages);
    }
    
    /**
     * Manually trigger a temperature data poll from The Things Network.
     * This endpoint requires ADMIN role and is useful for testing or immediate updates.
     * Uses the simple implementation that directly maps to the TTN API structure.
     * 
     * @return Message indicating that the poll has been triggered
     */
    @PostMapping("/poll/simple")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> pollWithSimpleService() {
        logger.info("Manually triggering temperature data poll with simple service");
        
        try {
            simpleBathTemperaturePollingService.pollTemperatureData();
            return ResponseEntity.ok("Temperature data poll triggered successfully with simple service");
        } catch (Exception e) {
            logger.error("Error in simple polling: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error triggering temperature data poll: " + e.getMessage());
        }
    }
    
    /**
     * Get health status of the temperature polling service
     * 
     * @return Health status information
     */
    @GetMapping("/polling/health")
    public ResponseEntity<Map<String, Object>> getPollingHealth() {
        Map<String, Object> healthStatus = simpleBathTemperaturePollingService.getHealthStatus();
        return ResponseEntity.ok(healthStatus);
    }
    
    /**
     * Manually trigger a temperature data poll from The Things Network.
     * This endpoint tries all available polling services in sequence until one succeeds.
     * 
     * @return Message indicating that the poll has been triggered
     */
    @PostMapping("/poll/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> pollWithAllServices() {
        logger.info("Manually triggering temperature data poll with all services");

        StringBuilder resultBuilder = new StringBuilder();
        boolean anySuccess = false;

        // Try simple service first
        try {
            simpleBathTemperaturePollingService.pollTemperatureData();
            resultBuilder.append("Simple service: SUCCESS\n");
            anySuccess = true;
        } catch (Exception e) {
            logger.warn("Simple service failed: {}", e.getMessage());
            resultBuilder.append("Simple service: FAILED - ").append(e.getMessage()).append("\n");
        }


        if (anySuccess) {
            return ResponseEntity.ok("Temperature data poll results:\n" + resultBuilder.toString());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("All polling services failed:\n" + resultBuilder.toString());
        }
    }

}
