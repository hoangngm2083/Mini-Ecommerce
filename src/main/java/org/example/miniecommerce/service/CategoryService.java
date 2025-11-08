package org.example.miniecommerce.service;

import java.util.NoSuchElementException;

import org.example.miniecommerce.dto.category.CategoryResponse;
import org.example.miniecommerce.dto.category.CreateCategoryRequest;
import org.example.miniecommerce.entity.Category;
import org.example.miniecommerce.factory.CategoryFactory;
import org.example.miniecommerce.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Page<CategoryResponse> list(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Category> categories;
        if (keyword != null && !keyword.isBlank()) {
            categories = categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            categories = categoryRepository.findAll(pageable);
        }
        return categories.map(CategoryFactory::toResponse);
    }

    public CategoryResponse create(CreateCategoryRequest req) {
        Category category = CategoryFactory.fromCreateRequest(req);
        return CategoryFactory.toResponse(categoryRepository.save(category));
    }

    public CategoryResponse get(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found with id: " + id));
        return CategoryFactory.toResponse(category);
    }

    public CategoryResponse update(Long id, CreateCategoryRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found with id: " + id));
        CategoryFactory.updateCategory(category, req);
        return CategoryFactory.toResponse(categoryRepository.save(category));
    }

    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found with id: " + id));
        categoryRepository.delete(category);
    }
}
