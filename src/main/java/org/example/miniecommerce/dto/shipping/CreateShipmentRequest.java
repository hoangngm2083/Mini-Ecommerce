package org.example.miniecommerce.dto.shipping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record CreateShipmentRequest(
    @NotNull Long orderId,
    Long shippedBy,
    @NotBlank String address,
    @NotBlank String method,
    @DecimalMin(value = "0.0") BigDecimal fee,
    String notes
) {}