package com.example.aims.subsystempaypal;

import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.subsystempaypal.dto.AmountDTO;
import com.example.aims.subsystempaypal.dto.PayPalRefundRequest;
import com.example.aims.subsystempaypal.dto.PaypalExperienceContext;
import com.example.aims.subsystempaypal.dto.PaypalOrderRequest;
import com.example.aims.subsystempaypal.dto.PaypalPaymentSource;
import com.example.aims.subsystempaypal.dto.PaypalWalletSource;
import com.example.aims.subsystempaypal.dto.PurchaseUnit;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayPalOrderRequestMapper {

    PaypalConfig paypalConfig;
    CurrencyConverter currencyConverter;

    public PaypalOrderRequest toCreateOrderRequest(Order order, Invoice invoice) {
        return PaypalOrderRequest.builder()
                .intent("CAPTURE")
                .purchaseUnits(List.of(
                        PurchaseUnit.builder()
                                .customId(String.valueOf(order.getOrderId()))
                                .invoiceId(String.valueOf(invoice.getInvoiceId()) + "-" + java.util.UUID.randomUUID().toString().substring(0, 8))
                                .amount(AmountDTO.builder()
                                        .currencyCode("USD")
                                        .value(currencyConverter.toUsdString(invoice))
                                        .build())
                                .build()
                ))
                .paymentSource(PaypalPaymentSource.builder()
                        .paypal(PaypalWalletSource.builder()
                                .experienceContext(PaypalExperienceContext.builder()
                                        .returnUrl(paypalConfig.getReturnUrl())
                                        .cancelUrl(paypalConfig.getCancelUrl())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public PayPalRefundRequest toRefundRequest(BigDecimal usdAmount) {
        return PayPalRefundRequest.builder()
                .amount(AmountDTO.builder()
                        .currencyCode("USD")
                        .value(String.format(Locale.US, "%.2f", usdAmount))
                        .build())
                .build();
    }
}
