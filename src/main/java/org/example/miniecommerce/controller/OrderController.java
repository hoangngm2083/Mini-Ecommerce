package org.example.miniecommerce.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.dto.order.*;
import org.example.miniecommerce.service.order.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody @NotNull CreateOrderRequest request,
            @Valid @RequestHeader @NotBlank String userId) {
        OrderResponse response = orderService.createOrder(Long.parseLong(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@RequestHeader @NotBlank String userId) {
        return ResponseEntity.ok(orderService.getMyOrders(Long.parseLong(userId)));
    }

    // @GetMapping("/{id}")
    // public ResponseEntity<OrderResponse> getOrder(@PathVariable @NotNull Long id,
    //         @RequestHeader @NotBlank String userId) {
    //     return ResponseEntity.ok(orderService.getOrderById(id, Long.parseLong(userId)));
    // }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable @NotNull Long id,
            @RequestHeader @NotBlank String userId) {
        return ResponseEntity.ok(orderService.getOrderDetailById(id, Long.parseLong(userId)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderStatusResponse> updateStatus(@PathVariable @NotNull Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request, @RequestHeader @NotBlank String userId) {
        return ResponseEntity.ok(orderService.updateStatus(id, request, Long.parseLong(userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> deleteOrder(@PathVariable @NotNull Long id,
            @RequestHeader @NotBlank String userId) {
        return ResponseEntity.ok(orderService.deleteOrder(id, Long.parseLong(userId)));
    }

}