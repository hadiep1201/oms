package com.example.aims.config;

import com.example.aims.security.JwtAuthenticationFilter;
import com.example.aims.security.RestAuthenticationHandlers;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {

    JwtAuthenticationFilter jwtAuthenticationFilter;
    RestAuthenticationHandlers restAuthenticationHandlers;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationHandlers)
                        .accessDeniedHandler(restAuthenticationHandlers))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/refresh").permitAll()
                        .requestMatchers("/api/token_generate", "/bank/api/transaction-sync").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/featured").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/manager")
                        .hasRole("PRODUCT_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/products/{id:\\d+}").permitAll()
                        .requestMatchers("/api/orders/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products")
                        .hasRole("PRODUCT_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**")
                        .hasRole("PRODUCT_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                        .hasRole("PRODUCT_MANAGER")
                        .requestMatchers("/api/files/**")
                        .hasRole("PRODUCT_MANAGER")
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/auth/me", "/api/auth/logout")
                        .authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
