package org.example.miniecommerce.service;

import org.example.miniecommerce.dto.category.CategoryResponse;
import org.example.miniecommerce.dto.category.CreateCategoryRequest;
import org.example.miniecommerce.dto.category.UpdateCategoryRequest;
import org.springframework.data.domain.Page;

public interface CategoryService {
    Page<CategoryResponse> list(String keyword, int page, int size);
    CategoryResponse create(CreateCategoryRequest req);
    CategoryResponse get(Long id);
    CategoryResponse update(Long id, UpdateCategoryRequest req);
    void delete(Long id);
}