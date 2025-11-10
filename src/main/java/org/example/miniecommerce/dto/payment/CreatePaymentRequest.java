package org.example.miniecommerce.dto.payment;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(
    @NotNull Long orderId,
    @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
    @NotBlank String method
) {}