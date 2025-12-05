package org.example.miniecommerce.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.example.miniecommerce.dto.shipping.CreateShipmentRequest;
import org.example.miniecommerce.dto.shipping.ShipmentResponse;
import org.example.miniecommerce.dto.shipping.UpdateShipmentRequest;
import org.example.miniecommerce.entity.Shipping;
import org.example.miniecommerce.mapper.ShippingMapper;
import org.example.miniecommerce.service.ShippingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
public class ShippingController {

    private final ShippingService svc;

    public ShippingController(ShippingService svc) {
        this.svc = svc;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody CreateShipmentRequest req) {
        Shipping s = svc.create(req);
        return ResponseEntity.status(201)
                .body(ShippingMapper.toResponse(s));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ShipmentResponse> update(@PathVariable Long id,
            @RequestBody UpdateShipmentRequest req,
            @RequestHeader @NotBlank String userId) {

        Shipping s = svc.update(id, req, Long.parseLong(userId));
        return ResponseEntity.ok(ShippingMapper.toResponse(s));
    }

    @GetMapping("/fee")
    public ResponseEntity<?> getShippingFee(@RequestParam String method) {
        java.math.BigDecimal fee = svc.getFeeByMethod(method);
        return ResponseEntity.ok(java.util.Map.of(
            "method", method,
            "fee", fee
        ));
    }
}