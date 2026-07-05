package com.example.aims.payment;

import com.example.aims.dto.response.PaymentCompletionResponse;
import com.example.aims.dto.response.PaymentInitiationResponse;

public interface PaymentProvider {

    PaymentMethod method();

    PaymentInitiationResponse initiate(Integer orderId);

    PaymentCompletionResponse complete(PaymentCompletionCommand command);
}
