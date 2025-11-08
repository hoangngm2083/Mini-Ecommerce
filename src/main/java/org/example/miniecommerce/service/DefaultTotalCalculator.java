package org.example.miniecommerce.service;

import org.example.miniecommerce.entity.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DefaultTotalCalculator implements TotalCalculator {
    @Override
    public BigDecimal calculate(Order order) {
        return order.getItems()
                .stream()
                .map(item -> item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
