package org.example.miniecommerce.controller;

import org.example.miniecommerce.dto.auth.AuthResponse;
import org.example.miniecommerce.dto.auth.LoginRequest;
import org.example.miniecommerce.dto.user.CreateUserRequest;
import org.example.miniecommerce.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  
  @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody CreateUserRequest request,
            HttpSession session){
        
      AuthResponse response = authService.register(request);

      // Lưu user vào session sau khi đăng ký thành công
      session.setAttribute("user", response.getUser());

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
      }


  @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request,
            HttpSession session
    ) {
        AuthResponse response = authService.login(request);

        // Lưu user vào session khi đăng nhập thành công
        session.setAttribute("user", response.getUser());

        return ResponseEntity.ok(response);
    }
}
