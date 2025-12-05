package org.example.miniecommerce.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShipmentDto(
        Long id,
        Long orderId,
        String address,
        String method,
        BigDecimal fee,
        String status,
        String notes,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
