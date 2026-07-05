package com.example.aims.service.shipping;

import com.example.aims.dto.response.ShippingFeeResponse;

public interface ShippingFeeCalculatorStrategy {
    ShippingFeeStrategyType getType();

    ShippingFeeResponse calculate(ShippingFeeCalculationContext context);
}
