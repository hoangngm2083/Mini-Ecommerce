package org.example.miniecommerce.dto.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmedEvent(
        Long orderId,
        Long userId,
        BigDecimal amount,
        List<OrderItemDto> items
) {}
