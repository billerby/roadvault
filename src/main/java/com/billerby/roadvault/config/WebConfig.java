package com.billerby.roadvault.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Web configuration for SPA support.
 * This configuration ensures that all frontend routes are properly handled
 * by serving index.html for any path that doesn't match an API endpoint or static resource.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Handle static assets from /assets/ path with proper caching
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCachePeriod(3600); // Cache for 1 hour in production
        
        // Handle other static files (favicons, manifests, etc.)
        registry.addResourceHandler("/favicon.ico", "/favicon.png", "/favicon.svg", 
                        "/site.webmanifest", "/*.worker.js")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
        
        // Handle all other paths that aren't API paths - serve index.html for SPA routing
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new IndexHtmlResourceResolver());
    }

    /**
     * Custom resource resolver that serves index.html for any path that:
     * 1. Doesn't start with /api/
     * 2. Doesn't match a static resource
     * 3. Is not an actuator endpoint
     * 
     * This enables client-side routing for the Vue.js SPA.
     */
    private static class IndexHtmlResourceResolver extends PathResourceResolver {
        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource requestedResource = location.createRelative(resourcePath);
            
            // If the requested resource exists, serve it
            if (requestedResource.exists() && requestedResource.isReadable()) {
                return requestedResource;
            }
            
            // Don't serve index.html for API endpoints or actuator endpoints
            if (resourcePath.startsWith("api/") || resourcePath.startsWith("actuator/")) {
                return null;
            }
            
            // For all other paths, serve index.html to enable SPA routing
            return new ClassPathResource("/static/index.html");
        }
    }
}
