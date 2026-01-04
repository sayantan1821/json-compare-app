package com.jsoncompare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

//    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:4200}")
//    private String allowedOrigins;

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        // Split and trim origins
//        String[] origins = allowedOrigins.split(",");
//        for (int i = 0; i < origins.length; i++) {
//            origins[i] = origins[i].trim();
//        }
//
//        // Configure CORS for API endpoints
//        registry.addMapping("/api/**")
//                .allowedOrigins(origins)
//                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
//                .allowedHeaders("Content-Type", "Authorization", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers")
//                .exposedHeaders("Authorization", "Content-Type")
//                .allowCredentials(true)
//                .maxAge(3600);
//
//        // Also allow CORS for health endpoint
//        registry.addMapping("/health")
//                .allowedOrigins(origins)
//                .allowedMethods("GET", "OPTIONS")
//                .allowedHeaders("*")
//                .maxAge(3600);
//    }
}

