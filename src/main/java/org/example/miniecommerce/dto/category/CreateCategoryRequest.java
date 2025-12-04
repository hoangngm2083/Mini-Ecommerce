package org.example.miniecommerce.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(

        @NotBlank(message = "Tên danh mục không được để trống")
        @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2-100 ký tự")
        String name,

        @NotBlank(message = "Mô tả không được để trống")
        @Size(max = 500, message = "Mô tả không quá 500 ký tự")
        String description

) {}