package org.example.miniecommerce.dto.order;

import org.example.miniecommerce.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderStatusResponse(
        Long id,
        OrderStatus status,
        LocalDateTime updatedAt
) {}
