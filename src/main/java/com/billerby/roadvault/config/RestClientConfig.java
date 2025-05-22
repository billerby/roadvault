package com.billerby.roadvault.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for REST client templates.
 * Provides configured RestTemplate beans with proper timeouts and error handling.
 */
@Configuration
public class RestClientConfig {

    /**
     * RestTemplate with timeouts for external API calls.
     * Configured to prevent hanging on network issues.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .connectTimeout(Duration.ofSeconds(5))    // Connection timeout
                .readTimeout(Duration.ofSeconds(15))      // Read timeout  
                .build();
    }

    /**
     * RestTemplate specifically for TTN API calls with shorter timeouts.
     * More aggressive timeouts to prevent blocking scheduled tasks.
     */
    @Bean("ttnRestTemplate")
    public RestTemplate ttnRestTemplate() {
        return new RestTemplateBuilder()
                .connectTimeout(Duration.ofSeconds(3))    // Quick connection timeout
                .readTimeout(Duration.ofSeconds(10))      // Reasonable read timeout
                .build();
    }
}
