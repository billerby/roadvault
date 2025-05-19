package com.billerby.roadvault.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

/**
 * Configuration for FreeMarker and Apache FOP.
 */
@Configuration
public class TemplateConfig {

    /**
     * Configure FreeMarker for template processing.
     * This is separate from the web FreeMarker configuration.
     * 
     * @return A configured FreeMarkerConfigurer
     */
    @Bean
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
    public freemarker.template.Configuration freemarkerConfiguration(FreeMarkerConfigurer configurer) {
        return configurer.getConfiguration();
    }
}
