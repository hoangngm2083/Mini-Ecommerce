package org.example.miniecommerce.factory;

import org.example.miniecommerce.dto.shipping.CreateShipmentRequest;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.OrderStatus;
import org.example.miniecommerce.entity.Shipping;

public class ShippingFactory {
    public static Shipping fromCreateRequest(CreateShipmentRequest req, Order order) {
        Shipping s = new Shipping();
        s.setOrder(order);
        s.setAddress(req.address());
        s.setCity(req.city());
        s.setPostalCode(req.postalCode());
        s.setCountry(req.country());
        s.setStatus(Shipping.Status.PENDING);
        return s;
    }

    public static void applyUpdate(Shipping s, String status, String address, String city, String postalCode, String country) {
        if (status != null) {
            Shipping.Status newStatus = Shipping.Status.valueOf(status);
            s.setStatus(newStatus);
            if (newStatus == Shipping.Status.SHIPPED) {
                s.getOrder().setStatus(OrderStatus.SHIPPED);
            }
            else if (newStatus == Shipping.Status.DELIVERED) {
                s.getOrder().setStatus(OrderStatus.DELIVERED);
            }
        }
        if (address != null) s.setAddress(address);
        if (city != null) s.setCity(city);
        if (postalCode != null) s.setPostalCode(postalCode);
        if (country != null) s.setCountry(country);
    }
}