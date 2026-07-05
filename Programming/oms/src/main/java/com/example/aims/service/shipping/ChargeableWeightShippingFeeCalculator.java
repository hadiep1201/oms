package com.example.aims.service.shipping;

import com.example.aims.dto.response.ShippingFeeResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ChargeableWeightShippingFeeCalculator implements ShippingFeeCalculatorStrategy {

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("100000");
    private static final BigDecimal MAX_FREE_SHIPPING_DISCOUNT = new BigDecimal("25000");
    private static final BigDecimal HANOI_HCMC_BASE_FEE = new BigDecimal("22000");
    private static final BigDecimal HANOI_HCMC_BASE_WEIGHT = new BigDecimal("3.0");
    private static final BigDecimal OTHER_PROVINCE_BASE_FEE = new BigDecimal("30000");
    private static final BigDecimal OTHER_PROVINCE_BASE_WEIGHT = new BigDecimal("0.5");
    private static final BigDecimal EXTRA_FEE_PER_HALF_KG = new BigDecimal("2500");
    private static final BigDecimal HALF_KG = new BigDecimal("0.5");

    @Override
    public ShippingFeeStrategyType getType() {
        return ShippingFeeStrategyType.CHARGEABLE_WEIGHT;
    }

    @Override
    public ShippingFeeResponse calculate(ShippingFeeCalculationContext context) {
        BigDecimal actualWeight = defaultToZero(context.getActualWeight());
        BigDecimal volumetricWeight = defaultToZero(context.getVolumetricWeight());
        BigDecimal subTotal = defaultToZero(context.getSubTotal());
        BigDecimal chargeableWeight = actualWeight.max(volumetricWeight);

        BigDecimal shippingFee = calculateShippingFeeByWeight(chargeableWeight, context.getCity());

        boolean freeShippingApplied = false;
        if (subTotal.compareTo(FREE_SHIPPING_THRESHOLD) > 0) {
            BigDecimal discount = shippingFee.min(MAX_FREE_SHIPPING_DISCOUNT);
            shippingFee = shippingFee.subtract(discount);
            freeShippingApplied = true;
        }

        return ShippingFeeResponse.builder()
                .shippingFee(shippingFee)
                .freeShippingApplied(freeShippingApplied)
                .build();
    }

    private BigDecimal calculateShippingFeeByWeight(BigDecimal totalWeight, String city) {
        BigDecimal baseFee;
        BigDecimal extraWeight;

        String cityLower = city == null ? "" : city.toLowerCase();
        if (cityLower.contains("hanoi") || cityLower.contains("hochiminh")|| cityLower.contains("hà nội") || cityLower.contains("ha noi")
                || cityLower.contains("hồ chí minh") || cityLower.contains("ho chi minh")) {
            baseFee = HANOI_HCMC_BASE_FEE;
            extraWeight = totalWeight.subtract(HANOI_HCMC_BASE_WEIGHT).max(BigDecimal.ZERO);
        } else {
            baseFee = OTHER_PROVINCE_BASE_FEE;
            extraWeight = totalWeight.subtract(OTHER_PROVINCE_BASE_WEIGHT).max(BigDecimal.ZERO);
        }

        BigDecimal extraUnits = extraWeight.divide(HALF_KG, 0, RoundingMode.CEILING);
        BigDecimal extraFee = extraUnits.multiply(EXTRA_FEE_PER_HALF_KG);

        return baseFee.add(extraFee);
    }

    private BigDecimal defaultToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
