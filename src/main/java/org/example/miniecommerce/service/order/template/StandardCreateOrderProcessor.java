package org.example.miniecommerce.service.order.template;

import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.dto.order.CreateOrderItemDto;
import org.example.miniecommerce.dto.order.CreateOrderRequest;
import org.example.miniecommerce.dto.product.ProductResponse;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.factory.OrderFactory;
import org.example.miniecommerce.repository.OrderRepository;
import org.example.miniecommerce.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StandardCreateOrderProcessor extends CreateOrderProcessor {

    private final OrderRepository orderRepository;
    private final OrderFactory orderFactory;
    private final ProductService productService;

    @Override
    protected void checkInventory(CreateOrderRequest request) {
        List<CreateOrderItemDto> itemDtos = request.items();

        List<ProductResponse> products = productService.findAllByIds(itemDtos.stream()
                .map(CreateOrderItemDto::productId)
                .toList());
        if (products.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }

        HashMap<Long, Integer> map = new HashMap<>();
        itemDtos.forEach(item -> {
            map.put(item.productId(), item.quantity());
        });

        for (ProductResponse product : products) {
            if (product.stockQuantity() <= map.get(product.id())) {
                throw new IllegalArgumentException(product.name() + " đã hết hàng!");
            }
        }

    }

    @Override
    protected Order createOrder(Long userId, CreateOrderRequest request) {
        return orderFactory.createOrder(userId, request.items());
    }

    @Override
    protected void saveOrder(Order order) {
        orderRepository.save(order);
    }

    @Override
    protected void deductInventory(Order order) {
        // Trừ số lượng sản phẩm từ inventory sau khi đặt hàng thành công
        order.getItems()
                .forEach(item -> {
                    Integer currentStock = productService.get(item.getProductId())
                            .stockQuantity();
                    Integer newStock = currentStock - item.getQuantity();
                    productService.updateStockQuantity(item.getProductId(), newStock);
                });
    }

}
