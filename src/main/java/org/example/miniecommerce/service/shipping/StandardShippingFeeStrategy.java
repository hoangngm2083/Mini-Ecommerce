package org.example.miniecommerce.service.shipping;

import org.example.miniecommerce.entity.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy Pattern: Chiến lược tính phí ship tiêu chuẩn
 */
@Component
public class StandardShippingFeeStrategy implements ShippingFeeStrategy {

    private static final BigDecimal STANDARD_FEE = new BigDecimal("30000");

    @Override
    public BigDecimal calculateFee(Order order) {
        // Phí ship tiêu chuẩn cố định
        return STANDARD_FEE;
    }

    @Override
    public boolean supports(String method) {
        return "STANDARD".equalsIgnoreCase(method);
    }
}
