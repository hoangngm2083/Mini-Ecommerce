package org.example.miniecommerce.event;

import org.example.miniecommerce.entity.Shipping;

public record ShippingStatusUpdatedEvent(Shipping shipping) {
}
