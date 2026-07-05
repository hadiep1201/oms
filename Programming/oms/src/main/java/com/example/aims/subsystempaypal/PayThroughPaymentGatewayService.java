package com.example.aims.subsystempaypal;

import com.example.aims.dto.response.PayOrderResponse;
import com.example.aims.entity.Invoice;
import com.example.aims.entity.Order;
import com.example.aims.entity.PaymentTransaction;
import com.example.aims.enums.OrderStatus;
import com.example.aims.exception.PaymentErrorCode;
import com.example.aims.exception.PaymentException;
import com.example.aims.repository.InvoiceRepository;
import com.example.aims.repository.OrderRepository;
import com.example.aims.repository.PaymentTransactionRepository;
import com.example.aims.subsystempaypal.dto.PayPalRefundRequest;
import com.example.aims.subsystempaypal.dto.PayPalRefundResponse;
import com.example.aims.subsystempaypal.dto.PaypalCaptureResponse;
import com.example.aims.subsystempaypal.dto.PaypalOrderRequest;
import com.example.aims.subsystempaypal.dto.PaypalOrderResponse;
import com.example.aims.subsystempaypal.dto.PaypalTokenResponse;
import com.example.aims.subsystempaypal.dto.UrlResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayThroughPaymentGatewayService implements IPayPalPaymentService {

    OrderRepository orderRepository;
    InvoiceRepository invoiceRepository;
    PaymentTransactionRepository paymentTransactionRepository;
    IPayPalApiGateway apiGateway;
    PayPalPaymentValidator validator;
    PayPalOrderRequestMapper requestMapper;
    PayPalTransactionFactory transactionFactory;
    PayOrderResponseAssembler assembler;
    CurrencyConverter currencyConverter;

    @Override
    @Transactional(readOnly = true)
    public UrlResponse payOrder(Integer orderId) {
        validator.validateConfig();
        Order order = loadOrderForPayment(orderId);
        Invoice invoice = validator.requireInvoice(order);
        validator.ensureNoExistingTransaction(invoice);

        PaypalOrderRequest request = requestMapper.toCreateOrderRequest(order, invoice);
        PaypalTokenResponse tokenResponse = apiGateway.getAccessToken();
        PaypalOrderResponse orderResponse = apiGateway.createOrder(request, tokenResponse.getAccessToken());
        return assembler.toUrlResponse(orderResponse);
    }

    @Override
    @Transactional
    public PayOrderResponse capturePayment(Integer orderId, String token) {
        validator.validateConfig();
        Order order = loadOrder(orderId);
        Invoice invoice = validator.requireInvoice(order);

        PaymentTransaction existingTransaction = paymentTransactionRepository
                .findByInvoice_InvoiceId(invoice.getInvoiceId())
                .orElse(null);
        if (existingTransaction != null) {
            invoice.setPaymentTransaction(existingTransaction);
            return assembler.toPayOrderResponse(order, invoice, existingTransaction);
        }

        validator.ensurePendingPayment(order);

        PaypalTokenResponse tokenResponse = apiGateway.getAccessToken();
        PaypalCaptureResponse captureResponse = apiGateway.capturePayment(token, tokenResponse.getAccessToken());
        if (!"COMPLETED".equalsIgnoreCase(captureResponse.getStatus())) {
            throw new PaymentException(PaymentErrorCode.CAPTURE_FAILED,
                    "PayPal capture did not complete successfully");
        }

        PaymentTransaction transaction = transactionFactory.createCaptureTransaction(order, invoice, token, captureResponse);
        invoice.setPaymentTransaction(transaction);
        invoiceRepository.save(invoice);

        order.setStatus(OrderStatus.PAYMENT_SUCCESS);
        orderRepository.save(order);

        return assembler.toPayOrderResponse(order, invoice, transaction);
    }

    @Override
    public PayPalRefundResponse refundCapture(String captureId, BigDecimal amountVnd) {
        validator.validateConfig();
        if (captureId == null || captureId.isBlank()) {
            throw new PaymentException(PaymentErrorCode.REFUND_CAPTURE_ID_MISSING,
                    "PayPal capture ID is missing");
        }
        if (amountVnd == null || amountVnd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException(PaymentErrorCode.REFUND_AMOUNT_INVALID,
                    "Refund amount is invalid");
        }

        PaypalTokenResponse tokenResponse = apiGateway.getAccessToken();
        BigDecimal usdAmount = currencyConverter.toUsd(amountVnd);
        PayPalRefundRequest refundRequest = requestMapper.toRefundRequest(usdAmount);
        return apiGateway.refundPayment(captureId, refundRequest, tokenResponse.getAccessToken());
    }

    private Order loadOrderForPayment(Integer orderId) {
        Order order = loadOrder(orderId);
        validator.ensurePendingPayment(order);
        return order;
    }

    private Order loadOrder(Integer orderId) {
        return orderRepository.findDetailedByOrderId(orderId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORDER_NOT_FOUND,
                        "Order not found: " + orderId));
    }
}
