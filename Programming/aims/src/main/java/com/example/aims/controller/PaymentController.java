package com.example.aims.controller;

import com.example.aims.dto.request.CompletePaymentRequest;
import com.example.aims.dto.request.InitiatePaymentRequest;
import com.example.aims.dto.response.ApiResponse;
import com.example.aims.dto.response.PaymentCompletionResponse;
import com.example.aims.dto.response.PaymentInitiationResponse;
import com.example.aims.payment.PaymentMethod;
import com.example.aims.payment.PaymentOrchestratorService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentOrchestratorService paymentOrchestratorService;

    @PostMapping("/{orderId}/payments")
    public ApiResponse<PaymentInitiationResponse> initiatePayment(
            @PathVariable Integer orderId,
            @RequestBody @Valid InitiatePaymentRequest request) {
        PaymentInitiationResponse result = paymentOrchestratorService.initiate(
                orderId,
                request.getMethod()
        );
        return ApiResponse.<PaymentInitiationResponse>builder().result(result).build();
    }

    @PostMapping("/{orderId}/payments/{method}/complete")
    public ApiResponse<PaymentCompletionResponse> completePayment(
            @PathVariable Integer orderId,
            @PathVariable PaymentMethod method,
            @RequestBody(required = false) CompletePaymentRequest request) {
        PaymentCompletionResponse result = paymentOrchestratorService.complete(
                orderId,
                method,
                request
        );
        return ApiResponse.<PaymentCompletionResponse>builder().result(result).build();
    }
}
