package com.example.aims.payment;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCompletionCommand {
    Integer orderId;
    PaymentMethod method;
    String token;
}
