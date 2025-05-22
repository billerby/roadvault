package com.billerby.roadvault.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Entity for storing bath temperature measurements from IoT devices.
 * This stores data sent from Dragino LSN50v2-D20 temperature sensors via LoRaWAN.
 */
@Entity
@Table(name = "bath_temperature", indexes = {
    @Index(name = "idx_bath_temperature_device_time", columnList = "device_id, received_at"),
    @Index(name = "idx_bath_temperature_received_at", columnList = "received_at"),
    @Index(name = "idx_bath_temperature_device_time_desc", columnList = "device_id, received_at DESC")
})
public class BathTemperature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "temperature", nullable = false)
    private Double temperature;

    @Column(name = "battery_percentage")
    private Integer batteryPercentage;

    @Column(name = "latitude", precision = 20, scale = 14)
    private java.math.BigDecimal latitude;

    @Column(name = "longitude", precision = 20, scale = 14)
    private java.math.BigDecimal longitude;

    @Column(name = "altitude", precision = 20, scale = 14)
    private java.math.BigDecimal altitude;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    // Konstruktorer
    public BathTemperature() {
    }

    public BathTemperature(String deviceId, Double temperature, Integer batteryPercentage, 
                           Double latitude, Double longitude, Double altitude, 
                           Instant receivedAt, String rawPayload) {
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.batteryPercentage = batteryPercentage;
        this.latitude = (latitude != null) ? new java.math.BigDecimal(latitude.toString()) : null;
        this.longitude = (longitude != null) ? new java.math.BigDecimal(longitude.toString()) : null;
        this.altitude = (altitude != null) ? new java.math.BigDecimal(altitude.toString()) : null;
        this.receivedAt = receivedAt;
        this.rawPayload = rawPayload;
    }

    // Constructor with BigDecimal parameters
    public BathTemperature(String deviceId, Double temperature, Integer batteryPercentage, 
                           java.math.BigDecimal latitude, java.math.BigDecimal longitude, java.math.BigDecimal altitude, 
                           Instant receivedAt, String rawPayload) {
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.batteryPercentage = batteryPercentage;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.receivedAt = receivedAt;
        this.rawPayload = rawPayload;
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

    public java.math.BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(java.math.BigDecimal latitude) {
        this.latitude = latitude;
    }

    // For backward compatibility
    public void setLatitude(Double latitude) {
        this.latitude = (latitude != null) ? new java.math.BigDecimal(latitude.toString()) : null;
    }

    public java.math.BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(java.math.BigDecimal longitude) {
        this.longitude = longitude;
    }

    // For backward compatibility
    public void setLongitude(Double longitude) {
        this.longitude = (longitude != null) ? new java.math.BigDecimal(longitude.toString()) : null;
    }

    public java.math.BigDecimal getAltitude() {
        return altitude;
    }

    public void setAltitude(java.math.BigDecimal altitude) {
        this.altitude = altitude;
    }

    // For backward compatibility
    public void setAltitude(Double altitude) {
        this.altitude = (altitude != null) ? new java.math.BigDecimal(altitude.toString()) : null;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    @Override
    public String toString() {
        return "BathTemperature{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", temperature=" + temperature +
                ", batteryPercentage=" + batteryPercentage +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", receivedAt=" + receivedAt +
                '}';
    }
}
