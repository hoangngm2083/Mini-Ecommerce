package org.example.miniecommerce.dto.order;

import org.example.miniecommerce.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        Long userId,
        BigDecimal totalAmount,
        OrderStatus status,
        List<OrderItemDto> items,
        PaymentDto payment,
        ShipmentDto shipment
) {}
