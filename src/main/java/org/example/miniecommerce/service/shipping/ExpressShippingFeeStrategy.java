package org.example.miniecommerce.service.shipping;

import org.example.miniecommerce.entity.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy Pattern: Chiến lược tính phí ship nhanh
 */
@Component
public class ExpressShippingFeeStrategy implements ShippingFeeStrategy {

    private static final BigDecimal EXPRESS_FEE = new BigDecimal("50000");

    @Override
    public BigDecimal calculateFee(Order order) {
        // Phí ship nhanh cố định
        return EXPRESS_FEE;
    }

    @Override
    public boolean supports(String method) {
        return "EXPRESS".equalsIgnoreCase(method);
    }
}
