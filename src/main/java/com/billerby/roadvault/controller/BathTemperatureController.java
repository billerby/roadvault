package com.billerby.roadvault.controller;

import com.billerby.roadvault.dto.BathTemperatureDto;
import com.billerby.roadvault.dto.TtnWebhookDto;
import com.billerby.roadvault.service.BathTemperatureService;
import com.billerby.roadvault.service.SimpleBathTemperaturePollingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public BathTemperatureController(
            BathTemperatureService bathTemperatureService,
            SimpleBathTemperaturePollingService simpleBathTemperaturePollingService) {
        this.bathTemperatureService = bathTemperatureService;
        this.simpleBathTemperaturePollingService = simpleBathTemperaturePollingService;
    }

    /**
     * Webhook endpoint for The Things Network to send temperature data
     * 
     * @param webhookDto The webhook payload
     * @return ResponseEntity with the created temperature record
     */
    @PostMapping("/webhook")
    public ResponseEntity<BathTemperatureDto> processWebhook(@RequestBody TtnWebhookDto webhookDto) {
        logger.info("Received webhook from TTN");
        BathTemperatureDto result = bathTemperatureService.processWebhook(webhookDto);
        
        if (result == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        return ResponseEntity.ok(result);
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
