package org.example.miniecommerce.dto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty List<CreateOrderItemDto> items,
        @NotNull CreateOrderPaymentDto payment,
        @NotNull CreateOrderShipmentDto shipment
) {}
