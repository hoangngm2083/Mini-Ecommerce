package org.example.miniecommerce.dto.shipping;

import java.math.BigDecimal;

public record UpdateShipmentRequest(Long shippedBy, String method, BigDecimal fee, String status, String address, String notes) {
}