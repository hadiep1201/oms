package com.example.aims.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Coupling: Control coupling with browser clients (defines allowed origins and HTTP methods for API routes)
 * Cohesion: Functional cohesion
 * Reason: Configures cross-origin access and shared RestTemplate so the FE can call backend APIs
 *         (including ProductCommandController CUD endpoints) from localhost:4200.
 *
 * SOLID Review:
 * - SRP risk: configures both CORS for web MVC and RestTemplate bean creation in one class.
 *   Impact: unrelated infrastructure concerns (HTTP client vs CORS policy) change together.
 * - OCP/DIP/LSP/ISP: no clear violation for the CUD use case specifically.
 * Improvement direction: split CorsConfig and RestTemplateConfig into separate @Configuration classes.
 */
@Configuration
public class WebConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String uploadLocation = Paths.get("uploads").toAbsolutePath().toUri().toString();
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations(uploadLocation);
            }
        };
    }
}
