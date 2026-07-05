package com.example.aims.payment;

import com.example.aims.exception.PaymentErrorCode;
import com.example.aims.exception.PaymentException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of the available PaymentProvider strategies, keyed by PaymentMethod.
 *
 * Spring injects every PaymentProvider bean, so adding a new gateway (a new
 * PaymentProvider implementation) requires no change here - the registry is open
 * for extension, closed for modification (OCP).
 *
 * Cohesion: Functional - resolves a PaymentMethod to its provider.
 * Coupling: Data coupling - exchanges PaymentMethod / PaymentProvider only.
 *
 * Error handling: an unknown method raises a domain PaymentException
 * (UNSUPPORTED_PAYMENT_METHOD), consistent with the rest of the payment flow, instead
 * of a web-layer ResponseStatusException; GlobalExceptionHandler maps it to 400.
 */
@Component
public class PaymentProviderRegistry {

    private final Map<PaymentMethod, PaymentProvider> providers;

    public PaymentProviderRegistry(List<PaymentProvider> paymentProviders) {
        this.providers = new EnumMap<>(PaymentMethod.class);
        for (PaymentProvider provider : paymentProviders) {
            this.providers.put(provider.method(), provider);
        }
    }

    public PaymentProvider get(PaymentMethod method) {
        PaymentProvider provider = providers.get(method);
        if (provider == null) {
            throw new PaymentException(PaymentErrorCode.UNSUPPORTED_PAYMENT_METHOD,
                    "Unsupported payment method: " + method);
        }
        return provider;
    }
}