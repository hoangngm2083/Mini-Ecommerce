package org.example.miniecommerce.controller;

import java.util.List;

import org.example.miniecommerce.dto.user.UpdateUserRequest;
import org.example.miniecommerce.dto.user.UserResponse;
import org.example.miniecommerce.entity.User;
import org.example.miniecommerce.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
    

  @GetMapping
  public List <User> getAllUser () {
    return userService.getAllUser();
  }

  @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"User not logged in\"}");
        }
        return ResponseEntity.ok(user);
      }

  @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserId(id));
    }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> updateUser(
          @PathVariable String id,
          @RequestBody UpdateUserRequest request) {
      return ResponseEntity.ok(userService.updateUser(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteUser(@PathVariable String id) {
      userService.deleteUser(id);
      return ResponseEntity.ok("User deleted");
  }
}