package org.example.miniecommerce.controller;

import org.example.miniecommerce.dto.shipping.CreateShipmentRequest;
import org.example.miniecommerce.dto.shipping.ShipmentResponse;
import org.example.miniecommerce.dto.shipping.UpdateShipmentRequest;
import org.example.miniecommerce.entity.Shipping;
import org.example.miniecommerce.resources.ShippingResource;
import org.example.miniecommerce.service.ShippingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/shipments")
public class ShippingController {

    private final ShippingService svc;

    public ShippingController(ShippingService svc) { this.svc = svc; }

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody CreateShipmentRequest req) {
        Shipping s = svc.create(req);
        return ResponseEntity.status(201).body(ShippingResource.toResponse(s));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ShipmentResponse> update(@PathVariable Long id, @RequestBody UpdateShipmentRequest req) {
        Shipping s = svc.update(id, req);
        return ResponseEntity.ok(ShippingResource.toResponse(s));
    }
}