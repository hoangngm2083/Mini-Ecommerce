package org.example.miniecommerce.dto.payment;

import jakarta.validation.constraints.NotNull;

public record ConfirmPaymentRequest(
    @NotNull Long paymentId,
    @NotNull Boolean success
) {}