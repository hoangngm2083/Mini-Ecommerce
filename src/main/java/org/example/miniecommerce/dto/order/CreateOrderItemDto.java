package org.example.miniecommerce.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderItemDto(
        @NotNull Long productId,
        @Min(1) Integer quantity,
        @NotNull BigDecimal price
) {}
