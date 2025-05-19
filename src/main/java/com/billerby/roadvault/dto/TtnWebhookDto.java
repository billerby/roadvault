package com.billerby.roadvault.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for handling incoming webhooks from The Things Network.
 * This represents the structure of the webhook payload for temperature sensor data.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TtnWebhookDto {

    @JsonProperty("result")
    private Result result;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("end_device_ids")
        private EndDeviceIds endDeviceIds;

        @JsonProperty("received_at")
        private String receivedAt;

        @JsonProperty("uplink_message")
        private UplinkMessage uplinkMessage;

        // Getters and setters
        public EndDeviceIds getEndDeviceIds() {
            return endDeviceIds;
        }

        public void setEndDeviceIds(EndDeviceIds endDeviceIds) {
            this.endDeviceIds = endDeviceIds;
        }

        public String getReceivedAt() {
            return receivedAt;
        }

        public void setReceivedAt(String receivedAt) {
            this.receivedAt = receivedAt;
        }

        public UplinkMessage getUplinkMessage() {
            return uplinkMessage;
        }

        public void setUplinkMessage(UplinkMessage uplinkMessage) {
            this.uplinkMessage = uplinkMessage;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EndDeviceIds {
        @JsonProperty("device_id")
        private String deviceId;

        @JsonProperty("dev_eui")
        private String devEui;

        @JsonProperty("dev_addr")
        private String devAddr;

        // Getters and setters
        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDevEui() {
            return devEui;
        }

        public void setDevEui(String devEui) {
            this.devEui = devEui;
        }

        public String getDevAddr() {
            return devAddr;
        }

        public void setDevAddr(String devAddr) {
            this.devAddr = devAddr;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UplinkMessage {
        @JsonProperty("f_port")
        private Integer fPort;

        @JsonProperty("f_cnt")
        private Integer fCnt;

        @JsonProperty("frm_payload")
        private String frmPayload;

        @JsonProperty("decoded_payload")
        private Map<String, Object> decodedPayload;

        @JsonProperty("rx_metadata")
        private List<RxMetadata> rxMetadata;

        @JsonProperty("last_battery_percentage")
        private LastBatteryPercentage lastBatteryPercentage;

        @JsonProperty("locations")
        private Map<String, Location> locations;

        // Getters and setters
        public Integer getfPort() {
            return fPort;
        }

        public void setfPort(Integer fPort) {
            this.fPort = fPort;
        }

        public Integer getfCnt() {
            return fCnt;
        }

        public void setfCnt(Integer fCnt) {
            this.fCnt = fCnt;
        }

        public String getFrmPayload() {
            return frmPayload;
        }

        public void setFrmPayload(String frmPayload) {
            this.frmPayload = frmPayload;
        }

        public Map<String, Object> getDecodedPayload() {
            return decodedPayload;
        }

        public void setDecodedPayload(Map<String, Object> decodedPayload) {
            this.decodedPayload = decodedPayload;
        }

        public List<RxMetadata> getRxMetadata() {
            return rxMetadata;
        }

        public void setRxMetadata(List<RxMetadata> rxMetadata) {
            this.rxMetadata = rxMetadata;
        }

        public LastBatteryPercentage getLastBatteryPercentage() {
            return lastBatteryPercentage;
        }

        public void setLastBatteryPercentage(LastBatteryPercentage lastBatteryPercentage) {
            this.lastBatteryPercentage = lastBatteryPercentage;
        }

        public Map<String, Location> getLocations() {
            return locations;
        }

        public void setLocations(Map<String, Location> locations) {
            this.locations = locations;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RxMetadata {
        @JsonProperty("gateway_ids")
        private Map<String, String> gatewayIds;

        @JsonProperty("time")
        private String time;

        @JsonProperty("rssi")
        private Integer rssi;

        @JsonProperty("snr")
        private Double snr;

        @JsonProperty("location")
        private Location location;

        // Getters and setters
        public Map<String, String> getGatewayIds() {
            return gatewayIds;
        }

        public void setGatewayIds(Map<String, String> gatewayIds) {
            this.gatewayIds = gatewayIds;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public Integer getRssi() {
            return rssi;
        }

        public void setRssi(Integer rssi) {
            this.rssi = rssi;
        }

        public Double getSnr() {
            return snr;
        }

        public void setSnr(Double snr) {
            this.snr = snr;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;

        @JsonProperty("altitude")
        private Double altitude;

        @JsonProperty("source")
        private String source;

        // Getters and setters
        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Double getAltitude() {
            return altitude;
        }

        public void setAltitude(Double altitude) {
            this.altitude = altitude;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LastBatteryPercentage {
        @JsonProperty("f_cnt")
        private Integer fCnt;

        @JsonProperty("value")
        private Integer value;

        @JsonProperty("received_at")
        private String receivedAt;

        // Getters and setters
        public Integer getfCnt() {
            return fCnt;
        }

        public void setfCnt(Integer fCnt) {
            this.fCnt = fCnt;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getReceivedAt() {
            return receivedAt;
        }

        public void setReceivedAt(String receivedAt) {
            this.receivedAt = receivedAt;
        }
    }

    // Getters and setters
    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
