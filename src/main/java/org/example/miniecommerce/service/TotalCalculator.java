package org.example.miniecommerce.service;

import org.example.miniecommerce.entity.Order;

import java.math.BigDecimal;

public interface TotalCalculator {
    BigDecimal calculate(Order order);
}