package com.billerby.roadvault.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables scheduling for the application.
 * This allows the use of @Scheduled annotations.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Configuration class for enabling scheduling
}
