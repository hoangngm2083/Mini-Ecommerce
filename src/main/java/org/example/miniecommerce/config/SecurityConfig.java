package org.example.miniecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(request -> request
    .requestMatchers("/api/auth/register").permitAll()
            .requestMatchers("/api/auth/login").permitAll()

            // Xem sản phẩm (khách vãng lai cũng được xem)
            .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()

            // ================================
            // 2. CUSTOMER - MUA HÀNG + THÔNG TIN CÁ NHÂN
            // ================================
            .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()           // xem + sửa chính mình
            .requestMatchers("/api/orders", "/api/orders/me", "/api/orders/me/**").hasRole("CUSTOMER")
            .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("CUSTOMER")
            .requestMatchers("/api/payments", "/api/payments/**").hasRole("CUSTOMER")
            .requestMatchers("/api/shippings", "/api/shippings/**").hasRole("CUSTOMER")

            // ================================
            // 3. STAFF - CHỈ QUẢN LÝ SẢN PHẨM & DANH MỤC
            // ================================
            .requestMatchers(HttpMethod.POST,   "/api/categories", "/api/categories/**").hasRole("STAFF")
            .requestMatchers(HttpMethod.PUT,    "/api/categories/**").hasRole("STAFF")
            .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("STAFF")

            .requestMatchers(HttpMethod.POST,   "/api/products", "/api/products/**").hasRole("STAFF")
            .requestMatchers(HttpMethod.PUT,    "/api/products/**").hasRole("STAFF")
            .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("STAFF")

            // ================================
            // 4. ADMIN - TOÀN QUYỀN
            // ================================
            .requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN")                    // quản lý toàn bộ user
            .requestMatchers(HttpMethod.PUT,    "/api/orders/**/status").hasRole("ADMIN")       // duyệt/cancel đơn
            .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET,    "/api/orders/**").hasRole("ADMIN")              // xem đơn của ai cũng được
            .requestMatchers("/api/admin/**").hasRole("ADMIN")                                  // đường dẫn admin riêng

            // ADMIN cũng được làm mọi thứ của STAFF + CUSTOMER (tự động vì hasRole không loại trừ)
            // Nếu muốn ADMIN override STAFF thì thêm hasAnyRole("ADMIN","STAFF") ở trên cũng được

            // ================================
            // 5. TẤT CẢ CÁC REQUEST CÒN LẠI → PHẢI ĐĂNG NHẬP
            // ===============================
    
        .anyRequest()
        .authenticated());

    http.csrf(csrf -> csrf.disable());

    return http.build();
  }

}