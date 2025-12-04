# Mini Ecommerce UI

Giao diện đơn giản cho hệ thống Mini Ecommerce, được xây dựng bằng HTML, CSS, JavaScript và Bootstrap.

## Cấu trúc File

```
ui/
├── index.html      # Trang chính với authentication + main app
├── demo.html       # Version demo offline với authentication
├── styles.css      # CSS styling cho responsive và UI
├── api.js         # API utility functions
├── app.js         # Logic chính của ứng dụng
└── README.md      # Tài liệu này
```

## Tính năng

### 🛒 Giỏ hàng (bên trái - 20%)
- Hiển thị danh sách sản phẩm trong giỏ
- Điều khiển số lượng (+/-) với giới hạn kho
- Xóa sản phẩm khỏi giỏ
- Nút "Đặt hàng" khi có sản phẩm trong giỏ

### 📦 Sản phẩm (bên phải - 80%)
- Ô tìm kiếm sản phẩm theo tên
- Hiển thị sản phẩm dạng grid với thông tin:
  - Tên sản phẩm
  - Mô tả
  - Giá (VNĐ)
  - Số lượng tồn kho
- Nút "Thêm vào giỏ" với kiểm tra tồn kho
- Phân trang với thông tin tổng số sản phẩm

### 🔐 Xác thực người dùng
- **Màn hình đăng nhập**: Form đăng nhập cơ bản với username/password
- **Kiểm tra trạng thái**: Tự động hiển thị login hoặc main screen
- **Đăng xuất**: Logout từ dropdown menu
- **Demo login**: admin / admin123 (cho testing)

### 🧭 Điều hướng (Navigation)
- **Sản phẩm**: Màn hình mua hàng chính với giỏ hàng
- **Đơn hàng của tôi**: Lịch sử đơn hàng với chi tiết
- **User menu**: Thông tin user và logout

### 📋 Đơn hàng của tôi
- Danh sách đơn hàng từ API server (`GET /api/orders/me`)
- Chi tiết sản phẩm với tên sản phẩm từ API
- Trạng thái đơn hàng (Created, Pending Payment, Pending Shipment, Completed, Cancelled)
- Tổng tiền và thông tin user

## API Endpoints

### Sản phẩm
```
GET /api/products?page={page}&size={size}&keyword={keyword}
```
- `page`: Trang hiện tại (bắt đầu từ 0)
- `size`: Số sản phẩm mỗi trang
- `keyword`: Từ khóa tìm kiếm

### Đặt hàng
```
POST /api/orders
Headers: userId={userId}
Body: {
  "items": [
    {
      "productId": {id},
      "quantity": {số lượng}
    }
  ]
}
```

### Lịch sử đơn hàng
```
GET /api/orders/me
Headers: userId={userId}
```

### Chi tiết sản phẩm
```
GET /api/products/{id}
```

## Cách chạy

### 1. Khởi động Backend
```bash
# Chạy Spring Boot backend trên port 8080
./mvnw spring-boot:run
```

### 2. Chạy Frontend

#### Option A: Test với Backend thật
```bash
# Python 3
python -m http.server 3000

# Node.js
npx http-server ui -p 3000
```
Truy cập: `http://localhost:3000/index.html`

#### Option B: Test Demo Offline (không cần backend)
```bash
# Mở trực tiếp file trong trình duyệt
# Hoặc dùng web server như trên
```
Truy cập: `http://localhost:3000/demo.html`

### 3. Cấu hình Backend URL
Backend đã được cấu hình chạy trên port 8081, UI đã được cập nhật tương ứng:
```javascript
BASE_URL: 'http://localhost:8081' // Đã được cấu hình đúng
```

**Lưu ý CORS**: Backend đã được cấu hình để cho phép frontend từ `localhost:*` gọi API, nên không cần lo lắng về lỗi CORS.

## Giao diện Responsive

- **Desktop**: Layout chia đôi 20:80
- **Mobile**: Layout xếp chồng, giỏ hàng ở dưới

## Mở rộng

Giao diện được thiết kế đơn giản và dễ mở rộng cho các bước tiếp theo:

1. **Thanh toán**: Thêm form thanh toán sau khi đặt hàng
2. **Địa chỉ giao hàng**: Thêm form nhập địa chỉ và shipping
3. **Đăng ký**: Thêm form đăng ký user mới
4. **Profile**: Trang quản lý thông tin cá nhân
5. **Admin Panel**: Giao diện quản trị (nếu có role admin)
6. **Toast notifications**: Thay alert bằng toast đẹp mắt
7. **Loading states**: Thêm skeleton loading cho UX tốt hơn
8. **Real authentication**: Tích hợp JWT hoặc session-based auth

## Demo Workflow

1. **Đăng nhập**: Nhập username: `admin`, password: `admin123`
2. **Tìm sản phẩm**: Nhập từ khóa vào ô search
3. **Thêm vào giỏ**: Click "Thêm vào giỏ" trên sản phẩm
4. **Điều chỉnh số lượng**: Sử dụng +/- trong giỏ hàng
5. **Đặt hàng**: Click "Đặt hàng" để tạo đơn hàng (gửi `POST /api/orders`)
6. **Xem đơn hàng**: Click "Đơn hàng của tôi" để xem lịch sử từ `GET /api/orders/me`
7. **Đăng xuất**: Click dropdown user > Đăng xuất

Dữ liệu sẽ được gửi về backend theo đúng API endpoints đã phân tích.
