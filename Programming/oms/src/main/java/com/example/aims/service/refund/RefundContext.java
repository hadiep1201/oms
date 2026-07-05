package com.example.aims.service.refund;

import com.example.aims.entity.Invoice;
import com.example.aims.entity.PaymentTransaction;
import com.example.aims.entity.User;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RefundContext {
    PaymentTransaction transaction;
    Invoice invoice;
    User manager;
    String reason;
}
