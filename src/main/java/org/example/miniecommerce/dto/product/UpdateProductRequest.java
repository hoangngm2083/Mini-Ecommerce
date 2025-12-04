
package org.example.miniecommerce.dto.product;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank(message = "Tên sản phẩm không được để trống")
        @Size(min = 2, max = 200, message = "Tên sản phẩm phải từ 2-200 ký tự")
        String name,

        @Size(max = 1000, message = "Mô tả không quá 1000 ký tự")
        String description,

        @DecimalMin(value = "0.0", inclusive = true, message = "Giá phải ≥ 0")
        BigDecimal price,

        @Min(value = 0, message = "Số lượng tồn kho phải ≥ 0")
        Integer stockQuantity,

        @Positive(message = "ID danh mục phải là số dương")
        Long categoryId
) {}