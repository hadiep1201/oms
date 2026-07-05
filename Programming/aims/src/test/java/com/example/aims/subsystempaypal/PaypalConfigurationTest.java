package com.example.aims.subsystempaypal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaypalConfigurationTest {

    @Test
    void applicationYamlBindsPaypalGatewayPropertiesUnderPaymentPaypal() throws IOException {
        List<PropertySource<?>> propertySources = new YamlPropertySourceLoader()
                .load("application", new ClassPathResource("application.yaml"));

        assertThat(propertySources)
                .anySatisfy(source -> {
                    assertThat(source.getProperty("payment.paypal.base-url")).isNotNull();
                    assertThat(source.getProperty("payment.paypal.return-url")).isNotNull();
                    assertThat(source.getProperty("payment.paypal.cancel-url")).isNotNull();
                    assertThat(source.getProperty("payment.paypal.vnd-to-usd-rate")).isNotNull();
                    assertThat(source.getProperty("shipping.fee.base-url")).isNull();
                });
    }
}
