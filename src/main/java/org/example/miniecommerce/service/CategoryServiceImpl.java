package org.example.miniecommerce.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.dto.category.CategoryResponse;
import org.example.miniecommerce.dto.category.CreateCategoryRequest;
import org.example.miniecommerce.dto.category.UpdateCategoryRequest;
import org.example.miniecommerce.entity.Category;
import org.example.miniecommerce.factory.CategoryFactory;
import org.example.miniecommerce.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Page<CategoryResponse> list(String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Category> categories = (keyword != null && !keyword.isBlank())
                ? categoryRepository.findByNameContainingIgnoreCase(keyword, pageable)
                : categoryRepository.findAll(pageable);

        return categories.map(CategoryFactory::toResponse);
    }

    @Override
    public CategoryResponse create(CreateCategoryRequest req) {
        Category category = CategoryFactory.fromCreateRequest(req);
        String name = category.getName();
        if (categoryRepository.existsByNameIgnoreCaseTrim(name)) {
            throw new IllegalArgumentException("Danh mục '" + name + "' đã tồn tại!");
        }
        categoryRepository.insert(category);
        return CategoryFactory.toResponse(category);
    }

    @Override
    public CategoryResponse get(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryFactory::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));
    }

    @Override
    public CategoryResponse update(Long id, UpdateCategoryRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));
        CategoryFactory.updateCategory(category, req);
        String name = category.getName();
        if (categoryRepository.existsByNameIgnoreCaseTrimAndIdNot(name,id)) {
            throw new IllegalArgumentException("Danh mục '" + name + "' đã tồn tại!");
        }
        categoryRepository.update(category);
        return CategoryFactory.toResponse(category);
    }

    @Override
    public void delete(Long id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));

        long productCount = categoryRepository.countActiveProductsByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Không thể xóa! Còn " + productCount + " sản phẩm thuộc danh mục này.");
        }
        categoryRepository.delete(id);
    }
}
