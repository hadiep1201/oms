package com.example.aims.payment;

import com.example.aims.dto.response.PayOrderResponse;

/**
 * Gateway-neutral port for reading the pay-order result (order + delivery + transaction
 * info) for any payment method.
 *
 * Implemented by PayOrderQueryService; used by the payment providers when completing a
 * payment. Being gateway-neutral, a new QR/redirect gateway reuses it instead of
 * re-implementing the read/assemble logic.
 *
 * Cohesion: Functional - one operation: read an order's pay-order view.
 * Coupling: Data coupling - input is Integer orderId; output is a DTO.
 */
public interface IPayOrderResult {

    PayOrderResponse getPayOrderResult(Integer orderId);
}