package com.billerby.roadvault.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data Transfer Object for bath temperature measurements.
 * Used for API requests/responses.
 */
public class BathTemperatureDto {
    private Long id;
    private String deviceId;
    private Double temperature;
    private Integer batteryPercentage;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal altitude;
    private Instant receivedAt;

    // Konstruktorer
    public BathTemperatureDto() {
    }

    public BathTemperatureDto(Long id, String deviceId, Double temperature, 
                             Integer batteryPercentage, BigDecimal latitude,
                             BigDecimal longitude, BigDecimal altitude, Instant receivedAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.batteryPercentage = batteryPercentage;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.receivedAt = receivedAt;
    }

    // Getters och setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(Integer batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getAltitude() {
        return altitude;
    }

    public void setAltitude(BigDecimal altitude) {
        this.altitude = altitude;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}
