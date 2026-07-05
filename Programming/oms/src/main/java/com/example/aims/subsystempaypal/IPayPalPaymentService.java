package com.example.aims.subsystempaypal;

import com.example.aims.dto.response.PayOrderResponse;
import com.example.aims.subsystempaypal.dto.PayPalRefundResponse;
import com.example.aims.subsystempaypal.dto.UrlResponse;

import java.math.BigDecimal;

public interface IPayPalPaymentService {
    UrlResponse payOrder(Integer orderId);
    PayOrderResponse capturePayment(Integer orderId, String paypalToken);
    PayPalRefundResponse refundCapture(String captureId, BigDecimal amountVnd);
}
