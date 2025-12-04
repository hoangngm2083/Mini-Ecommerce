package org.example.miniecommerce.service;

import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.dto.PageResponse;
import org.example.miniecommerce.dto.product.CreateProductRequest;
import org.example.miniecommerce.dto.product.ProductResponse;
import org.example.miniecommerce.dto.product.UpdateProductRequest;
import org.example.miniecommerce.entity.Category;
import org.example.miniecommerce.entity.Product;
import org.example.miniecommerce.factory.ProductFactory;
import org.example.miniecommerce.repository.CategoryRepository;
import org.example.miniecommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public PageResponse<ProductResponse> list(String keyword, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products;

        if (keyword != null && !keyword.isBlank()) {
            products = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        List<ProductResponse> content = products.getContent().stream()
                .map(product -> {
                    return ProductFactory.toResponse(product);
                })
                .toList();

        return new PageResponse<>(
                content,
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isFirst(),
                products.isLast(),
                products.isEmpty()
        );
    }

    @Override
    public ProductResponse create(CreateProductRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục mà bạn muốn thêm sản phẩm"));
        Product product = ProductFactory.fromCreateRequest(req, category);
        String name = product.getName();
        if (productRepository.existsByNameIgnoreCaseTrim(name)) {
            throw new IllegalArgumentException("Sản phẩm với tên '" + name + "' đã tồn tại!");
        }
        return ProductFactory.toResponse(productRepository.insert(product));
    }

    @Override
    public ProductResponse get(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        return ProductFactory.toResponse(product);
    }

    @Override
    public ProductResponse update(Long id, UpdateProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));
        ProductFactory.updateProduct(product, req, category);
        String name = product.getName();
        if (productRepository.existsByNameIgnoreCaseTrimAndIdNot(name,id)) {
            throw new IllegalArgumentException("Sản phẩm với tên '" + name + "' đã tồn tại!");
        }
        return ProductFactory.toResponse(productRepository.update(product));
    }

    @Override
    public List<ProductResponse> findAllByIds(List<Long> ids) {
        List<ProductResponse> products = new ArrayList<>();
        ids.forEach(id -> {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
            products.add(ProductFactory.toResponse(product));
        });
        return products;
    }

    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        productRepository.delete(product.getId());
    }
    
    @Override
    public void updateStockQuantity(Long productId, Integer newStockQuantity) {
        productRepository.updateStockQuantity(productId, newStockQuantity);
    }
}
