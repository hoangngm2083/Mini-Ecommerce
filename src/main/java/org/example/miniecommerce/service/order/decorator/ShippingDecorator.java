package org.example.miniecommerce.service.order.decorator;

import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.OrderStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Decorator Pattern: Thêm tính năng giao hàng nhanh
 */
@Service
public class ShippingDecorator extends OrderDecorator {

    public ShippingDecorator() {
        this.name = OrderDecoratorName.SHIPPING;
    }


    @Override
    public Order apply(Order order, BigDecimal fee) {
        BigDecimal newTotal = order.getTotalAmount()
                .add(fee);
        order.setTotalAmount(newTotal);
        return order;
    }


    @Override
    public boolean canApply(Order order) {
        // Chỉ áp dụng cho đơn hàng chưa được ship
        return order.getStatus() == OrderStatus.CREATED || order.getStatus() == OrderStatus.PENDING_SHIPMENT;
    }
}
