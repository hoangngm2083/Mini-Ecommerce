package org.example.miniecommerce.mediator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.Payment;
import org.example.miniecommerce.entity.Shipping;
import org.example.miniecommerce.repository.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProcessingMediatorImpl implements OrderProcessingMediator {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void notifyPaymentConfirmed(Payment payment, boolean success) {
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + payment.getOrderId()));

        if (success) {
            order.handlePaymentSuccess();
            log.info("Order status updated to PAID after payment confirmation: orderId = {}", payment.getOrderId());
        } else {
            order.handlePaymentFailed();
            log.info("Order status updated to CANCELLED after payment failed: orderId = {}", payment.getOrderId());
        }

        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void notifyShipmentCreated(Shipping shipping) {
        Order order = orderRepository.findById(shipping.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + shipping.getOrderId()));

        order.handleShipmentCreated();
        orderRepository.save(order);
        log.info("Order status updated to PROCESSING after shipment created: orderId = {}", shipping.getOrderId());
    }

    @Override
    @Transactional
    public void notifyShipmentUpdated(Shipping shipping) {
        Order order = orderRepository.findById(shipping.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + shipping.getOrderId()));

        switch (shipping.getStatus()) {
            case SHIPPING:
                order.handleShipmentStarted();
                log.info("Order status updated to SHIPPING: orderId = {}", shipping.getOrderId());
                break;
            case COMPLETED:
                order.handleShipmentDelivered();
                log.info("Order status updated to DELIVERED: orderId = {}", shipping.getOrderId());
                break;
            default:
                // No action needed for other shipping statuses
                return;
        }

        orderRepository.save(order);
    }
}
