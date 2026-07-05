package com.example.aims.dto.response;

import com.example.aims.entity.QRCode;
import com.example.aims.payment.PaymentFlowType;
import com.example.aims.payment.PaymentMethod;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentInitiationResponse {

    PaymentMethod method;
    PaymentFlowType flowType;
    String redirectUrl;
    QRCode qrCode;
}
