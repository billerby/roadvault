package com.billerby.roadvault.service;

import com.billerby.roadvault.dto.BathTemperatureDto;
import com.billerby.roadvault.dto.TtnWebhookDto;
import com.billerby.roadvault.model.BathTemperature;
import com.billerby.roadvault.repository.BathTemperatureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for handling bath temperature operations.
 */
@Service
public class BathTemperatureService {

    private static final Logger logger = LoggerFactory.getLogger(BathTemperatureService.class);
    private final BathTemperatureRepository bathTemperatureRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public BathTemperatureService(BathTemperatureRepository bathTemperatureRepository, 
                                 ObjectMapper objectMapper) {
        this.bathTemperatureRepository = bathTemperatureRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Process a webhook payload from The Things Network and save the temperature data.
     * 
     * @param webhookDto The webhook payload
     * @return The saved BathTemperatureDto
     */
    public BathTemperatureDto processWebhook(TtnWebhookDto webhookDto) {
        try {
            logger.info("Processing webhook from device: {}", 
                    webhookDto.getResult().getEndDeviceIds().getDeviceId());
            
            // Extract device ID
            String deviceId = webhookDto.getResult().getEndDeviceIds().getDeviceId();
            
            // Extract temperature from decoded payload
            Map<String, Object> decodedPayload = webhookDto.getResult().getUplinkMessage().getDecodedPayload();
            Double temperature = null;
            
            // The payload can contain temperature in different fields based on the sensor
            // First look for TempC1, then TempC_SHT if the first one isn't available
            if (decodedPayload.containsKey("TempC1")) {
                temperature = Double.valueOf(decodedPayload.get("TempC1").toString());
            } else if (decodedPayload.containsKey("TempC_SHT")) {
                temperature = Double.valueOf(decodedPayload.get("TempC_SHT").toString());
            } else {
                logger.warn("Temperature field not found in payload for device {}", deviceId);
                return null;
            }
            
            // Extract battery percentage
            Integer batteryPercentage = null;
            if (webhookDto.getResult().getUplinkMessage().getLastBatteryPercentage() != null) {
                batteryPercentage = webhookDto.getResult().getUplinkMessage().getLastBatteryPercentage().getValue();
            }
            
            // Extract location information
            Double latitude = null;
            Double longitude = null;
            Double altitude = null;
            
            // First try to get location from rxMetadata (gateway location)
            if (webhookDto.getResult().getUplinkMessage().getRxMetadata() != null && 
                !webhookDto.getResult().getUplinkMessage().getRxMetadata().isEmpty()) {
                TtnWebhookDto.RxMetadata metadata = webhookDto.getResult().getUplinkMessage().getRxMetadata().get(0);
                if (metadata.getLocation() != null) {
                    latitude = metadata.getLocation().getLatitude();
                    longitude = metadata.getLocation().getLongitude();
                    altitude = metadata.getLocation().getAltitude();
                }
            }
            
            // If not found, try to get from the locations object (user-defined location)
            if ((latitude == null || longitude == null) && 
                webhookDto.getResult().getUplinkMessage().getLocations() != null &&
                webhookDto.getResult().getUplinkMessage().getLocations().containsKey("user")) {
                TtnWebhookDto.Location userLocation = webhookDto.getResult().getUplinkMessage().getLocations().get("user");
                latitude = userLocation.getLatitude();
                longitude = userLocation.getLongitude();
                altitude = userLocation.getAltitude();
            }
            
            // Parse received timestamp
            Instant receivedAt = Instant.parse(webhookDto.getResult().getReceivedAt());
            
            // Store raw payload as JSON string
            String rawPayload = objectMapper.writeValueAsString(webhookDto);
            
            // Create and save the temperature record
            BathTemperature bathTemperature = new BathTemperature(
                deviceId, temperature, batteryPercentage, 
                latitude, longitude, altitude, receivedAt, rawPayload
            );
            
            BathTemperature savedTemperature = bathTemperatureRepository.save(bathTemperature);
            
            // Convert to DTO and return
            return convertToDto(savedTemperature);
            
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return null;
        }
    }
    
    /**
     * Get all temperature records for a specific device
     * 
     * @param deviceId The device ID
     * @return List of BathTemperatureDto
     */
    public List<BathTemperatureDto> getTemperaturesForDevice(String deviceId) {
        List<BathTemperature> temperatures = bathTemperatureRepository.findByDeviceIdOrderByReceivedAtDesc(deviceId);
        return temperatures.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all temperature records within a time range
     * 
     * @param startTime Start of time range
     * @param endTime End of time range
     * @return List of BathTemperatureDto
     */
    public List<BathTemperatureDto> getTemperaturesInTimeRange(Instant startTime, Instant endTime) {
        List<BathTemperature> temperatures = bathTemperatureRepository.findByReceivedAtBetweenOrderByReceivedAtDesc(
                startTime, endTime);
        return temperatures.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get the latest temperature record for a specific device
     * 
     * @param deviceId The device ID
     * @return BathTemperatureDto or null if not found
     */
    public BathTemperatureDto getLatestTemperatureForDevice(String deviceId) {
        BathTemperature temperature = bathTemperatureRepository.findFirstByDeviceIdOrderByReceivedAtDesc(deviceId);
        return temperature != null ? convertToDto(temperature) : null;
    }
    
    /**
     * Get the latest temperature record regardless of device
     * 
     * @return BathTemperatureDto or null if not found
     */
    public BathTemperatureDto getLatestTemperature() {
        BathTemperature temperature = bathTemperatureRepository.findFirstByOrderByReceivedAtDesc();
        return temperature != null ? convertToDto(temperature) : null;
    }
    
    /**
     * Get daily average temperatures for a specific device within a time range
     * 
     * @param deviceId The device ID
     * @param startTime Start of time range
     * @param endTime End of time range
     * @return Map of date string to average temperature
     */
    public Map<String, Double> getDailyAverageTemperatures(String deviceId, Instant startTime, Instant endTime) {
        List<Object[]> results = bathTemperatureRepository.getDailyAverageTemperatures(deviceId, startTime, endTime);
        Map<String, Double> averages = new HashMap<>();
        
        for (Object[] result : results) {
            String date = result[0].toString();
            Double avgTemp = Double.valueOf(result[1].toString());
            averages.put(date, avgTemp);
        }
        
        return averages;
    }
    
    /**
     * Get hourly average temperatures for a specific device within a time range
     * 
     * @param deviceId The device ID
     * @param startTime Start of time range
     * @param endTime End of time range
     * @return Map of hour string to average temperature
     */
    public Map<String, Double> getHourlyAverageTemperatures(String deviceId, Instant startTime, Instant endTime) {
        List<Object[]> results = bathTemperatureRepository.getHourlyAverageTemperatures(deviceId, startTime, endTime);
        Map<String, Double> averages = new HashMap<>();
        
        for (Object[] result : results) {
            String hour = result[0].toString();
            Double avgTemp = Double.valueOf(result[1].toString());
            averages.put(hour, avgTemp);
        }
        
        return averages;
    }
    
    /**
     * Convert a BathTemperature entity to a BathTemperatureDto
     * 
     * @param bathTemperature The entity to convert
     * @return The converted DTO
     */
    private BathTemperatureDto convertToDto(BathTemperature bathTemperature) {
        return new BathTemperatureDto(
            bathTemperature.getId(),
            bathTemperature.getDeviceId(),
            bathTemperature.getTemperature(),
            bathTemperature.getBatteryPercentage(),
            bathTemperature.getLatitude(),
            bathTemperature.getLongitude(),
            bathTemperature.getAltitude(),
            bathTemperature.getReceivedAt()
        );
    }
}
