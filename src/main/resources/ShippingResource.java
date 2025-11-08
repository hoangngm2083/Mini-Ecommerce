package org.example.miniecommerce.resources;

import org.example.miniecommerce.dto.shipping.ShipmentResponse;
import org.example.miniecommerce.entity.Shipping;

import java.time.LocalDateTime;

public class ShippingResource {

    public static ShipmentResponse toResponse(Shipping s) {
        Long orderId = s.getOrder() != null ? s.getOrder().getId() : null;
        LocalDateTime created = null;
        LocalDateTime updated = null;
        try {
            created = (LocalDateTime) s.getCreatedAt();
            updated = (LocalDateTime) s.getUpdatedAt();
        } catch (Exception e) { /* ignore */ }

        return new ShipmentResponse(
                s.getId(),
                orderId,
                s.getAddress(),
                s.getCity(),
                s.getPostalCode(),
                s.getCountry(),
                s.getStatus().name(),
                created,
                updated
        );
    }
}