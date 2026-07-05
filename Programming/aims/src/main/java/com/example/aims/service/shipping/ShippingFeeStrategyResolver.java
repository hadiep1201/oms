package com.example.aims.service.shipping;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ShippingFeeStrategyResolver {

    private final Map<ShippingFeeStrategyType, ShippingFeeCalculatorStrategy> strategies;

    public ShippingFeeStrategyResolver(List<ShippingFeeCalculatorStrategy> strategyList) {
        this.strategies = new EnumMap<>(ShippingFeeStrategyType.class);

        for (ShippingFeeCalculatorStrategy strategy : strategyList) {
            strategies.put(strategy.getType(), strategy);
        }
    }

    public ShippingFeeCalculatorStrategy resolve(ShippingFeeStrategyType type) {
        ShippingFeeCalculatorStrategy strategy = strategies.get(type);

        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported shipping fee strategy: " + type);
        }

        return strategy;
    }
}
