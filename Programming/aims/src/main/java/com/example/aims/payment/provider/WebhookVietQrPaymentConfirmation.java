package com.example.aims.payment.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "vietqr.confirmation.mode", havingValue = "production")
public class WebhookVietQrPaymentConfirmation implements VietQrPaymentConfirmation {

    @Override
    public String confirm(Integer orderId) {
        return null;
    }
}