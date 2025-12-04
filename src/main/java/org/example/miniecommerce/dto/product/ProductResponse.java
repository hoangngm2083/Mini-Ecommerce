package org.example.miniecommerce.dto.product;

import org.example.miniecommerce.dto.category.CategoryResponse;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        Long categoryId,
        CategoryResponse category
        ) {
}
