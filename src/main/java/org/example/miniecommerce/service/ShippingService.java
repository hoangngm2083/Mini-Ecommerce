package org.example.miniecommerce.service;

import org.example.miniecommerce.dto.shipping.CreateShipmentRequest;
import org.example.miniecommerce.dto.shipping.UpdateShipmentRequest;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.Shipping;
import org.example.miniecommerce.factory.ShippingFactory;
import org.example.miniecommerce.repository.ShippingRepository;
import org.example.miniecommerce.service.order.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShippingService {

    private final ShippingRepository shippingRepository;
    private final OrderLookupService orderLookupService;
    private final OrderService orderService;

    public ShippingService(ShippingRepository shippingRepository,
                           OrderLookupService orderLookupService,
                           OrderService orderService) {
        this.shippingRepository = shippingRepository;
        this.orderLookupService = orderLookupService;
        this.orderService = orderService;
    }

    @Transactional
    public Shipping create(CreateShipmentRequest req) {
        Order order = orderLookupService.findByIdOrThrow(req.orderId());
        Shipping s = ShippingFactory.fromCreateRequest(req, order);
        return shippingRepository.save(s);
    }

    @Transactional
    public Shipping update(Long id, UpdateShipmentRequest req) {
        Shipping s = shippingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Shipment not found"));
        ShippingFactory.applyUpdate(s, req.status(), req.address(), req.city(), req.postalCode(), req.country());
        Shipping saved = shippingRepository.save(s);
        if (saved.getStatus() == Shipping.Status.DELIVERED) {
            orderService.markDelivered(saved.getOrder().getId());
        }
        return saved;
    }
}