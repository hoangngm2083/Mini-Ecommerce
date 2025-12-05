package org.example.miniecommerce.factory;

import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.dto.shipping.CreateShipmentRequest;
import org.example.miniecommerce.dto.shipping.UpdateShipmentRequest;
import org.example.miniecommerce.dto.shipping.UpdateShipmentType;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.Shipping;
import org.example.miniecommerce.entity.User;
import org.example.miniecommerce.service.shipping.ShippingFeeCalculator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ShippingFactory {

    private final ShippingFeeCalculator shippingFeeCalculator;

    public Shipping fromCreateRequest(CreateShipmentRequest req, Order order, User shippedBy) {
        Shipping s = new Shipping();
        s.setOrder(order);
        s.setOrderId(order.getId());
        s.setShippedBy(shippedBy);
        if (shippedBy != null) {
            s.setShippedById(shippedBy.getId());
        }
        s.setAddress(req.address());
        s.setMethod(Shipping.Method.valueOf(req.method()
                .toUpperCase()));

        // Tự động tính phí ship dựa trên method và order
        s.setFee(shippingFeeCalculator.calculateFee(req.method(), order));

        s.setNotes(req.notes());
        s.setStatus(Shipping.Status.CREATED);
        return s;
    }

    public static void applyUpdate(Shipping s, UpdateShipmentRequest req, User shippedBy) {
        if (req.type() == UpdateShipmentType.ASSIGN_TASK) {

            // Khi nhân viên nhận task: update shipped_by, status, shipped_at
            s.setShippedBy(shippedBy);
            s.setShippedById(shippedBy.getId());
            if (req.status() != null) {
                Shipping.Status newStatus = Shipping.Status.valueOf(req.status()
                        .toUpperCase());
                s.setStatus(newStatus);
                if (newStatus == Shipping.Status.SHIPPING && s.getShippedAt() == null) {
                    s.setShippedAt(LocalDateTime.now());
                }
            }
        } else if (req.type() == UpdateShipmentType.COMPLETE_TASK) {
            // Validate that the user completing task is the one assigned
            if (s.getShippedById() == null || !s.getShippedById()
                    .equals(shippedBy.getId())) {
                throw new IllegalArgumentException("Chỉ nhân viên được assign mới có thể hoàn thành task!");
            }

            // Khi nhân viên hoàn thành task: update status, delivered_at
            if (req.status() != null) {
                Shipping.Status newStatus = Shipping.Status.valueOf(req.status()
                        .toUpperCase());
                s.setStatus(newStatus);
                if (newStatus == Shipping.Status.COMPLETED && s.getDeliveredAt() == null) {
                    s.setDeliveredAt(LocalDateTime.now());
                }
            }
        }

    }
}