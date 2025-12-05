package org.example.miniecommerce.mapper;

import org.example.miniecommerce.dto.shipping.ShipmentResponse;
import org.example.miniecommerce.entity.Shipping;

public class ShippingMapper {
    public static ShipmentResponse toResponse(Shipping s) {
        return new ShipmentResponse(
                s.getId(),
                s.getOrderId(),
                s.getShippedById(),
                s.getAddress(),
                s.getMethod().name().toLowerCase(),
                s.getFee(),
                s.getStatus().name().toLowerCase(),
                s.getShippedAt(),
                s.getDeliveredAt(),
                s.getNotes(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}