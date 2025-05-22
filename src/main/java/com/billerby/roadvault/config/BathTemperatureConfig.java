package com.billerby.roadvault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for bath temperature data polling.
 */
@Configuration
public class BathTemperatureConfig {

    @Value("${badtemperatur.api.url}")
    private String apiUrl;

    @Value("${badtemperatur.token}")
    private String token;

    public String getApiUrl() {
        return apiUrl;
    }

    public String getToken() {
        return token;
    }
}
