package org.example.miniecommerce.dto.order;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderShipmentDto(
        @NotBlank String address,
        String notes,
        @NotBlank String method
) {}
