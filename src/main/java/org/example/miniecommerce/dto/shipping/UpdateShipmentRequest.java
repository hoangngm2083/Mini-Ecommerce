package org.example.miniecommerce.dto.shipping;

public record UpdateShipmentRequest(
        UpdateShipmentType type,
        String status       // for both types - shippedBy is taken from header userId
) {
}