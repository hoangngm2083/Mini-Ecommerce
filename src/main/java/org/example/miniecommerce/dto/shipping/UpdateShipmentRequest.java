package org.example.miniecommerce.dto.shipping;
public record UpdateShipmentRequest(
    String status,
    String address,
    String city,
    String postalCode,
    String country
) {}