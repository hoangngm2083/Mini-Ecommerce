package org.example.miniecommerce.service;

import org.example.miniecommerce.dto.shipping.CreateShipmentRequest;
import org.example.miniecommerce.dto.shipping.UpdateShipmentRequest;
import org.example.miniecommerce.entity.Shipping;

public interface ShippingService {
    Shipping create(CreateShipmentRequest req);
    Shipping update(Long id, UpdateShipmentRequest req, Long userId);
}