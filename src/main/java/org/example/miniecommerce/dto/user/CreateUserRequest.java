package org.example.miniecommerce.dto.user;

import org.example.miniecommerce.entity.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {
  @NotBlank private String name;
  @NotNull private Role role;
  @NotNull private String email;
  @NotNull private String password;
}
