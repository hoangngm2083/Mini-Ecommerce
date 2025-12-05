package org.example.miniecommerce.service.shipping;

import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.entity.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strategy Pattern: Calculator quản lý các chiến lược tính phí ship
 */
@Service
@RequiredArgsConstructor
public class ShippingFeeCalculator {

    private final List<ShippingFeeStrategy> strategies;

    /**
     * Tính phí ship dựa trên method và order
     * @param method Phương thức ship
     * @param order Đơn hàng
     * @return Phí ship
     */
    public BigDecimal calculateFee(String method, Order order) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(method))
                .findFirst()
                .map(strategy -> strategy.calculateFee(order))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported shipping method: " + method));
    }
}
