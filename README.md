# Mini E-commerce API

Đồ án **Lập trình Hướng Đối Tượng (Java)** – xây dựng hệ thống **Mini E-commerce** backend bằng **Spring Boot + Spring Data JPA + Flyway + MySQL**.

## Công nghệ sử dụng

- Java 17+
- Spring Boot 3.x
- Spring Data JPA (Hibernate)
- Flyway (migration)
- MySQL 8
- Lombok
- Maven

---

## Cấu trúc project

```
src/main/java/org/example/miniecommerce/
 ├─ controller/    (REST API controllers)
 ├─ service/       (Business logic)
 ├─ repository/    (JPA repositories)
 ├─ entity/        (JPA entities)
 ├─ dto/           (Request/Response DTOs)
 ├─ factory/       (Factory Pattern mappers)
 └─ MiniEcommerceApplication.java (main class)
```

---

## Cài đặt & Chạy ứng dụng

### 1. Clone source code từ GitHub

```bash
git clone https://github.com/hoangngm2083/Mini-Ecommerce.git
cd Mini-Ecommerce
```

### 2. Cấu hình Database

Tạo database MySQL:

```sql
CREATE DATABASE mini-ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Chỉnh sửa `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mini-ecommerce
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

### 3. Migration Database (Flyway)

Flyway sẽ tự động chạy migration khi bạn start project lần đầu.  
Schema + dữ liệu mẫu được định nghĩa trong:

```
src/main/resources/db/migration/V1__init.sql
```
(Nếu không chạy được V1_.sql ngay ở thư mục migration thì copy file V1_.sql trong thư mục migration vào src/main/resources/db/migration/ rồi start project lần đầu thì sẽ khởi tạo các bảng và dữ liệu mẫu, sau khi đã migrate tables thì xóa file này ở src/main/resources/db/migration/ đi)
### 4. Mở project bằng IntelliJ IDEA

- File → Open → chọn thư mục project.
- Đảm bảo IntelliJ nhận **Java SDK 17**.
- Cài plugin Lombok (nếu chưa có) và bật `Enable annotation processing`.

### 5. Chạy ứng dụng

- Vào `MiniEcommerceApplication.java` → nhấn **Run**.
- Server chạy tại: `http://localhost:8081`.

---

## Test API

Sử dụng **Postman**:

- Import collection: `postman/Mini-Ecommerce-API.json`
- Gồm các module:
  - Products (CRUD sản phẩm)
  - Orders (tạo đơn, cập nhật trạng thái)
  - Payments (thanh toán đơn hàng)
  - Shippings (giao hàng, cập nhật trạng thái)

Ví dụ test nhanh:

```bash
GET http://localhost:8080/api/products
POST http://localhost:8080/api/orders
```

---

## Tài khoản mẫu

Có sẵn trong dữ liệu seed (Flyway migration):

- Admin: `admin@example.com` / `admin123`
- User: `john@example.com` / `password123`
- User: `jane@example.com` / `password456`

---

## Ghi chú

- Đây là bản **backend API** thuần (không có frontend).
- Áp dụng **OOP, SOLID, IoC/DI, Design Patterns** như: Factory, Strategy, State, Observer, Builder...
- Có thể mở rộng thêm tính năng: search nâng cao, phân quyền, cache proxy.
