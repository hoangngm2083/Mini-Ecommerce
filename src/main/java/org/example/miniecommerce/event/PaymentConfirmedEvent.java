package org.example.miniecommerce.event;

import org.example.miniecommerce.entity.Payment;

public record PaymentConfirmedEvent(Payment payment, boolean success) {
}
