package com.example.aims.payment;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentConfirmationCommand {
    Integer orderId;
    Long paidAmount;
    String reference;
    String method;
}