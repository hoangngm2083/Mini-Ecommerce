package org.example.miniecommerce.mapper;

import org.example.miniecommerce.dto.shipping.ShipmentResponse;
import org.example.miniecommerce.entity.Shipping;

public class ShippingMapper {
    public static ShipmentResponse toResponse(Shipping s) {
        return new ShipmentResponse(
                s.getId(),
                s.getOrder() != null ? s.getOrder().getId() : null,
                s.getAddress(),
                s.getCity(),
                s.getPostalCode(),
                s.getCountry(),
                s.getStatus().name().toLowerCase(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}