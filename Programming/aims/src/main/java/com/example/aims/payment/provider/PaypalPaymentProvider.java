package com.example.aims.payment.provider;

import com.example.aims.dto.response.PayOrderResponse;
import com.example.aims.dto.response.PaymentCompletionResponse;
import com.example.aims.dto.response.PaymentInitiationResponse;
import com.example.aims.exception.PaymentErrorCode;
import com.example.aims.exception.PaymentException;
import com.example.aims.payment.PaymentCompletionCommand;
import com.example.aims.payment.PaymentFlowType;
import com.example.aims.payment.PaymentMethod;
import com.example.aims.payment.PaymentProvider;
import com.example.aims.subsystempaypal.IPayPalPaymentService;
import com.example.aims.subsystempaypal.dto.UrlResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaypalPaymentProvider implements PaymentProvider {

    IPayPalPaymentService payPalPaymentService;

    @Override
    public PaymentMethod method() {
        return PaymentMethod.PAYPAL;
    }

    @Override
    public PaymentInitiationResponse initiate(Integer orderId) {
        UrlResponse response = payPalPaymentService.payOrder(orderId);
        return PaymentInitiationResponse.builder()
                .method(method())
                .flowType(PaymentFlowType.REDIRECT)
                .redirectUrl(response.getUrl())
                .build();
    }

    @Override
    public PaymentCompletionResponse complete(PaymentCompletionCommand command) {
        if (command.getToken() == null || command.getToken().isBlank()) {
            throw new PaymentException(PaymentErrorCode.CAPTURE_FAILED,
                    "PayPal completion token is required");
        }
        PayOrderResponse order = payPalPaymentService.capturePayment(
                command.getOrderId(),
                command.getToken()
        );
        return PaymentCompletionResponse.builder()
                .method(method())
                .order(order)
                .build();
    }
}
