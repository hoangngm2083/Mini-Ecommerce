package org.example.miniecommerce.factory;

import org.example.miniecommerce.dto.category.CategoryResponse;
import org.example.miniecommerce.dto.category.CreateCategoryRequest;
import org.example.miniecommerce.dto.category.UpdateCategoryRequest;
import org.example.miniecommerce.entity.Category;

public class CategoryFactory {
    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription());
    }

    public static Category fromCreateRequest(CreateCategoryRequest req) {
        Category c = new Category();
        c.setName(req.name());
        c.setDescription(req.description());
        return c;
    }

    public static void updateCategory(Category c, UpdateCategoryRequest req) {
        c.setName(req.name());
        c.setDescription(req.description());
    }
}
