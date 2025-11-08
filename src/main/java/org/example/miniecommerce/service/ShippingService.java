package org.example.miniecommerce.service;

import org.example.miniecommerce.dto.shipping.CreateShipmentRequest;
import org.example.miniecommerce.dto.shipping.UpdateShipmentRequest;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.Shipping;
import org.example.miniecommerce.factory.ShippingFactory;
import org.example.miniecommerce.repository.ShippingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShippingService {

    private final ShippingRepository repo;
    private final OrderLookupService orderLookup;

    public ShippingService(ShippingRepository repo, OrderLookupService orderLookup) {
        this.repo = repo;
        this.orderLookup = orderLookup;
    }

    @Transactional
    public Shipping create(CreateShipmentRequest req) {
        Order order = orderLookup.findByIdOrThrow(req.orderId());
        Shipping s = ShippingFactory.fromCreateRequest(req, order);
        return repo.save(s);
    }

    @Transactional
    public Shipping update(Long id, UpdateShipmentRequest req) {
        Shipping s = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));
        ShippingFactory.applyUpdate(s, req.status(), req.address(), req.city(), req.postalCode(), req.country());
        return repo.save(s);
    }
}