package org.example.miniecommerce.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDto(
        Long id,
        Long orderId,
        BigDecimal amount,
        String paymentMethod,
        String status,
        String failureReason,
        String transactionId,
        LocalDateTime paidAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
