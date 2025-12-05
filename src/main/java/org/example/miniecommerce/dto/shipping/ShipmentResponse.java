package org.example.miniecommerce.dto.shipping;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShipmentResponse(
    Long id,
    Long orderId,
    Long shippedBy,
    String address,
    String method,
    BigDecimal fee,
    String status,
    LocalDateTime shippedAt,
    LocalDateTime deliveredAt,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}