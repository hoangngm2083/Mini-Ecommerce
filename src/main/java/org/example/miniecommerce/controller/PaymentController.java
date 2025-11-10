package org.example.miniecommerce.controller;

import org.example.miniecommerce.dto.payment.CreatePaymentRequest;
import org.example.miniecommerce.dto.payment.ConfirmPaymentRequest;
import org.example.miniecommerce.dto.payment.PaymentResponse;
import org.example.miniecommerce.entity.Payment;
import org.example.miniecommerce.mapper.PaymentMapper;
import org.example.miniecommerce.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService svc;

    public PaymentController(PaymentService svc) {
        this.svc = svc;
    }

    // Tạo payment mới cho một order
    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest req) {
        Payment p = svc.create(req);
        return ResponseEntity.status(201).body(PaymentMapper.toResponse(p));
    }

    // Xác nhận payment (ví dụ sau khi provider callback)
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirm(@Valid @RequestBody ConfirmPaymentRequest req) {
        Payment p = svc.confirm(req);
        return ResponseEntity.ok(PaymentMapper.toResponse(p));
    }
}
