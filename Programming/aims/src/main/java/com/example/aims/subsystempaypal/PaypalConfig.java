package com.example.aims.subsystempaypal;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Coupling level(s): Data coupling through Spring configuration binding.
 * Related class(es): data coupling with PayThroughPaymentGatewayService and PayPalApiGateway,
 * which read PayPal credentials, URLs, and exchange-rate values from this configuration object.
 * Cohesion level: Functional cohesion.
 * Reason: The class only groups PayPal configuration values that are injected into PayPal services
 * and gateways. It does not contain behavior or shared mutable global state.
 */
@Data
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "payment.paypal")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaypalConfig {

    String clientId;
    String secretKey;
    String baseUrl;
    String returnUrl;
    String cancelUrl;
    BigDecimal vndToUsdRate;
}
