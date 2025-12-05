package org.example.miniecommerce.service.order.template;

import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.dto.order.CreateOrderRequest;
import org.example.miniecommerce.dto.product.ProductResponse;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.factory.OrderFactory;
import org.example.miniecommerce.repository.OrderRepository;
import org.example.miniecommerce.service.ProductService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StandardCreateOrderProcessor extends CreateOrderProcessor {

    private final OrderRepository orderRepository;
    private final OrderFactory orderFactory;
    private final ProductService productService;


    @Override
    protected Order createOrder(Long userId, CreateOrderRequest request) {
        return orderFactory.createOrder(userId, request.items());
    }

    @Override
    protected void saveOrder(Order order) {
        orderRepository.save(order);
    }

    @Override
    protected void deductInventory(CreateOrderRequest request) {
        // Kiểm tra và trừ số lượng sản phẩm từ inventory ngay lập tức
        request.items()
                .forEach(item -> {
                    ProductResponse product = productService.get(item.productId());
                    if (product.stockQuantity() < item.quantity()) {
                        throw new IllegalArgumentException(product.name() + " đã hết hàng!");
                    }
                    Integer newStock = product.stockQuantity() - item.quantity();
                    productService.updateStockQuantity(item.productId(), newStock);
                });
    }

}
