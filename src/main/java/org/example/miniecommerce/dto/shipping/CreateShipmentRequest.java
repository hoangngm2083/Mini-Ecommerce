package org.example.miniecommerce.dto.shipping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateShipmentRequest(
    @NotNull Long orderId,
    Long shippedBy,
    @NotBlank String address,
    @NotBlank String method,
    String notes
) {}