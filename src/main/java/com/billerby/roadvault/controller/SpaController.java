package com.billerby.roadvault.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to handle SPA routing for the Vue.js frontend.
 * This ensures that all frontend routes are served correctly by returning the index.html
 * for any request that doesn't match an API endpoint.
 */
@Controller
public class SpaController {

    /**
     * Serve the index.html for all frontend routes.
     * This enables client-side routing for the Vue.js SPA.
     */
    @GetMapping(value = {
        "/",
        "/login", 
        "/dashboard",
        "/members/**",
        "/payments/**",
        "/reports/**", 
        "/admin/**",
        "/profile/**"
    })
    public ResponseEntity<Resource> index() {
        Resource resource = new ClassPathResource("static/index.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }
}

