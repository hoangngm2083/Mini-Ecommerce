package org.example.miniecommerce.factory;

import org.example.miniecommerce.dto.shipping.CreateShipmentRequest;
import org.example.miniecommerce.dto.shipping.UpdateShipmentRequest;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.Shipping;
import org.example.miniecommerce.entity.User;

import java.time.LocalDateTime;

public class ShippingFactory {

    public static Shipping fromCreateRequest(CreateShipmentRequest req, Order order, User shippedBy) {
        Shipping s = new Shipping();
        s.setOrder(order);
        s.setOrderId(order.getId());
        s.setShippedBy(shippedBy);
        if (shippedBy != null) {
            s.setShippedById(shippedBy.getId());
        }
        s.setAddress(req.address());
        s.setMethod(Shipping.Method.valueOf(req.method().toUpperCase()));
        s.setFee(req.fee());
        s.setNotes(req.notes());
        s.setStatus(Shipping.Status.CREATED);
        return s;
    }

    public static void applyUpdate(Shipping s, UpdateShipmentRequest req, User shippedBy) {
        if (shippedBy != null) {
            s.setShippedBy(shippedBy);
            s.setShippedById(shippedBy.getId());
        }
        if (req.method() != null) {
            Shipping.Method newMethod = Shipping.Method.valueOf(req.method().toUpperCase());
            s.setMethod(newMethod);
        }
        if (req.fee() != null) s.setFee(req.fee());
        if (req.status() != null) {
            Shipping.Status newStatus = Shipping.Status.valueOf(req.status().toUpperCase());
            s.setStatus(newStatus);

            if (newStatus == Shipping.Status.SHIPPING && s.getShippedAt() == null) {
                s.setShippedAt(LocalDateTime.now());
            } else if (newStatus == Shipping.Status.COMPLETED && s.getDeliveredAt() == null) {
                s.setDeliveredAt(LocalDateTime.now());
            }

        }
        if (req.address() != null) s.setAddress(req.address());
        if (req.notes() != null) s.setNotes(req.notes());
    }
}