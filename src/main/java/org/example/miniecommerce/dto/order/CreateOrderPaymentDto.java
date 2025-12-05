package org.example.miniecommerce.dto.order;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderPaymentDto(
        @NotBlank String method
) {}
