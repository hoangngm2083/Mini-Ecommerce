package org.example.miniecommerce.dto.shipping;

import java.time.LocalDateTime;

public record ShipmentResponse(
    Long id,
    Long orderId,
    String address,
    String city,
    String postalCode,
    String country,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}