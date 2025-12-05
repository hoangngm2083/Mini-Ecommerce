package org.example.miniecommerce.service;

import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.dto.auth.AuthResponse;
import org.example.miniecommerce.dto.auth.LoginRequest;
import org.example.miniecommerce.dto.user.CreateUserRequest;
import org.example.miniecommerce.dto.user.UserResponse;
import org.example.miniecommerce.entity.User;
import org.example.miniecommerce.factory.UserFactory;
import org.example.miniecommerce.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserFactory userFactory;


    //register
    @Override
    public AuthResponse register(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = userFactory.toUser(request);
        userRepository.save(user);


        UserResponse userResponse = userFactory.toUserResponse(user);

        return AuthResponse.builder()
                .message("Register successful")
                .user(userResponse)
                .build();
    }

    // login
    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getPassword()
                .equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        UserResponse userResponse = userFactory.toUserResponse(user);

        return AuthResponse.builder()
                .message("Login successful")
                .user(userResponse)
                .build();
    }
}
