package org.example.miniecommerce.service.order;

import org.example.miniecommerce.dto.order.*;
import org.example.miniecommerce.service.order.decorator.OrderDecoratorName;

import java.math.BigDecimal;
import java.util.List;


public interface OrderService {
    // POST /api/orders
    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    // GET /api/orders/me
    List<OrderResponse> getMyOrders(Long userId);

    // GET /api/orders/{id}
    OrderResponse getOrderById(Long id, Long userId);

    OrderResponse getOrderById(Long id);

    // GET /api/orders/{id}/detail - New endpoint for order with payment and shipment details
    OrderDetailResponse getOrderDetailById(Long id, Long userId);

    // PUT /api/orders/{id}/status
    OrderStatusResponse updateStatus(Long id, UpdateOrderStatusRequest request, Long userId);

    // State pattern methods - thay thế cho updateStatus(Long id, OrderStatus status)
    void handlePaymentSuccess(Long orderId);
    void handleShipmentCreated(Long orderId);
    void handleShipmentStarted(Long orderId);
    void handleShipmentDelivered(Long orderId);
    void handlePaymentFailed(Long orderId);
    void handleCancel(Long orderId);

    // DELETE /api/orders/{id}
    DeleteResponse deleteOrder(Long id, Long userId);

    void addFee(Long orderId, OrderDecoratorName name, BigDecimal fee);


}
