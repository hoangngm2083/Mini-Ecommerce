package org.example.miniecommerce.service.shipping;

import org.example.miniecommerce.entity.Order;

import java.math.BigDecimal;

/**
 * Strategy Pattern: Interface định nghĩa cách tính phí ship
 */
public interface ShippingFeeStrategy {

    /**
     * Tính phí ship dựa trên đơn hàng
     * @param order Đơn hàng cần tính phí ship
     * @return Phí ship
     */
    BigDecimal calculateFee(Order order);

    /**
     * Kiểm tra xem strategy này có áp dụng cho method ship không
     * @param method Phương thức ship (STANDARD, EXPRESS, etc.)
     * @return true nếu áp dụng được
     */
    boolean supports(String method);
}
