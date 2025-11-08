package org.example.miniecommerce.dto.order;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty List<CreateOrderItemDto> items
) {}
