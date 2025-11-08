package org.example.miniecommerce.controller;

import org.example.miniecommerce.dto.payment.ConfirmPaymentRequest;
import org.example.miniecommerce.dto.payment.CreatePaymentRequest;
import org.example.miniecommerce.dto.payment.PaymentResponse;
import org.example.miniecommerce.entity.Payment;
import org.example.miniecommerce.resources.PaymentResource;
import org.example.miniecommerce.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService svc;

    public PaymentController(PaymentService svc) { this.svc = svc; }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest req) {
        Payment p = svc.create(req);
        return ResponseEntity.status(201).body(PaymentResource.toResponse(p));
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirm(@Valid @RequestBody ConfirmPaymentRequest req) {
        Payment p = svc.confirm(req);
        return ResponseEntity.ok(PaymentResource.toResponse(p));
    }
}