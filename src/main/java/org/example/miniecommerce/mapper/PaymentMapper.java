package org.example.miniecommerce.mapper;

import org.example.miniecommerce.dto.payment.PaymentResponse;
import org.example.miniecommerce.entity.Payment;

public class PaymentMapper {
    public static PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrder() != null ? p.getOrder().getId() : null,
                p.getAmount(),
                p.getMethod(),
                p.getStatus().name()..toLowerCase(),
                p.getPaidAt(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}