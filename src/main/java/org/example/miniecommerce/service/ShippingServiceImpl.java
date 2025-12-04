package org.example.miniecommerce.service;

import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.dto.shipping.CreateShipmentRequest;
import org.example.miniecommerce.dto.shipping.UpdateShipmentRequest;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.Shipping;
import org.example.miniecommerce.entity.User;
import org.example.miniecommerce.factory.ShippingFactory;
import org.example.miniecommerce.repository.ShippingRepository;
import org.example.miniecommerce.repository.UserRepository;
import org.example.miniecommerce.service.order.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingRepository repo;
    private final OrderLookupService orderLookup;
    private final OrderService orderService;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public Shipping create(CreateShipmentRequest req) {
        Order order = orderLookup.findByIdOrThrow(req.orderId());
        User shippedBy = null;
        if (req.shippedBy() != null) {
            shippedBy = userRepository.findById(req.shippedBy())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }
        Shipping s = ShippingFactory.fromCreateRequest(req, order, shippedBy);
        orderService.handleShipmentCreated(order.getId());
        repo.save(s);

        // Sau khi save, query lại để lấy Shipping mới nhất
        return repo.findByOrderId(order.getId())
                .stream()
                .reduce((first, second) -> second)
                .orElse(s);
    }

    @Override
    @Transactional
    public Shipping update(Long id, UpdateShipmentRequest req) {
        Shipping s = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found"));

        User shippedBy = null;
        if (req.shippedBy() != null) {
            shippedBy = userRepository.findById(req.shippedBy())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }

        ShippingFactory.applyUpdate(s, req, shippedBy);

        switch (s.getStatus()) {
            case SHIPPING:
                orderService.handleShipmentStarted(s.getOrderId());
                break;
            case COMPLETED:
                orderService.handleShipmentDelivered(s.getOrderId());
                break;
            default:
                // No action needed for other shipping statuses
                break;
        }

        repo.update(s);

        return s;
    }
}
