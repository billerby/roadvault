package com.billerby.roadvault.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

/**
 * Test-specific configuration for FreeMarker and Apache FOP.
 */
@Configuration
@Profile("test")
public class TestTemplateConfig {

    /**
     * Configure FreeMarker for template processing in tests.
     * 
     * @return A configured FreeMarkerConfigurer
     */
    @Bean
    @Primary
    public FreeMarkerConfigurer freemarkerConfigurer() {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPath("classpath:/templates/");
        configurer.setDefaultEncoding("UTF-8");
        return configurer;
    }
    
    /**
     * This Bean exposes the FreeMarker Configuration for direct use.
     * 
     * @param configurer The FreeMarkerConfigurer bean
     * @return The FreeMarker Configuration instance
     */
    @Bean
    @Primary
    public freemarker.template.Configuration freemarkerConfiguration(FreeMarkerConfigurer configurer) {
        return configurer.getConfiguration();
    }
}
