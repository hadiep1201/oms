package com.example.aims.controller;

import com.example.aims.dto.request.CapturePaypalPaymentRequest;
import com.example.aims.dto.request.CompletePaymentRequest;
import com.example.aims.dto.response.ApiResponse;
import com.example.aims.dto.response.PayOrderResponse;
import com.example.aims.dto.response.PaymentCompletionResponse;
import com.example.aims.dto.response.PaymentInitiationResponse;
import com.example.aims.payment.PaymentMethod;
import com.example.aims.payment.PaymentOrchestratorService;
import com.example.aims.subsystempaypal.dto.UrlResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Compatibility controller for the legacy PayPal endpoints.
 * The payment workflow is delegated to PaymentOrchestratorService so provider
 * logic stays behind the common PaymentProvider abstraction.
 *
 * Cohesion: Communicational - all endpoints handle PayPal payment for an order.
 * Coupling: Data coupling with PaymentOrchestratorService (orderId, token).
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentGatewayController {

    PaymentOrchestratorService paymentOrchestratorService;

    @PostMapping("/{orderId}/payments/paypal")
    public ApiResponse<UrlResponse> payOrder(@PathVariable Integer orderId) {
        PaymentInitiationResponse payment = paymentOrchestratorService.initiate(
                orderId,
                PaymentMethod.PAYPAL
        );
        UrlResponse result = UrlResponse.builder()
                .url(payment.getRedirectUrl())
                .build();
        return ApiResponse.<UrlResponse>builder().result(result).build();
    }

    @PostMapping("/{orderId}/payments/paypal/capture")
    public ApiResponse<PayOrderResponse> capturePayment(
            @PathVariable Integer orderId,
            @RequestBody @Valid CapturePaypalPaymentRequest request) {
        PaymentCompletionResponse payment = paymentOrchestratorService.complete(
                orderId,
                PaymentMethod.PAYPAL,
                CompletePaymentRequest.builder()
                        .token(request.getToken())
                        .build()
        );
        PayOrderResponse result = payment.getOrder();
        return ApiResponse.<PayOrderResponse>builder().result(result).build();
    }
}
