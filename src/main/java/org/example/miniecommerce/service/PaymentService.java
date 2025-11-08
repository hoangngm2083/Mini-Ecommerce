package org.example.miniecommerce.service;

import org.example.miniecommerce.dto.payment.CreatePaymentRequest;
import org.example.miniecommerce.dto.payment.ConfirmPaymentRequest;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.Payment;
import org.example.miniecommerce.factory.PaymentFactory;
import org.example.miniecommerce.repository.PaymentRepository;
import org.example.miniecommerce.service.order.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final OrderLookupService orderLookupService; // helper to load Order by id (or use OrderRepository)

    public PaymentService(PaymentRepository paymentRepository,
                          OrderService orderService,
                          OrderLookupService orderLookupService) {
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
        this.orderLookupService = orderLookupService;
    }

    @Transactional
    public Payment create(CreatePaymentRequest req) {
        Order order = orderLookupService.findByIdOrThrow(req.orderId());
        Payment payment = PaymentFactory.fromCreateRequest(req, order);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment confirm(ConfirmPaymentRequest req) {
        Optional<Payment> opt = paymentRepository.findById(req.paymentId());
        Payment p = opt.orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        // idempotency: if already PAID and success -> return
        if (p.getStatus() == Payment.Status.PAID && req.success()) return p;
        PaymentFactory.applyConfirm(p, req.success());
        Payment saved = paymentRepository.save(p);
        if (saved.getStatus() == Payment.Status.PAID) {
            // notify order service
            orderService.markPaid(saved.getOrder().getId());
        }
        return saved;
    }
}