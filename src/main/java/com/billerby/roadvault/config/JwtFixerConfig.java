package com.billerby.roadvault.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Key;

@Configuration
public class JwtFixerConfig {

    @Value("${roadvault.app.jwtSecret}")
    private String jwtSecret;

    @Bean
    public Key jwtSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        // Ensure key is properly sized for HS512
        return Keys.hmacShaKeyFor(keyBytes);
    }
}