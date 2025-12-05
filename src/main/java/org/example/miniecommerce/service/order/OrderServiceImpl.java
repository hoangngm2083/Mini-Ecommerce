package org.example.miniecommerce.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.miniecommerce.dto.order.*;
import org.example.miniecommerce.dto.payment.CreatePaymentRequest;
import org.example.miniecommerce.dto.shipping.CreateShipmentRequest;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.OrderStatus;
import org.example.miniecommerce.repository.OrderRepository;
import org.example.miniecommerce.service.PaymentService;
import org.example.miniecommerce.service.ShippingService;
import org.example.miniecommerce.service.UserService;
import org.example.miniecommerce.service.order.decorator.OrderDecoratorManager;
import org.example.miniecommerce.service.order.decorator.OrderDecoratorName;
import org.example.miniecommerce.service.order.template.StandardCreateOrderProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final StandardCreateOrderProcessor orderProcessor;
    private final OrderDecoratorManager decoratorManager;
    private final UserService userService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;


    // POST /api/orders - Enhanced with Template Method Pattern
    @Override
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        validateUserExists(userId);
        // Use Template Method Pattern for order processing
        Order order = orderProcessor.processOrder(userId, request);

        // Create payment for the order
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                order.getId(),
                request.payment().method()
        );
        paymentService.create(paymentRequest);

        // Create shipment for the order
        CreateShipmentRequest shipmentRequest = new CreateShipmentRequest(
                order.getId(),
                null, // shippedBy will be set later when assigned
                request.shipment().address(),
                request.shipment().method(),
                BigDecimal.ZERO, // fee will be calculated later
                request.shipment().notes()
        );
        shippingService.create(shipmentRequest);

        return mapToResponse(order);
    }
    @Override
    public void addFee(Long orderId, OrderDecoratorName name, BigDecimal fee) {

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }

        Order order = decoratorManager.applyDecorator(orderOpt.get(), name, fee);

        orderRepository.save(order);
    }

    // GET /api/orders/me
    @Override
    public List<OrderResponse> getMyOrders(Long userId) {
        // Validate user exists
        validateUserExists(userId);
        
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET /api/orders/{id}
    @Override
    public OrderResponse getOrderById(Long id, Long userId) {
        // Validate user exists
        validateUserExists(userId);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUserId()
                .equals(userId)) {
            throw new RuntimeException("You can only view your own orders");
        }
        return mapToResponse(order);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return mapToResponse(order);
    }

    // PUT /api/orders/{id}/status - Enhanced with State Pattern
    @Override
    public OrderStatusResponse updateStatus(Long id, UpdateOrderStatusRequest request, Long userId) {
        // Validate user exists
        validateUserExists(userId);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId()
                .equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        order.setStatus(request.status());
        orderRepository.save(order);
        return new OrderStatusResponse(order.getId(), order.getStatus(), LocalDateTime.now());
    }

    // State pattern methods - thay thế cho updateStatus(Long id, OrderStatus status)
    @Override
    public void handlePaymentSuccess(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.handlePaymentSuccess();
        orderRepository.save(order);
    }

    @Override
    public void handleShipmentCreated(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.handleShipmentCreated();
        orderRepository.save(order);
    }

    @Override
    public void handleShipmentStarted(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.handleShipmentStarted();
        orderRepository.save(order);
    }

    @Override
    public void handleShipmentDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.handleShipmentDelivered();
        orderRepository.save(order);
    }

    @Override
    public void handlePaymentFailed(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.handlePaymentFailed();
        orderRepository.save(order);
    }

    @Override
    public void handleCancel(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.handleCancel();
        orderRepository.save(order);
    }

    // DELETE /api/orders/{id}
    @Override
    public DeleteResponse deleteOrder(Long id, Long userId) {
        // Validate user exists
        validateUserExists(userId);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId()
                .equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalStateException("Only created orders can be deleted");
        }

        orderRepository.deleteById(id);
        return new DeleteResponse("Order deleted");
    }



    private OrderResponse mapToResponse(Order order) {
        List<OrderItemDto> items = order.getItems()
                .stream()
                .map(item -> new OrderItemDto(item.getProductId(), item.getQuantity(), item.getPrice()))
                .toList();
        return new OrderResponse(order.getId(), order.getUserId(), order.getTotalAmount(), order.getStatus(), items);
    }


    private void validateUserExists(Long userId) {
        try {
            userService.getUserId(String.valueOf(userId));
            log.debug("User with id {} exists in database", userId);
        } catch (RuntimeException e) {
            log.error("User with id {} does not exist in database", userId);
            throw new IllegalArgumentException("User with id " + userId + " does not exist", e);
        }
    }
}
