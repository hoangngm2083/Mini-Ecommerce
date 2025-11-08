package org.example.miniecommerce.dto.auth;

import org.example.miniecommerce.dto.user.UserResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
  private UserResponse user;
  private String message;
}
