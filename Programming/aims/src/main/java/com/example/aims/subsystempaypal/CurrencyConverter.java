package com.example.aims.subsystempaypal;

import com.example.aims.entity.Invoice;
import com.example.aims.exception.PaymentErrorCode;
import com.example.aims.exception.PaymentException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CurrencyConverter {

    PaypalConfig paypalConfig;

    public BigDecimal toUsd(BigDecimal vndAmount) {
        if (vndAmount == null) {
            throw new PaymentException(PaymentErrorCode.INVOICE_AMOUNT_MISSING,
                    "Amount to convert is missing");
        }
        BigDecimal exchangeRate = paypalConfig.getVndToUsdRate();
        if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException(PaymentErrorCode.CONFIG_INCOMPLETE,
                    "PayPal exchange rate configuration is invalid");
        }
        return vndAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP);
    }

    public String toUsdString(Invoice invoice) {
        BigDecimal totalAmount = invoice.getTotalAmount();
        if (totalAmount == null) {
            throw new PaymentException(PaymentErrorCode.INVOICE_AMOUNT_MISSING,
                    "Invoice total amount is missing");
        }
        return toUsd(totalAmount).toPlainString();
    }
}
