# Payment & Shipment Documentation

## Tổng quan hệ thống Payment & Shipment

Hệ thống Mini-Ecommerce có 2 module chính liên quan đến thanh toán và giao hàng:
- **Payment**: Xử lý thanh toán cho đơn hàng
- **Shipment**: Xử lý vận chuyển và giao hàng

Hai module này hoạt động song song và phối hợp thông qua **OrderProcessingMediator** để cập nhật trạng thái đơn hàng.

---

## 1. Database Schema

### Bảng `payments`
Lưu trữ thông tin thanh toán cho từng đơn hàng.

| Cột | Kiểu dữ liệu | Mô tả | Constraints |
|-----|--------------|-------|-------------|
| `id` | BIGINT | Primary key | AUTO_INCREMENT, PRIMARY KEY |
| `order_id` | BIGINT | ID đơn hàng liên kết | NOT NULL, FK -> orders.id |
| `amount` | DECIMAL(12,2) | Số tiền thanh toán | NOT NULL |
| `payment_method` | VARCHAR(100) | Phương thức thanh toán (CREDIT_CARD, BANK_TRANSFER, E_WALLET, CASH) | NOT NULL |
| `status` | ENUM | Trạng thái thanh toán | NOT NULL, DEFAULT 'PENDING'<br>Values: PENDING, SUCCESS, FAILED |
| `paid_at` | DATETIME | Thời gian thanh toán thành công | NULL |
| `created_at` | DATETIME | Thời gian tạo | DEFAULT CURRENT_TIMESTAMP |
| `updated_at` | DATETIME | Thời gian cập nhật | DEFAULT CURRENT_TIMESTAMP ON UPDATE |
| `deleted_at` | DATETIME | Soft delete | DEFAULT NULL |

**Indexes:**
- `idx_payment_method` trên `payment_method`
- `idx_payment_status` trên `status`
- `idx_payment_created` trên `created_at`

**Quan hệ:**
- Many-to-One với bảng `orders` qua `order_id`
- Một đơn hàng có thể có nhiều payment (cho phép retry payment thất bại)

### Bảng `shipments`
Lưu trữ thông tin vận chuyển cho từng đơn hàng.

| Cột | Kiểu dữ liệu | Mô tả | Constraints |
|-----|--------------|-------|-------------|
| `id` | BIGINT | Primary key | AUTO_INCREMENT, PRIMARY KEY |
| `order_id` | BIGINT | ID đơn hàng liên kết | NOT NULL, FK -> orders.id |
| `shipped_by` | BIGINT | ID nhân viên giao hàng | NULL, FK -> users.id |
| `address` | VARCHAR(255) | Địa chỉ giao hàng | NULL |
| `method` | ENUM | Phương thức vận chuyển | NOT NULL, DEFAULT 'STANDARD'<br>Values: STANDARD, EXPRESS |
| `fee` | DECIMAL(12,2) | Phí vận chuyển | NOT NULL |
| `status` | ENUM | Trạng thái vận chuyển | NOT NULL, DEFAULT 'CREATED'<br>Values: CREATED, SHIPPING, COMPLETED, REJECTED |
| `shipped_at` | DATETIME | Thời gian bắt đầu giao | NULL |
| `delivered_at` | DATETIME | Thời gian giao thành công | NULL |
| `notes` | VARCHAR(255) | Ghi chú thêm | NULL |
| `created_at` | DATETIME | Thời gian tạo | DEFAULT CURRENT_TIMESTAMP |
| `updated_at` | DATETIME | Thời gian cập nhật | DEFAULT CURRENT_TIMESTAMP ON UPDATE |
| `deleted_at` | DATETIME | Soft delete | DEFAULT NULL |

**Indexes:**
- `idx_carrier` trên `shipped_by`
- `idx_status` trên `status`
- `idx_shipped_at` trên `shipped_at`

**Quan hệ:**
- Many-to-One với bảng `orders` qua `order_id`
- Many-to-One với bảng `users` qua `shipped_by` (nhân viên giao hàng)
- Một đơn hàng chỉ có một shipment

---

## Quy trình đặt hàng

Quy trình đặt hàng trong hệ thống bao gồm các bước sau:

1. **Tạo đơn hàng**: Khách hàng tạo đơn hàng với thông tin sản phẩm, phương thức thanh toán và địa chỉ giao hàng
   - **API**: `POST /api/orders`
   - **Body**: ```json
     {
       "items": [
         {"productId": 1, "quantity": 2}
       ],
       "payment": {"method": "CREDIT_CARD"},
       "shipment": {
         "address": "123 Main Street, Ho Chi Minh City",
         "method": "STANDARD",
         "notes": "Please handle with care"
       }
     }
     ```
   - **Hệ thống tự động**: Tạo payment (trạng thái PENDING) và shipment (trạng thái CREATED)

2. **Xác nhận thanh toán**: Khách hàng thực hiện thanh toán và gọi API confirm payment
   - **API**: `POST /api/payments/confirm`
   - **Body**: ```json
     {
       "paymentId": 456,
       "success": true
     }
     ```
   - **Kết quả**: Payment status → SUCCESS, Order status → PENDING_SHIPMENT

3. **Nhận task giao hàng**: Nhân viên STAFF nhận task giao hàng (chỉ khi payment đã SUCCESS)
   - **API**: `PATCH /api/shipments/{id}`
   - **Header**: `userId: 789` (phải có role STAFF)
   - **Body**: ```json
     {
       "type": "ASSIGN_TASK",
       "status": "SHIPPING"
     }
     ```
   - **Validation**:
     - User phải có role STAFF
     - Shipment phải ở trạng thái CREATED
     - shippedBy sẽ được set bằng userId từ header
   - **Kết quả**: Shipment status → SHIPPING, Order status → PENDING_SHIPMENT

4. **Hoàn thành giao hàng**: Nhân viên STAFF hoàn thành giao hàng
   - **API**: `PATCH /api/shipments/{id}`
   - **Header**: `userId: 789` (phải trùng với nhân viên được assign và có role STAFF)
   - **Body**: ```json
     {
       "type": "COMPLETE_TASK",
       "status": "COMPLETED"
     }
     ```
   - **Validation**:
     - User phải có role STAFF
     - User phải là người được assign cho shipment (shippedBy == userId)
     - Shipment phải ở trạng thái SHIPPING
   - **Kết quả**: Shipment status → COMPLETED, Order status → COMPLETED

---

## 2. Controllers

### PaymentController (`/api/payments`)

| Endpoint | Method | Mô tả | Parameters | Response |
|----------|--------|-------|------------|----------|
| `/api/payments` | POST | Tạo payment mới cho đơn hàng | `CreatePaymentRequest`<br>- `orderId`: ID đơn hàng (required)<br>- `method`: Phương thức thanh toán (required) | `PaymentResponse` |
| `/api/payments/confirm` | POST | Xác nhận kết quả thanh toán | `ConfirmPaymentRequest`<br>- `paymentId`: ID payment (required)<br>- `success`: Kết quả thanh toán (required) | `PaymentResponse` |

**Chức năng:**
- **POST /api/payments**: Tạo payment mới với trạng thái PENDING
- **POST /api/payments/confirm**: Cập nhật trạng thái payment và thông báo cho OrderProcessingMediator

**Validation:**
- `orderId` phải tồn tại trong database
- `method` không được rỗng
- `paymentId` phải tồn tại trong database

### ShippingController (`/api/shipments`)

| Endpoint | Method | Mô tả | Parameters | Response |
|----------|--------|-------|------------|----------|
| `/api/shipments` | POST | Tạo shipment mới | `CreateShipmentRequest`<br>- `orderId`: ID đơn hàng (required)<br>- `shippedBy`: ID nhân viên giao (optional)<br>- `address`: Địa chỉ giao hàng (required)<br>- `method`: Phương thức vận chuyển (required)<br>- `fee`: Phí vận chuyển (required, >=0)<br>- `notes`: Ghi chú (optional) | `ShipmentResponse` |
| `/api/shipments/{id}` | PATCH | Cập nhật shipment | Path: `id` (ID shipment)<br>Header: `userId` (ID user thực hiện)<br>Body: `UpdateShipmentRequest`<br>- `type`: Loại update (ASSIGN_TASK/COMPLETE_TASK)<br>- `shippedBy`: ID nhân viên (cho ASSIGN_TASK)<br>- `status`: Trạng thái mới | `ShipmentResponse` |

**Chức năng:**
- **POST /api/shipments**: Tạo shipment với trạng thái CREATED
- **PATCH /api/shipments/{id}**: Cập nhật shipment theo 2 loại:
  - `ASSIGN_TASK`: Nhân viên nhận giao việc (cập nhật `shippedBy`, `status`, `shippedAt`)
  - `COMPLETE_TASK`: Nhân viên hoàn thành giao việc (cập nhật `status`, `deliveredAt`)

**Validation:**
- `orderId` phải tồn tại
- `shippedBy` phải là user hợp lệ (nếu có)
- `address`, `method`, `fee` không được rỗng
- `fee` >= 0
- `userId` (header) bắt buộc cho PATCH operations

---

## 3. Services

### PaymentService & PaymentServiceImpl

**Giao diện PaymentService:**
```java
Payment create(CreatePaymentRequest req);
Payment confirm(ConfirmPaymentRequest req);
```

**PaymentServiceImpl Logic:**

#### `create(CreatePaymentRequest req)`
1. **Validate**: Kiểm tra `orderId` tồn tại qua `OrderLookupService`
2. **Create Payment**: Sử dụng `PaymentFactory.fromCreateRequest()` để tạo Payment entity
   - `orderId`: Từ request
   - `amount`: Lấy từ `order.getTotalAmount()`
   - `paymentMethod`: Từ request
   - `status`: PENDING
3. **Persist**: Lưu vào database qua `PaymentRepository.save()`
4. **Return**: Query lại payment mới nhất cho order đó

#### `confirm(ConfirmPaymentRequest req)`
1. **Validate**: Tìm Payment theo `paymentId`
2. **Check duplicate**: Nếu payment đã SUCCESS và `success=true`, return existing
3. **Update status**:
   - Nếu `success=true`: status = SUCCESS, set `paidAt = LocalDateTime.now()`
   - Nếu `success=false`: status = FAILED
4. **Notify mediator**: Gọi `mediator.notifyPaymentConfirmed(payment, success)`
5. **Persist**: Cập nhật database qua `PaymentRepository.update()`

**Business Rules:**
- Một order có thể có nhiều payments (cho retry failed payments)
- Payment confirmation có thể được gọi nhiều lần (idempotent cho SUCCESS)
- Payment FAILED không set `paidAt`

### ShippingService & ShippingServiceImpl

**ShippingServiceImpl Logic:**

#### `create(CreateShipmentRequest req)`
1. **Validate**: Kiểm tra `orderId` tồn tại qua `OrderLookupService`
2. **Validate user**: Nếu `shippedBy` có giá trị, kiểm tra user tồn tại
3. **Create Shipping**: Sử dụng `ShippingFactory.fromCreateRequest()` để tạo Shipping entity
   - `orderId`, `address`, `method`, `fee`, `notes`: Từ request
   - `shippedBy`: User entity (nếu có)
   - `status`: CREATED
4. **Persist**: Lưu vào database qua `ShippingRepository.save()`
5. **Notify mediator**: Gọi `mediator.notifyShipmentCreated(shipping)`
6. **Return**: Query lại shipping mới nhất cho order đó

#### `update(Long id, UpdateShipmentRequest req, Long userId)`
1. **Validate user role**: Kiểm tra user có role STAFF qua `UserRoleRepository.getRoleNameByUserId(userId)`
2. **Find shipping**: Tìm theo `id`
3. **Validate by type**:
   - **ASSIGN_TASK**: Chỉ khi `status == CREATED`
   - **COMPLETE_TASK**: Chỉ khi `shippedById == userId` và `status == SHIPPING`
4. **Get user**: Lấy user từ `userId` (từ header) thay vì `req.shippedBy()`
5. **Apply update**: Sử dụng `ShippingFactory.applyUpdate(s, req, shippedBy, userId)`
   - **ASSIGN_TASK**: Validate `shippedBy.getId() == userId`, cập nhật `shippedBy`, `status`, `shippedAt`
   - **COMPLETE_TASK**: Validate `s.getShippedById() == userId`, cập nhật `status`, `deliveredAt`
6. **Notify mediator**: Gọi `mediator.notifyShipmentUpdated(shipping)`
7. **Persist**: Cập nhật database

**Business Rules:**
- Một order chỉ có một shipment
- **Chỉ user có role STAFF mới có thể thực hiện các thao tác shipping**
- `shippedBy` được lấy từ `userId` trong header, không cần trong request body
- `ASSIGN_TASK`: `shippedBy` phải khớp với `userId` từ header
- Chỉ nhân viên được assign (shippedBy == userId) mới có thể complete task
- `ASSIGN_TASK` chỉ cho shipment ở trạng thái CREATED
- `COMPLETE_TASK` chỉ cho shipment ở trạng thái SHIPPING
- Timestamp tự động set khi chuyển trạng thái

---

## 4. Factories

### PaymentFactory

**Chức năng:** Tạo và cập nhật Payment entities

#### `fromCreateRequest(CreatePaymentRequest req, Order order)`
- Tạo Payment mới với dữ liệu từ request và order
- Set `amount = order.getTotalAmount()`
- Set `status = PENDING`
- Không set `paidAt`

#### `applyConfirm(Payment p, boolean success)` - *Deprecated*
- Logic đã được chuyển vào PaymentServiceImpl

### ShippingFactory

**Chức năng:** Tạo và cập nhật Shipping entities

#### `fromCreateRequest(CreateShipmentRequest req, Order order, User shippedBy)`
- Tạo Shipping mới với dữ liệu từ request
- Set `order`, `orderId`, `shippedBy`, `shippedById`
- Set `method` từ enum parsing
- Set `status = CREATED`

#### `applyUpdate(Shipping s, UpdateShipmentRequest req, User shippedBy, Long userId)`
**ASSIGN_TASK:**
- Validate `shippedBy.getId() == userId` (báo lỗi nếu không khớp)
- Cập nhật `shippedBy` và `shippedById`
- Parse và set `status` từ string
- Nếu status chuyển sang SHIPPING và `shippedAt == null`: set `shippedAt = LocalDateTime.now()`

**COMPLETE_TASK:**
- Validate `s.getShippedById() == userId` (báo lỗi nếu không phải người assign)
- Parse và set `status` từ string
- Nếu status chuyển sang COMPLETED và `deliveredAt == null`: set `deliveredAt = LocalDateTime.now()`

---

## 5. Repositories

### PaymentRepository

**Methods:**
- `save(Payment payment)`: Insert payment mới
- `update(Payment payment)`: Update payment theo id
- `findById(Long id)`: Query payment theo id
- `findByOrderId(Long orderId)`: Query tất cả payments của một order

**SQL Operations:**
- **save**: INSERT với các trường: order_id, amount, payment_method, status, created_at, updated_at
- **update**: UPDATE amount, payment_method, status, paid_at, updated_at WHERE id = ?
- **findById/findByOrderId**: SELECT với mapping ResultSet -> Payment entity

### ShippingRepository

**Methods:**
- `save(Shipping shipping)`: Insert shipping mới
- `update(Shipping shipping)`: Update shipping theo id
- `findById(Long id)`: Query shipping theo id
- `findByOrderId(Long orderId)`: Query tất cả shipments của một order

**SQL Operations:**
- **save**: INSERT với các trường: order_id, shipped_by, address, method, fee, status, created_at, updated_at
- **update**: UPDATE shipped_by, address, method, fee, status, shipped_at, delivered_at, notes, updated_at WHERE id = ?
- **findById/findByOrderId**: SELECT với mapping ResultSet -> Shipping entity

---

## 6. OrderProcessingMediator

**Chức năng:** Điều phối giữa Payment/Shipping services và Order state machine

### `notifyPaymentConfirmed(Payment payment, boolean success)`
- Tìm Order theo `payment.getOrderId()`
- Nếu `success=true`: gọi `order.handlePaymentSuccess()`
- Nếu `success=false`: gọi `order.handlePaymentFailed()`
- Lưu Order đã cập nhật

### `notifyShipmentCreated(Shipping shipping)`
- Tìm Order theo `shipping.getOrderId()`
- Gọi `order.handleShipmentCreated()`
- Lưu Order đã cập nhật

### `notifyShipmentUpdated(Shipping shipping)`
- Tìm Order theo `shipping.getOrderId()`
- Theo `shipping.getStatus()`:
  - `SHIPPING`: gọi `order.handleShipmentStarted()`
  - `COMPLETED`: gọi `order.handleShipmentDelivered()`
- Lưu Order đã cập nhật

---

## 7. Order State Machine

Hệ thống sử dụng State Pattern để quản lý trạng thái đơn hàng.

### Các trạng thái Order:
1. **CREATED**: Đơn hàng vừa tạo
2. **PENDING_PAYMENT**: Đang chờ thanh toán (sau khi tạo shipment)
3. **PENDING_SHIPMENT**: Đang chờ giao hàng (sau khi payment thành công)
4. **COMPLETED**: Hoàn thành
5. **CANCELLED**: Đã hủy

### State Transitions:

```
CREATED ────────▶ PENDING_PAYMENT ────────▶ PENDING_SHIPMENT ────────▶ COMPLETED
    │                     │                         │
    │                     │                         │
    ▼                     ▼                         ▼
 CANCELLED ◄──────────────┼─────────────────────────┼───────────────────┘
                          │                         │
                          └─────────────────────────┼───────────────────▶ CANCELLED
                                                    │
                                                    ▼
                                                 CANCELLED
```

### Payment/Shipment ảnh hưởng đến Order State:

| Event | Từ State | Đến State | Điều kiện |
|-------|----------|-----------|-----------|
| Shipment Created | CREATED | PENDING_PAYMENT | Luôn |
| Payment Success | PENDING_PAYMENT | PENDING_SHIPMENT | Luôn |
| Payment Failed | PENDING_PAYMENT | CREATED | Có thể retry |
| Payment Failed | PENDING_PAYMENT | CANCELLED | Có thể hủy |
| Shipment Started | PENDING_SHIPMENT | PENDING_SHIPMENT | No change |
| Shipment Delivered | PENDING_SHIPMENT | COMPLETED | Luôn |

---

## 8. Business Rules & Constraints

### Payment Rules:
- Một order có thể có nhiều payments (cho retry failed payments)
- Payment phải có method hợp lệ: CREDIT_CARD, BANK_TRANSFER, E_WALLET, CASH
- Payment SUCCESS phải có `paid_at` timestamp
- Payment FAILED không có `paid_at`
- Không thể confirm payment đã SUCCESS lại thành FAILED

### Shipment Rules:
- Một order chỉ có duy nhất một shipment
- Shipment phải có địa chỉ giao hàng
- Shipment phải có method hợp lệ: STANDARD, EXPRESS
- Shipment phải có fee >= 0
- **Chỉ user có role STAFF mới có thể thực hiện các thao tác shipping**
- `shippedBy` được lấy từ `userId` trong header, không cần trong request body
- **ASSIGN_TASK**: `shippedBy` phải khớp với `userId` từ header
- **COMPLETE_TASK**: Chỉ user được assign (shippedBy == userId) mới có thể complete
- ASSIGN_TASK chỉ cho shipment CREATED
- COMPLETE_TASK chỉ cho shipment SHIPPING
- Shipped_at tự động set khi chuyển sang SHIPPING
- Delivered_at tự động set khi chuyển sang COMPLETED

### Order State Rules:
- Phải tạo shipment trước khi thanh toán
- Phải thanh toán thành công trước khi giao hàng
- Không thể giao hàng cho đơn hàng chưa thanh toán
- Đơn hàng COMPLETED không thể thay đổi trạng thái

---

## 9. Data Flow

### Tạo đơn hàng mới:
1. User tạo order → Status: CREATED
2. Admin tạo shipment → Status: PENDING_PAYMENT, Order state: PENDING_PAYMENT
3. User thanh toán → Payment status: SUCCESS, Order state: PENDING_SHIPMENT
4. Nhân viên giao hàng → Shipment status: SHIPPING
5. Nhân viên hoàn thành → Shipment status: COMPLETED, Order state: COMPLETED

### Retry Payment Flow:
1. Payment thất bại → Payment status: FAILED, Order state: CREATED
2. Tạo payment mới → Payment status: PENDING
3. Thanh toán lại → Payment status: SUCCESS, Order state: PENDING_SHIPMENT

---

## 10. Error Handling

### Payment Errors:
- Order không tồn tại: `IllegalArgumentException`
- Payment không tồn tại: `IllegalArgumentException`
- Payment đã confirm: Log warning, return existing payment

### Shipment Errors:
- Order không tồn tại: `IllegalArgumentException`
- Shipment không tồn tại: `IllegalArgumentException`
- User không tồn tại: `IllegalArgumentException`
- **User không có role STAFF**: `IllegalArgumentException` ("Chỉ nhân viên (STAFF) mới có thể thực hiện thao tác giao hàng!")
- **shippedBy không khớp với userId**: `IllegalArgumentException` ("shippedBy phải khớp với userId từ header!")
- **User không phải người assign**: `IllegalArgumentException` ("Chỉ nhân viên được assign mới có thể hoàn thành task!")
- Business rule violations: `IllegalArgumentException` với message tiếng Việt

### Validation Errors:
- Missing required fields: `@Valid` annotation throws validation exceptions
- Invalid data types: Spring Boot validation
- Database constraint violations: SQL exceptions

---

*Tài liệu được tạo tự động dựa trên phân tích codebase Mini-Ecommerce. Cập nhật lần cuối: December 5, 2025 (thêm validation role STAFF và sửa logic shippedBy)*
