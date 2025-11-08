package org.example.miniecommerce.controller;

import org.example.miniecommerce.dto.shipping.CreateShipmentRequest;
import org.example.miniecommerce.dto.shipping.ShipmentResponse;
import org.example.miniecommerce.dto.shipping.UpdateShipmentRequest;
import org.example.miniecommerce.entity.Shipping;
import org.example.miniecommerce.mapper.ShippingMapper;
import org.example.miniecommerce.service.ShippingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
public class ShippingController {

    private final ShippingService svc;

    public ShippingController(ShippingService svc) {
        this.svc = svc;
    }

    // Tạo shipment mới cho một order
    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody CreateShipmentRequest req) {
        Shipping s = svc.create(req);
        return ResponseEntity.status(201).body(ShippingMapper.toResponse(s));
    }

    // Cập nhật shipment (ví dụ thay đổi status sang DELIVERED)
    @PatchMapping("/{id}")
    public ResponseEntity<ShipmentResponse> update(@PathVariable Long id,
                                                   @RequestBody UpdateShipmentRequest req) {
        Shipping s = svc.update(id, req);
        return ResponseEntity.ok(ShippingMapper.toResponse(s));
    }
}