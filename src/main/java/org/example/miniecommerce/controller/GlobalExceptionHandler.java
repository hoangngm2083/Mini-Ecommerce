package org.example.miniecommerce.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Helper method – phải để đúng vị trí này
    private Map<String, Object> buildErrorResponse(HttpStatus status, String error, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", path.replace("uri=", ""));
        return body;
    }

    // 1. Validation @Valid @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Dữ liệu đầu vào không hợp lệ",
                request.getDescription(false)
        );
        body.put("errors", fieldErrors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 2. Validation @PathVariable, @RequestParam
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String field = v.getPropertyPath().toString();
            fieldErrors.put(field, v.getMessage());
        }

        Map<String, Object> body = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Tham số không hợp lệ",
                request.getDescription(false)
        );
        body.put("errors", fieldErrors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 3. ResponseStatusException (404, 400 bạn tự throw)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex, WebRequest request) {

        Map<String, Object> body = buildErrorResponse(
                (HttpStatus) ex.getStatusCode(),
                ex.getStatusCode() == HttpStatus.NOT_FOUND ? "Not Found" : "Bad Request",
                ex.getReason() != null ? ex.getReason() : "Yêu cầu không hợp lệ",
                request.getDescription(false)
        );
        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    // 4. Tên trùng → IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException ex, WebRequest request) {
        Map<String, Object> body = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 5. Không cho xóa vì còn sản phẩm → IllegalStateException
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(
            IllegalStateException ex, WebRequest request) {
        Map<String, Object> body = buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // 6. Lỗi server thật sự
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(
            Exception ex, WebRequest request) {

        ex.printStackTrace(); // để dev biết lỗi gì

        Map<String, Object> body = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Server Error",
                "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
                request.getDescription(false)
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}