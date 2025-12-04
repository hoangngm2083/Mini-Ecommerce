package org.example.miniecommerce.service;

import org.example.miniecommerce.dto.PageResponse;
import org.example.miniecommerce.dto.product.CreateProductRequest;
import org.example.miniecommerce.dto.product.ProductResponse;
import org.example.miniecommerce.dto.product.UpdateProductRequest;

import java.util.List;

public interface ProductService {
    PageResponse<ProductResponse> list(String keyword, Long categoryId, int page, int size);
    ProductResponse create(CreateProductRequest req);
    ProductResponse get(Long id);
    ProductResponse update(Long id, UpdateProductRequest req);
    List<ProductResponse> findAllByIds(List<Long> ids);
    void delete(Long id);
    void updateStockQuantity(Long productId, Integer newStockQuantity);
}
