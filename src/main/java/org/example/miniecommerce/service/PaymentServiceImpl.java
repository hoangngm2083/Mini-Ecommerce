package org.example.miniecommerce.service;

import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.dto.payment.ConfirmPaymentRequest;
import org.example.miniecommerce.dto.payment.CreatePaymentRequest;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.Payment;
import org.example.miniecommerce.factory.PaymentFactory;
import org.example.miniecommerce.repository.PaymentRepository;
import org.example.miniecommerce.mediator.OrderProcessingMediator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repo;
    private final OrderLookupService orderLookup;
    private final OrderProcessingMediator mediator;

    @Override
    @Transactional
    public Payment create(CreatePaymentRequest req) {
        Order order = orderLookup.findByIdOrThrow(req.orderId());
        Payment p = PaymentFactory.fromCreateRequest(req, order);

        repo.save(p);

        // Sau khi save, query lại để lấy Payment mới nhất
        return repo.findByOrderId(order.getId())
                .stream()
                .reduce((first, second) -> second)
                .orElse(p);
    }

    @Override
    @Transactional
    public Payment confirm(ConfirmPaymentRequest req) {
        // Add log.info for debugging NullPointerException
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentServiceImpl.class);
        log.info("[confirm] req.paymentId = {}, req.success = {}", req.paymentId(), req.success());

        Payment p = null;
        try {
            p = repo.findById(req.paymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
            log.info("[confirm] Payment found: id = {}, status = {}, amount = {}, method = {}", p.getId(),
                    p.getStatus(), p.getAmount(), p.getPaymentMethod());
        } catch (Exception e) {
            log.error("[confirm] Error retrieving Payment for id {}: {}", req.paymentId(), e.getMessage());
            throw e;
        }

        if (p.getStatus() == Payment.Status.SUCCESS && req.success()) {
            log.info("[confirm] Payment already successful, returning existing Payment");
            return p;
        }

        try {
            if (req.success()) {
                p.setStatus(Payment.Status.SUCCESS);
                p.setPaidAt(LocalDateTime.now());

                // Notify mediator to update order status
                mediator.notifyPaymentConfirmed(p, true);

                log.info("[confirm] Payment confirmed successfully: paymentId = {}, orderId = {}", p.getId(), p.getOrderId());
            } else {
                p.setStatus(Payment.Status.FAILED);
                mediator.notifyPaymentConfirmed(p, false);
            }
            log.info("[confirm] After status update: status = {}, paidAt = {}", p.getStatus(), p.getPaidAt());
        } catch (Exception e) {
            log.error("[confirm] Error updating payment/order status: {}", e.getMessage());
            throw e;
        }

        try {
            repo.update(p);
            log.info("[confirm] Payment updated successfully: id = {}", p.getId());
        } catch (Exception e) {
            log.error("[confirm] Error updating Payment: {}", e.getMessage());
            throw e;
        }

        return p;
    }
}
