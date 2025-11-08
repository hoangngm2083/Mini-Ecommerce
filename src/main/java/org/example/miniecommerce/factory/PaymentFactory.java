package org.example.miniecommerce.factory;

import org.example.miniecommerce.dto.payment.CreatePaymentRequest;
import org.example.miniecommerce.dto.payment.ConfirmPaymentRequest;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.Payment;

import java.time.LocalDateTime;

public class PaymentFactory {

    public static Payment fromCreateRequest(CreatePaymentRequest req, Order order) {
        Payment p = new Payment();
        // assume BaseEntity id generated elsewhere (e.g. @GeneratedValue)
        p.setOrder(order);
        p.setAmount(req.amount());
        p.setMethod(req.method());
        p.setStatus(Payment.Status.PENDING);
        // paidAt remains null until confirmed
        return p;
    }

    public static Payment applyConfirm(Payment p, boolean success) {
        if (success) {
            p.setStatus(Payment.Status.PAID);
            p.setPaidAt(LocalDateTime.now());
        } else {
            p.setStatus(Payment.Status.FAILED);
        }
        return p;
    }
}