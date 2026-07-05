package com.example.aims.payment;

/**
 * Gateway-neutral port exposing the server-side payable amount for an order (VND), read
 * from the order's invoice total.
 *
 * Implemented by PayOrderQueryService; used by the sandbox payment-confirmation strategy
 * (to build the Test Callback). Provided server-side so callers never supply or trust a
 * client amount. Neutral, so any gateway that needs the amount reuses it.
 *
 * Cohesion: Functional - one operation: read an order's payable amount.
 * Coupling: Data coupling - input is Integer orderId; output is a scalar.
 */
public interface IPayableAmountSource {

    long getPayableAmount(Integer orderId);
}