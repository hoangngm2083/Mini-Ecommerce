package org.example.miniecommerce.service;

import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.builder.OrderBuilder;
import org.example.miniecommerce.dto.order.*;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.OrderStatus;
import org.example.miniecommerce.repository.OrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderBuilder orderBuilder;
    private final TotalCalculator totalCalculator;
    private final ApplicationEventPublisher eventPublisher;

    // POST /api/orders
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        Order order = orderBuilder.createOrder(userId, request.items());
        order = orderRepository.save(order);
        return mapToResponse(order);
    }

    // GET /api/orders/me
    public List<OrderResponse> getMyOrders(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET /api/orders/{id}
    public OrderResponse getOrderById(Long id, Long userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUserId()
                .equals(userId)) {
            throw new RuntimeException("You can only view your own orders");
        }
        return mapToResponse(order);
    }

    // PUT /api/orders/{id}/status
    public OrderStatusResponse updateStatus(Long id, UpdateOrderStatusRequest request, Long userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId()
                .equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        order.setStatus(request.status());
        order = orderRepository.save(order);

        // Gọi Payment & Shipping Service (Event-driven)
        if (request.status() == OrderStatus.PAID) {
            // Publish event: OrderConfirmedEvent → PaymentService, ShippingService
            eventPublisher.publishEvent(
                    new OrderConfirmedEvent(order.getId(), order.getUserId(), order.getTotalAmount(), order.getItems()
                            .stream()
                            .map(item -> new OrderItemDto(item.getProductId(), item.getQuantity(), item.getPrice()))
                            .toList()));
        }

        return new OrderStatusResponse(order.getId(), order.getStatus(), LocalDateTime.now());
    }

    // DELETE /api/orders/{id}
    public DeleteResponse deleteOrder(Long id, Long userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId()
                .equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be deleted");
        }

        orderRepository.delete(order);
        return new DeleteResponse("Order deleted");
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemDto> items = order.getItems()
                .stream()
                .map(item -> new OrderItemDto(item.getProductId(), item.getQuantity(), item.getPrice()))
                .toList();
        return new OrderResponse(order.getId(), order.getUserId(), order.getTotalAmount(), order.getStatus(), items);
    }
}
