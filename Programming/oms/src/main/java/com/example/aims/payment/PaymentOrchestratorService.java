package com.example.aims.payment;

import com.example.aims.dto.request.CompletePaymentRequest;
import com.example.aims.dto.response.PaymentCompletionResponse;
import com.example.aims.dto.response.PaymentInitiationResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentOrchestratorService {

    PaymentProviderRegistry providerRegistry;

    public PaymentInitiationResponse initiate(Integer orderId, PaymentMethod method) {
        return providerRegistry.get(method).initiate(orderId);
    }

    public PaymentCompletionResponse complete(
            Integer orderId,
            PaymentMethod method,
            CompletePaymentRequest request
    ) {
        PaymentCompletionCommand command = PaymentCompletionCommand.builder()
                .orderId(orderId)
                .method(method)
                .token(request != null ? request.getToken() : null)
                .build();
        return providerRegistry.get(method).complete(command);
    }
}
