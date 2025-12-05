# User Module - Authentication & Authorization Documentation

## 📋 Mục lục
1. [Tổng quan](#tổng-quan)
2. [Kiến trúc User Module](#kiến-trúc-user-module)
3. [Authentication (Xác thực)](#authentication-xác-thực)
4. [Authorization (Phân quyền)](#authorization-phân-quyền)
5. [Role-Based Access Control](#role-based-access-control)
6. [Security Configuration](#security-configuration)
7. [API Endpoints](#api-endpoints)
8. [Flow Diagrams](#flow-diagrams)

---

## Tổng quan

User Module quản lý toàn bộ **Authentication** (xác thực danh tính) và **Authorization** (phân quyền truy cập) trong hệ thống Mini-Ecommerce. Module sử dụng **Spring Security** kết hợp với **Session-based Authentication** và **Role-Based Access Control (RBAC)**.

### Công nghệ sử dụng
- **Spring Security** - Framework bảo mật
- **Session Management** - Lưu trữ trạng thái đăng nhập
- **RBAC** - Phân quyền dựa trên vai trò
- **BCrypt** - Mã hóa mật khẩu (có thể bổ sung)

---

## Kiến trúc User Module

### 1. Entity Layer

#### User Entity
```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    private String name;           // Họ tên
    private Role role;             // Vai trò: ADMIN, CUSTOMER, STAFF
    private String email;          // Email (unique, dùng để login)
    private String password;       // Mật khẩu
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt; // Soft delete
}
```

#### Role Enum
```java
public enum Role {
    ADMIN,      // Quản trị viên - Quyền cao nhất
    CUSTOMER,   // Khách hàng - Người mua hàng
    STAFF       // Nhân viên - Quản lý sản phẩm, danh mục
}
```

### 2. Service Layer

#### AuthService
Xử lý logic authentication:
- **register()** - Đăng ký tài khoản mới
- **login()** - Đăng nhập

#### UserService
Quản lý thông tin user:
- **getAllUser()** - Lấy danh sách users (ADMIN only)
- **getUserId()** - Lấy thông tin user theo ID
- **updateUser()** - Cập nhật thông tin user
- **deleteUser()** - Xóa user (soft delete)

### 3. Controller Layer

#### AuthController (`/api/auth`)
- `POST /register` - Đăng ký tài khoản
- `POST /login` - Đăng nhập

#### UserController (`/api/users`)
- `GET /` - Lấy danh sách users
- `GET /me` - Lấy thông tin user hiện tại
- `GET /{id}` - Lấy thông tin user theo ID
- `PUT /{id}` - Cập nhật user
- `DELETE /{id}` - Xóa user

---

## Authentication (Xác thực)

### 1. Đăng ký (Register)

#### Flow
```
1. User gửi thông tin đăng ký (name, email, password, role)
2. Backend validate:
   - Email chưa tồn tại
   - Password đủ mạnh (frontend validation)
3. Tạo User entity với role = CUSTOMER (hardcoded ở frontend)
4. Lưu vào database
5. Tự động tạo session và đăng nhập
6. Trả về user info + success message
```

#### Request
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Nguyễn Văn A",
  "email": "nguyenvana@example.com",
  "password": "password123",
  "role": "CUSTOMER"
}
```

#### Response
```json
{
  "user": {
    "id": 6,
    "name": "Nguyễn Văn A",
    "email": "nguyenvana@example.com",
    "role": "CUSTOMER",
    "createdAt": "2025-12-05T15:00:00",
    "updatedAt": "2025-12-05T15:00:00"
  },
  "message": "Register successful"
}
```

#### Validation Rules
1. ✅ Email phải unique (không trùng)
2. ✅ Email phải đúng format
3. ✅ Password tối thiểu 6 ký tự (frontend)
4. ✅ Name không được rỗng
5. ✅ Role mặc định là CUSTOMER (hardcoded)

### 2. Đăng nhập (Login)

#### Flow
```
1. User gửi email + password
2. Backend tìm user theo email
3. So sánh password (plain text)
4. Nếu đúng:
   - Tạo session
   - Lưu user info vào session
   - Trả về user info
5. Nếu sai:
   - Trả về 401 Unauthorized
```

#### Request
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
```

#### Response
```json
{
  "user": {
    "id": 1,
    "name": "Admin User",
    "email": "admin@example.com",
    "role": "ADMIN",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "message": "Login successful"
}
```

### 3. Session Management

#### Cách hoạt động
```java
// AuthController.java
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(
    @RequestBody LoginRequest request,
    HttpSession session
) {
    AuthResponse response = authService.login(request);
    
    // Lưu user vào session
    session.setAttribute("user", response.getUser());
    
    return ResponseEntity.ok(response);
}
```

#### Session Storage
- **Key**: `"user"`
- **Value**: `UserResponse` object
- **Lifetime**: Mặc định của server (thường 30 phút)
- **Storage**: Server-side (in-memory hoặc Redis)

#### Lấy user từ session
```java
// UserController.java
@GetMapping("/me")
public ResponseEntity<?> getCurrentUser(HttpSession session) {
    Object user = session.getAttribute("user");
    if (user == null) {
        return ResponseEntity.status(401)
            .body("{\"message\": \"User not logged in\"}");
    }
    return ResponseEntity.ok(user);
}
```

---

## Authorization (Phân quyền)

### 1. Role-Based Access Control (RBAC)

#### Hierarchy (Phân cấp quyền)
```
ADMIN (Cao nhất)
  ↓
STAFF (Trung bình)
  ↓
CUSTOMER (Thấp nhất)
```

**Lưu ý:** Trong Spring Security, role hierarchy KHÔNG tự động. Mỗi role phải được khai báo riêng.

### 2. Quyền của từng Role

#### CUSTOMER (Khách hàng)
**Mục đích:** Người dùng cuối, mua hàng

**Quyền hạn:**
- ✅ Xem sản phẩm, danh mục (public)
- ✅ Đăng ký, đăng nhập
- ✅ Xem thông tin cá nhân (`/api/users/me`)
- ✅ Tạo đơn hàng (`POST /api/orders`)
- ✅ Xem đơn hàng của mình (`/api/orders/me`)
- ✅ Quản lý thanh toán (`/api/payments`)
- ✅ Quản lý vận chuyển (`/api/shippings`)
- ❌ KHÔNG được quản lý sản phẩm, danh mục
- ❌ KHÔNG được xem/quản lý users khác
- ❌ KHÔNG được xem/quản lý đơn hàng của người khác

**Lý do phân quyền:**
- Bảo vệ dữ liệu cá nhân của users khác
- Ngăn chặn thao tác trái phép với đơn hàng
- Đảm bảo chỉ staff mới được quản lý catalog

#### STAFF (Nhân viên)
**Mục đích:** Quản lý kho hàng, sản phẩm

**Quyền hạn:**
- ✅ Tất cả quyền của CUSTOMER
- ✅ Tạo/Sửa/Xóa sản phẩm (`/api/products`)
- ✅ Tạo/Sửa/Xóa danh mục (`/api/categories`)
- ❌ KHÔNG được quản lý users
- ❌ KHÔNG được xem/quản lý tất cả đơn hàng
- ❌ KHÔNG được thay đổi trạng thái đơn hàng

**Lý do phân quyền:**
- Cho phép quản lý catalog mà không cần quyền admin
- Tách biệt trách nhiệm: Staff quản lý sản phẩm, Admin quản lý hệ thống
- Giảm rủi ro khi có nhiều staff

#### ADMIN (Quản trị viên)
**Mục đích:** Quản lý toàn bộ hệ thống

**Quyền hạn:**
- ✅ Tất cả quyền của STAFF
- ✅ Quản lý tất cả users (`/api/users`)
- ✅ Xem tất cả đơn hàng (`/api/orders`)
- ✅ Thay đổi trạng thái đơn hàng (`PUT /api/orders/{id}/status`)
- ✅ Xóa đơn hàng (`DELETE /api/orders/{id}`)
- ✅ Truy cập admin panel (`/api/admin/**`)

**Lý do phân quyền:**
- Quyền cao nhất để quản lý toàn bộ hệ thống
- Có thể can thiệp vào mọi hoạt động
- Chịu trách nhiệm cuối cùng về dữ liệu

---

## Security Configuration

### 1. SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        // CORS Configuration
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);
            config.setAllowedOriginPatterns(List.of("http://localhost:*"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            return config;
        }));

        // Authorization Rules
        http.authorizeHttpRequests(request -> request
            // Public endpoints
            .requestMatchers("/api/auth/register").permitAll()
            .requestMatchers("/api/auth/login").permitAll()
            .requestMatchers("/api/shipments/fee").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()

            // User endpoints - SPECIFIC FIRST
            .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()
            .requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN")

            // Order endpoints - SPECIFIC FIRST
            .requestMatchers("/api/orders/me", "/api/orders/me/**").hasRole("CUSTOMER")
            .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("CUSTOMER")
            .requestMatchers("/api/orders/**").hasRole("ADMIN")
            
            // Payment & Shipping
            .requestMatchers("/api/payments", "/api/payments/**").hasRole("CUSTOMER")
            .requestMatchers("/api/shippings", "/api/shippings/**").hasRole("CUSTOMER")

            // Category & Product management
            .requestMatchers(HttpMethod.POST, "/api/categories", "/api/categories/**").hasRole("STAFF")
            .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("STAFF")
            .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("STAFF")
            .requestMatchers(HttpMethod.POST, "/api/products", "/api/products/**").hasRole("STAFF")
            .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("STAFF")
            .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("STAFF")

            // Admin-only
            .requestMatchers(HttpMethod.PUT, "/api/orders/**/status").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasRole("ADMIN")
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            // Default
            .anyRequest().authenticated()
        );

        // Disable CSRF (for API)
        http.csrf(csrf -> csrf.disable());

        return http.build();
    }
}
```

### 2. Nguyên tắc cấu hình

#### Rule Ordering (Thứ tự quan trọng!)
```
SPECIFIC (Cụ thể) → GENERAL (Chung)
```

**Ví dụ:**
```java
// ✅ ĐÚNG
.requestMatchers("/api/users/me").authenticated()      // Specific
.requestMatchers("/api/users/**").hasRole("ADMIN")     // General

// ❌ SAI
.requestMatchers("/api/users/**").hasRole("ADMIN")     // General match trước
.requestMatchers("/api/users/me").authenticated()      // Never reached!
```

**Lý do:** Spring Security đánh giá rules từ trên xuống, rule đầu tiên match sẽ được áp dụng.

#### Access Levels
1. **permitAll()** - Cho phép tất cả (kể cả anonymous)
2. **authenticated()** - Yêu cầu đăng nhập (bất kỳ role nào)
3. **hasRole("ROLE")** - Yêu cầu role cụ thể
4. **hasAnyRole("ROLE1", "ROLE2")** - Yêu cầu một trong các roles

---

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Đăng ký tài khoản mới |
| POST | `/api/auth/login` | Public | Đăng nhập |

### User Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/users` | ADMIN | Lấy danh sách tất cả users |
| GET | `/api/users/me` | Authenticated | Lấy thông tin user hiện tại |
| GET | `/api/users/{id}` | ADMIN | Lấy thông tin user theo ID |
| PUT | `/api/users/{id}` | ADMIN | Cập nhật thông tin user |
| DELETE | `/api/users/{id}` | ADMIN | Xóa user |

### Product & Category Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/products` | Public | Lấy danh sách sản phẩm |
| GET | `/api/products/{id}` | Public | Lấy chi tiết sản phẩm |
| POST | `/api/products` | STAFF | Tạo sản phẩm mới |
| PUT | `/api/products/{id}` | STAFF | Cập nhật sản phẩm |
| DELETE | `/api/products/{id}` | STAFF | Xóa sản phẩm |
| GET | `/api/categories` | Public | Lấy danh sách danh mục |
| GET | `/api/categories/{id}` | Public | Lấy chi tiết danh mục |
| POST | `/api/categories` | STAFF | Tạo danh mục mới |
| PUT | `/api/categories/{id}` | STAFF | Cập nhật danh mục |
| DELETE | `/api/categories/{id}` | STAFF | Xóa danh mục |

### Order Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/orders/me` | CUSTOMER | Lấy đơn hàng của mình |
| POST | `/api/orders` | CUSTOMER | Tạo đơn hàng mới |
| GET | `/api/orders` | ADMIN | Lấy tất cả đơn hàng |
| GET | `/api/orders/{id}` | ADMIN | Lấy chi tiết đơn hàng |
| PUT | `/api/orders/{id}/status` | ADMIN | Cập nhật trạng thái đơn hàng |
| DELETE | `/api/orders/{id}` | ADMIN | Xóa đơn hàng |

---

## Flow Diagrams

### 1. Registration Flow
```
User (Frontend)
    ↓
    POST /api/auth/register
    {name, email, password, role: "CUSTOMER"}
    ↓
AuthController
    ↓
AuthService.register()
    ↓
Validate: Email unique?
    ↓ Yes
UserFactory.toUser()
    ↓
UserRepository.save()
    ↓
Create Session
session.setAttribute("user", userResponse)
    ↓
Return AuthResponse
    ↓
Frontend: Auto login
```

### 2. Login Flow
```
User (Frontend)
    ↓
    POST /api/auth/login
    {email, password}
    ↓
AuthController
    ↓
AuthService.login()
    ↓
UserRepository.findByEmail()
    ↓
User found?
    ↓ Yes
Password match?
    ↓ Yes
Create Session
session.setAttribute("user", userResponse)
    ↓
Return AuthResponse
    ↓
Frontend: Store user info
```

### 3. Authorization Flow
```
User Request
    ↓
Spring Security Filter
    ↓
Check Session: User logged in?
    ↓ Yes
Extract User Role
    ↓
Match Request with SecurityConfig Rules
    ↓
Rule matches?
    ↓ Yes
Check Role Permission
    ↓
Has Permission?
    ↓ Yes
Allow Request → Controller
    ↓ No
Return 403 Forbidden
```

---

## Lý do thiết kế

### 1. Tại sao dùng Session thay vì JWT?

**Ưu điểm Session:**
- ✅ Đơn giản, dễ implement
- ✅ Server có full control (có thể revoke ngay lập tức)
- ✅ Không cần client lưu token
- ✅ Phù hợp với monolithic architecture

**Nhược điểm:**
- ❌ Không scale tốt (cần sticky session hoặc Redis)
- ❌ Không phù hợp với microservices
- ❌ CORS phức tạp hơn

**Kết luận:** Session phù hợp cho MVP và hệ thống nhỏ.

### 2. Tại sao hardcode role = CUSTOMER ở frontend?

**Lý do:**
- ✅ Bảo mật: Ngăn user tự đăng ký làm ADMIN/STAFF
- ✅ Business logic: Chỉ admin mới tạo được staff/admin
- ✅ Đơn giản: Không cần thêm logic phức tạp

**Cách tạo ADMIN/STAFF:**
- Thông qua database migration (seed data)
- Hoặc ADMIN tạo thông qua admin panel

### 3. Tại sao phân quyền theo endpoint thay vì method-level?

**Ưu điểm:**
- ✅ Tập trung tại một chỗ (SecurityConfig)
- ✅ Dễ review và audit
- ✅ Dễ test

**Nhược điểm:**
- ❌ Ít linh hoạt hơn `@PreAuthorize`
- ❌ Khó handle logic phức tạp (ví dụ: owner check)

**Kết luận:** Endpoint-level phù hợp cho RBAC đơn giản.

### 4. Tại sao CUSTOMER có quyền truy cập payments/shippings?

**Lý do:**
- ✅ Customer cần xem trạng thái thanh toán của đơn hàng
- ✅ Customer cần xem trạng thái vận chuyển
- ✅ Tăng trải nghiệm người dùng

**Bảo mật:**
- Backend phải validate: User chỉ xem được payment/shipping của đơn hàng của mình
- Sử dụng `userId` header để filter

---

## Best Practices

### 1. Security
- ✅ Luôn validate input
- ✅ Sử dụng BCrypt để hash password (TODO)
- ✅ Implement rate limiting cho login
- ✅ Log tất cả authentication attempts
- ✅ Sử dụng HTTPS trong production

### 2. Authorization
- ✅ Principle of Least Privilege (quyền tối thiểu)
- ✅ Specific rules trước General rules
- ✅ Document rõ ràng lý do phân quyền
- ✅ Regular security audit

### 3. Session Management
- ✅ Set session timeout hợp lý
- ✅ Invalidate session khi logout
- ✅ Sử dụng Redis cho production
- ✅ Implement "Remember Me" nếu cần

---

## Testing

### Test Cases

#### 1. Authentication
- [ ] Register với email mới → Success
- [ ] Register với email đã tồn tại → 400 Bad Request
- [ ] Login với credentials đúng → Success + Session created
- [ ] Login với email sai → 404 Not Found
- [ ] Login với password sai → 401 Unauthorized

#### 2. Authorization - CUSTOMER
- [ ] GET /api/products → 200 OK
- [ ] GET /api/users/me → 200 OK
- [ ] POST /api/orders → 200 OK
- [ ] GET /api/orders/me → 200 OK
- [ ] GET /api/users → 403 Forbidden
- [ ] POST /api/products → 403 Forbidden

#### 3. Authorization - STAFF
- [ ] POST /api/products → 200 OK
- [ ] PUT /api/categories/{id} → 200 OK
- [ ] GET /api/users → 403 Forbidden
- [ ] DELETE /api/orders/{id} → 403 Forbidden

#### 4. Authorization - ADMIN
- [ ] GET /api/users → 200 OK
- [ ] GET /api/orders → 200 OK
- [ ] PUT /api/orders/{id}/status → 200 OK
- [ ] DELETE /api/users/{id} → 200 OK

---

## Roadmap

### Phase 1 (Current)
- ✅ Session-based authentication
- ✅ Role-based authorization
- ✅ Basic security configuration

### Phase 2 (Future)
- [ ] BCrypt password hashing
- [ ] JWT authentication (optional)
- [ ] Refresh token mechanism
- [ ] OAuth2 integration (Google, Facebook)
- [ ] Two-factor authentication (2FA)
- [ ] Password reset functionality
- [ ] Email verification

### Phase 3 (Advanced)
- [ ] Audit logging
- [ ] Rate limiting
- [ ] IP whitelisting
- [ ] Advanced RBAC (permissions)
- [ ] Dynamic role assignment
- [ ] Multi-tenancy support

---

## Kết luận

User Module hiện tại cung cấp một hệ thống authentication và authorization **đơn giản nhưng hiệu quả** cho Mini-Ecommerce. Thiết kế tập trung vào:

1. **Bảo mật cơ bản** - Session-based auth với RBAC
2. **Dễ maintain** - Centralized security config
3. **Scalable** - Có thể mở rộng sang JWT/OAuth2
4. **User-friendly** - Clear error messages, good UX

Hệ thống phù hợp cho **MVP và small-to-medium applications**. Khi scale lên, nên consider:
- JWT thay vì Session
- Redis cho session storage
- More granular permissions
- Advanced security features (2FA, OAuth2)
