package com.example.aims.subsystempaypal;

import com.example.aims.subsystempaypal.dto.PayPalRefundRequest;
import com.example.aims.subsystempaypal.dto.PayPalRefundResponse;
import com.example.aims.subsystempaypal.dto.PaypalCaptureResponse;
import com.example.aims.subsystempaypal.dto.PaypalOrderRequest;
import com.example.aims.subsystempaypal.dto.PaypalOrderResponse;
import com.example.aims.subsystempaypal.dto.PaypalTokenResponse;

public interface IPayPalApiGateway {
    PaypalTokenResponse getAccessToken();
    PaypalOrderResponse createOrder(PaypalOrderRequest request, String accessToken);
    PaypalCaptureResponse capturePayment(String paypalOrderId, String accessToken);
    PayPalRefundResponse refundPayment(String captureId, PayPalRefundRequest request, String accessToken);
}
