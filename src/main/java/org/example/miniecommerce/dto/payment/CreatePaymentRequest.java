package org.example.miniecommerce.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record CreatePaymentRequest(
    @NotNull Long orderId,
    @NotBlank String method,
    @NotNull @DecimalMin(value = "0.0") BigDecimal amount
) {}