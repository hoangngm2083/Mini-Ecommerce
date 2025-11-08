package org.example.miniecommerce.dto.order;

import java.math.BigDecimal;

public record OrderItemDto(
        Long productId,
        Integer quantity,
        BigDecimal price
) {}
