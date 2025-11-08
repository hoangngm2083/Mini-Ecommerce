package org.example.miniecommerce.builder;

import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.dto.order.CreateOrderItemDto;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.OrderItem;
import org.example.miniecommerce.entity.OrderStatus;
import org.example.miniecommerce.repository.OrderItemRepository;
import org.example.miniecommerce.service.TotalCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderBuilder {

    private final OrderItemRepository orderItemRepository;
    private final TotalCalculator totalCalculator;


    public Order createOrder(Long userId, List<CreateOrderItemDto> itemsDto) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderItemDto dto : itemsDto) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(dto.productId());
            item.setQuantity(dto.quantity());
            item.setPrice(dto.price()); // Lấy từ product service
            order.getItems().add(item);
            total = total.add(dto.price().multiply(BigDecimal.valueOf(dto.quantity())));
        }

        order.setTotalAmount(total);
        return order;
    }
}