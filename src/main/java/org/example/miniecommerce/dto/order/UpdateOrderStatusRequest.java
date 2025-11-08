package org.example.miniecommerce.dto.order;

import jakarta.validation.constraints.NotNull;
import org.example.miniecommerce.entity.OrderStatus;

public record UpdateOrderStatusRequest(
        @NotNull OrderStatus status
) {}