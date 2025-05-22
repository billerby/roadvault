package com.billerby.roadvault.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for verifying the deserialization of TTN API responses.
 */
public class TtnApiResponseDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Test that the SimpleTtnApiResponseDto can correctly deserialize a real TTN API response.
     */
    @Test
    public void testSimpleTtnApiResponseDtoDeserialization() throws IOException {
        // Load the test JSON file
        String json = readTestResourceFile("ttn-response.json");
        
        // Deserialize to the DTO
        SimpleTtnApiResponseDto responseDto = objectMapper.readValue(json, SimpleTtnApiResponseDto.class);
        
        // Assert basic structure
        assertNotNull(responseDto, "Response DTO should not be null");
        assertNotNull(responseDto.getResult(), "Result should not be null");
        
        // Test end_device_ids
        SimpleTtnApiResponseDto.EndDeviceIds endDeviceIds = responseDto.getResult().getEndDeviceIds();
        assertNotNull(endDeviceIds, "EndDeviceIds should not be null");
        assertEquals("eui-a840411f8182f655", endDeviceIds.getDeviceId(), "Device ID should match");
        assertEquals("A840411F8182F655", endDeviceIds.getDevEui(), "DevEUI should match");
        
        // Test uplink_message
        SimpleTtnApiResponseDto.UplinkMessage uplinkMessage = responseDto.getResult().getUplinkMessage();
        assertNotNull(uplinkMessage, "UplinkMessage should not be null");
        assertEquals(2, uplinkMessage.getfPort(), "fPort should match");
        assertEquals(100086, uplinkMessage.getfCnt(), "fCnt should match");
        
        // Test decoded_payload
        assertNotNull(uplinkMessage.getDecodedPayload(), "DecodedPayload should not be null");
        assertTrue(uplinkMessage.getDecodedPayload().containsKey("TempC1"), "Decoded payload should contain TempC1");
        assertEquals(16.5, uplinkMessage.getDecodedPayload().get("TempC1"), "Temperature should match");
        
        // Test battery percentage
        assertNotNull(uplinkMessage.getLastBatteryPercentage(), "Last battery percentage should not be null");
        assertEquals(100, uplinkMessage.getLastBatteryPercentage().getValue(), "Battery percentage should match");
        
        // Test location
        assertNotNull(uplinkMessage.getLocations(), "Locations should not be null");
        assertTrue(uplinkMessage.getLocations().containsKey("user"), "Locations should contain 'user'");
        
        SimpleTtnApiResponseDto.Location userLocation = uplinkMessage.getLocations().get("user");
        assertNotNull(userLocation, "User location should not be null");
        assertEquals(58.08212796179405, userLocation.getLatitude(), 0.0001, "Latitude should match");
        assertEquals(11.65585845708847, userLocation.getLongitude(), 0.0001, "Longitude should match");
    }

    /**
     * Test our processing of TTN API response to extract bath temperature data.
     */
    @Test
    public void testProcessingTempDataFromApiResponse() throws IOException {
        // Load the test JSON file
        String json = readTestResourceFile("ttn-response.json");
        
        // Deserialize to the DTO
        SimpleTtnApiResponseDto responseDto = objectMapper.readValue(json, SimpleTtnApiResponseDto.class);
        
        // Extract the temperature data as our service would
        SimpleTtnApiResponseDto.Result result = responseDto.getResult();
        Map<String, Object> decodedPayload = result.getUplinkMessage().getDecodedPayload();
        
        // Verify we can extract the temperature correctly
        Double temperature = null;
        if (decodedPayload.containsKey("TempC1")) {
            Object tempObj = decodedPayload.get("TempC1");
            if (tempObj instanceof Number) {
                temperature = ((Number) tempObj).doubleValue();
            }
        }
        
        assertNotNull(temperature, "Temperature should be extracted");
        assertEquals(16.5, temperature, 0.0001, "Temperature should match expected value");
        
        // Verify we can extract battery percentage
        Integer batteryPercentage = result.getUplinkMessage().getLastBatteryPercentage().getValue();
        assertNotNull(batteryPercentage, "Battery percentage should be extracted");
        assertEquals(100, batteryPercentage, "Battery percentage should match expected value");
        
        // Verify location extraction
        Double latitude = result.getUplinkMessage().getLocations().get("user").getLatitude();
        Double longitude = result.getUplinkMessage().getLocations().get("user").getLongitude();
        
        assertNotNull(latitude, "Latitude should be extracted");
        assertNotNull(longitude, "Longitude should be extracted");
        assertEquals(58.08212796179405, latitude, 0.0001, "Latitude should match expected value");
        assertEquals(11.65585845708847, longitude, 0.0001, "Longitude should match expected value");
    }
    
    /**
     * Helper method to read a test resource file into a string.
     */
    private String readTestResourceFile(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource(fileName);
        return Files.readString(Paths.get(resource.getURI()));
    }
}
