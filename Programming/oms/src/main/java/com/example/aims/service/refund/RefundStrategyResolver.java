package com.example.aims.service.refund;

import com.example.aims.exception.PaymentException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class RefundStrategyResolver {

    private final Map<RefundMethod, IRefundStrategy> strategies;

    public RefundStrategyResolver(List<IRefundStrategy> strategyList) {
        this.strategies = new EnumMap<>(RefundMethod.class);
        for (IRefundStrategy strategy : strategyList) {
            strategies.put(strategy.getMethod(), strategy);
        }
    }

    public IRefundStrategy resolve(String paymentMethod) {
        RefundMethod method = RefundMethod.from(paymentMethod);
        if (method == null || !strategies.containsKey(method)) {
            throw new PaymentException("UNSUPPORTED_REFUND_METHOD",
                    "Unsupported refund method: " + paymentMethod);
        }
        return strategies.get(method);
    }
}
