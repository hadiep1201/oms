package com.example.aims.subsystempaypal;

import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.enums.OrderStatus;
import com.example.aims.exception.PaymentErrorCode;
import com.example.aims.exception.PaymentException;
import com.example.aims.repository.PaymentTransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayPalPaymentValidator {

    PaypalConfig paypalConfig;
    PaymentTransactionRepository paymentTransactionRepository;

    public void validateConfig() {
        if (isBlank(paypalConfig.getClientId())
                || isBlank(paypalConfig.getSecretKey())
                || isBlank(paypalConfig.getBaseUrl())
                || isBlank(paypalConfig.getReturnUrl())
                || isBlank(paypalConfig.getCancelUrl())) {
            throw new PaymentException(PaymentErrorCode.CONFIG_INCOMPLETE, "PayPal configuration is incomplete");
        }
    }

    public void ensurePendingPayment(Order order) {
        if (!OrderStatus.PENDING_PAYMENT.equals(order.getStatus())) {
            throw new PaymentException(PaymentErrorCode.ORDER_NOT_PENDING_PAYMENT,
                    "Order is not in PENDING_PAYMENT state");
        }
    }

    public Invoice requireInvoice(Order order) {
        Invoice invoice = order.getInvoice();
        if (invoice == null) {
            throw new PaymentException(PaymentErrorCode.INVOICE_MISSING,
                    "Invoice not found for order: " + order.getOrderId());
        }
        return invoice;
    }

    public void ensureNoExistingTransaction(Invoice invoice) {
        if (paymentTransactionRepository.findByInvoice_InvoiceId(invoice.getInvoiceId()).isPresent()) {
            throw new PaymentException(PaymentErrorCode.DUPLICATE_TRANSACTION,
                    "Invoice already has a payment transaction");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
