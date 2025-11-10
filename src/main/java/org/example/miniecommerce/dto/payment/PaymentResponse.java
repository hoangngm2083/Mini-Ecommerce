package org.example.miniecommerce.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
    Long id,
    Long orderId,
    BigDecimal amount,
    String method,
    String status,
    LocalDateTime paidAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}