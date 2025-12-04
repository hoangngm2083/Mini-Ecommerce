package org.example.miniecommerce.service.order.template;

import org.example.miniecommerce.dto.order.CreateOrderRequest;
import org.example.miniecommerce.entity.Order;
import org.springframework.transaction.annotation.Transactional;

/**
 * Template Method Pattern: Abstract class định nghĩa workflow xử lý đơn hàng
 */
public abstract class CreateOrderProcessor {

    /**
     * Template Method: Workflow chính để xử lý đơn hàng
     */
    @Transactional
    public final Order processOrder(Long userId, CreateOrderRequest request) {
        // Step 1: Validate request
        validateRequest(request);

        // Step 2: Check inventory
        checkInventory(request);


        // Step 3: Create order with userId
        Order originalOrder = createOrder(userId, request);

        // Step 4: Calculate total additional costs
        Order updatedOrder = calculateTotalAdditionalCosts(originalOrder);

        // Step 6: Deduct inventory after successful order creation
        deductInventory(updatedOrder);

        // Step 5: Save order to db
        saveOrder(updatedOrder);


        return updatedOrder;
    }

    // Abstract methods - must be implemented by subclasses
    protected abstract void checkInventory(CreateOrderRequest request);

    protected abstract Order createOrder(Long userId, CreateOrderRequest request);

    protected void validateRequest(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request cannot be null");
        }

        if (request.items() == null || request.items()
                .isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Validate each item
        request.items()
                .forEach(item -> {
                    if (item.productId() == null) {
                        throw new IllegalArgumentException("Product ID cannot be null");
                    }
                    if (item.quantity() <= 0) {
                        throw new IllegalArgumentException("Quantity must be positive");
                    }
                });
    }

    // Hook methods - can be overridden by subclasses
    protected Order calculateTotalAdditionalCosts(Order order) {
        return order;
    }

    protected abstract void saveOrder(Order order);

    protected abstract void deductInventory(Order order);
}
