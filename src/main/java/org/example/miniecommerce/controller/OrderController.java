package org.example.miniecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.dto.order.*;
import org.example.miniecommerce.service.AuthService;
import org.example.miniecommerce.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService; // Lấy userId từ token

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request,
            @RequestHeader String userId) {
        OrderResponse response = orderService.createOrder(Long.parseLong(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@RequestHeader String userId) {
        return ResponseEntity.ok(orderService.getMyOrders(Long.parseLong(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id, @RequestHeader String userId) {
        return ResponseEntity.ok(orderService.getOrderById(id, Long.parseLong(userId)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderStatusResponse> updateStatus(@PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request, @RequestHeader String userId) {
        return ResponseEntity.ok(orderService.updateStatus(id, request, Long.parseLong(userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> deleteOrder(@PathVariable Long id, @RequestHeader String userId) {
        return ResponseEntity.ok(orderService.deleteOrder(id, Long.parseLong(userId)));
    }
}
