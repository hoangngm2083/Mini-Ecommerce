package org.example.miniecommerce.dto.shipping;

public record UpdateShipmentRequest(
        UpdateShipmentType type,
        Long shippedBy,     // for ASSIGN_TASK
        String status       // for both types
) {
}