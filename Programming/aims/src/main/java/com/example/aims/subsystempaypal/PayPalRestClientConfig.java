package com.example.aims.subsystempaypal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PayPalRestClientConfig {

    @Bean
    public RestClient paypalRestClient() {
        return RestClient.builder().build();
    }
}
