package org.example.miniecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // ⭐ BẬT CORS TRONG SECURITY (RẤT QUAN TRỌNG)
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);
            config.setAllowedOriginPatterns(List.of("http://localhost:*"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            return config;
        }));

        http.authorizeHttpRequests(request -> request
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/login").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()

                .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()
                .requestMatchers("/api/orders", "/api/orders/me", "/api/orders/me/**").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("CUSTOMER")
                .requestMatchers("/api/payments", "/api/payments/**").hasRole("CUSTOMER")
                .requestMatchers("/api/shippings", "/api/shippings/**").hasRole("CUSTOMER")

                .requestMatchers(HttpMethod.POST, "/api/categories", "/api/categories/**").hasRole("STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("STAFF")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("STAFF")

                .requestMatchers(HttpMethod.POST, "/api/products", "/api/products/**").hasRole("STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("STAFF")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("STAFF")

                .requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/orders/**/status").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/orders/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                .anyRequest().authenticated()
        );

        http.csrf(csrf -> csrf.disable());

        return http.build();
    }
}
