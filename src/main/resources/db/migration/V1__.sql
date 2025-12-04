-- ============================
-- V1__init.sql - UPDATED
-- Mini-Ecommerce with Advanced Payment & Shipping
-- ============================

-- USERS
CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255)              NOT NULL,
    role       VARCHAR(20)               NOT NULL,
    email      VARCHAR(255)              NOT NULL UNIQUE,
    password   VARCHAR(255)              NOT NULL,
    created_at DATETIME                           DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME                           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME                           DEFAULT NULL
);


-- CATEGORIES
CREATE TABLE categories
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  DATETIME DEFAULT NULL
);

-- PRODUCTS
CREATE TABLE products
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255)   NOT NULL,
    description    TEXT,
    price          DECIMAL(12, 2) DEFAULT 0 NOT NULL,
    stock_quantity INT            DEFAULT 0 NOT NULL,
    category_id    BIGINT NOT NULL,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at     DATETIME DEFAULT NULL,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- ORDERS (UPDATED with COMPLETED status)
CREATE TABLE orders
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    total_amount DECIMAL(12, 2)                                                      NOT NULL,
    status       ENUM ('CREATED','PENDING_SHIPMENT','PENDING_PAYMENT','COMPLETED','CANCELLED') NOT NULL DEFAULT 'CREATED',
    created_at   DATETIME                                                                     DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME                                                                     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   DATETIME                                                                     DEFAULT NULL,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ORDER ITEMS
CREATE TABLE order_items
(
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT   DEFAULT 1         NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_orderitem_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- PAYMENTS (UPDATED with advanced fields)
CREATE TABLE payments
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT,
    amount          DECIMAL(12, 2)                          NOT NULL,
    payment_method  VARCHAR(100)                            NOT NULL,
    status          ENUM ('PENDING','SUCCESS','FAILED')     NOT NULL DEFAULT 'PENDING',
    paid_at         DATETIME                                         DEFAULT NULL,
    created_at      DATETIME                                         DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME                                         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME                                         DEFAULT NULL,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders (id),
    INDEX idx_payment_method (payment_method),
    INDEX idx_payment_status (status),
    INDEX idx_payment_created (created_at)
);

-- SHIPMENTS (UPDATED with advanced fields, renamed from SHIPPINGS)
CREATE TABLE shipments
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id             BIGINT NOT NULL,
    shipped_by           BIGINT DEFAULT NULL,
    address     VARCHAR(255),
    method               ENUM ('STANDARD','EXPRESS') NOT NULL DEFAULT 'STANDARD',
    fee                  DECIMAL(12, 2)                          NOT NULL, 
    status               ENUM ('CREATED','SHIPPING','COMPLETED','REJECTED') NOT NULL DEFAULT 'CREATED',
    shipped_at           DATETIME                                           DEFAULT NULL,
    delivered_at         DATETIME                                          DEFAULT NULL,
    notes                VARCHAR(255)                                       DEFAULT NULL,
    created_at           DATETIME                                           DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME                                           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at           DATETIME                                           DEFAULT NULL,
    CONSTRAINT fk_shipment_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_shipment_carrier FOREIGN KEY (shipped_by) REFERENCES users (id),
    INDEX idx_carrier (shipped_by),
    INDEX idx_status (status),
    INDEX idx_shipped_at (shipped_at)
);





-- ============================
-- SAMPLE DATA
-- ============================

-- USERS
INSERT INTO users (name, role , email, password)
VALUES ('Admin User', 'ADMIN','admin@example.com','admin123'),
       ('John Doe', 'CUSTOMER' ,'john@example.com', 'password123'),
       ('Jane Smith', 'STAFF','jane@example.com', 'password456'),
       ('Bob Johnson', 'STAFF','bob@example.com', 'password789'),
       ('Alice Brown', 'CUSTOMER','alice@example.com', 'password999');

-- CATEGORIES
INSERT INTO categories (name, description) VALUES
        ('Điện thoại & Phụ kiện',       'Smartphone, ốp lưng, sạc dự phòng'),
        ('Laptop & Máy tính',           'Laptop gaming, văn phòng, linh kiện PC'),
        ('Thời trang Nam',              'Áo thun, quần jeans, giày sneaker nam'),
        ('Thời trang Nữ',               'Váy, áo kiểu, túi xách nữ'),
        ('Mỹ phẩm & Chăm sóc da',       'Son, kem dưỡng, mặt nạ');

-- PRODUCTS
INSERT INTO products (name, description, price, stock_quantity, category_id) VALUES

-- 1. Điện thoại & Phụ kiện (10 sản phẩm)
    ('iPhone 15 Pro Max 256GB', 'Chính hãng VN/A', 32990000, 8, 1),
    ('Samsung Galaxy S24 Ultra', '12GB/256GB', 28990000, 12, 1),
    ('Ốp lưng iPhone 15 MagSafe', 'Chống sốc cao cấp', 350000, 120, 1),
    ('Sạc dự phòng 20000mAh PD 65W', 'Xiaomi chính hãng', 790000, 85, 1),
    ('Cáp sạc Type-C to Lightning', 'Baseus 20W', 150000, 200, 1),
    ('Kính cường lực iPhone 15 Pro', 'Full màn hình', 89000, 300, 1),
    ('Tai nghe AirPods Pro 2', 'Chính hãng Apple', 5790000, 15, 1),
    ('Xiaomi 14 12GB/512GB', 'New 2025', 18990000, 20, 1),
    ('Ốp lưng trong suốt Samsung S24', 'Chính hãng', 220000, 150, 1),
    ('Pin dự phòng Anker 10000mAh', 'Nhỏ gọn', 590000, 100, 1),

-- 2. Laptop & Máy tính (10 sản phẩm)
    ('MacBook Air M3 2025', '8GB/256GB', 26990000, 6, 2),
    ('ASUS ROG Strix G16', 'i9-13980HX, RTX 4070', 48990000, 4, 2),
    ('Dell XPS 14 2025', 'OLED 3.2K', 42990000, 5, 2),
    ('Laptop Acer Nitro 5', 'i5-13420H, RTX 3050', 18990000, 15, 2),
    ('Bàn phím cơ Keychron K8 Pro', 'Hotswap RGB', 2190000, 40, 2),
    ('Chuột Logitech MX Master 3S', 'Không dây cao cấp', 2590000, 60, 2),
    ('RAM DDR5 32GB 6000MHz', 'Corsair Vengeance', 3890000, 35, 2),
    ('Ổ cứng SSD NVMe 2TB', 'Samsung 990 Pro', 4590000, 50, 2),
    ('Màn hình 27" 4K Dell U2723QE', 'IPS Black', 10990000, 10, 2),
    ('Laptop Lenovo ThinkPad X1 Carbon Gen 12', 'Doanh nhân', 38990000, 8, 2),

-- 3. Thời trang Nam (10 sản phẩm)
    ('Áo thun Unisex Oversize', 'Cotton 100%', 189000, 200, 3),
    ('Quần jeans nam slimfit', 'Đen trơn', 450000, 120, 3),
    ('Giày sneaker nam trắng', 'Da PU cao cấp', 690000, 80, 3),
    ('Áo sơ mi nam dài tay', 'Oxford chống nhăn', 390000, 150, 3),
    ('Dây nịt da bò thật', 'Khóa tự động', 350000, 100, 3),
    ('Mũ lưỡi trai MLB NY', 'Chính hãng', 790000, 90, 3),
    ('Áo khoác bomber nam', 'Chống nước nhẹ', 590000, 70, 3),
    ('Quần short kaki nam', 'Nhiều màu', 280000, 180, 3),
    ('Tất vớ nam cổ ngắn', 'Combo 5 đôi', 99000, 300, 3),
    ('Áo polo nam cao cấp', 'Vải cá sấu', 320000, 140, 3),

-- 4. Thời trang Nữ (10 sản phẩm)
    ('Váy maxi hoa nhí', 'Dáng dài thanh lịch', 420000, 90, 4),
    ('Áo croptop nữ', 'Ôm dáng sexy', 189000, 200, 4),
    ('Túi xách nữ đeo chéo', 'Da PU cao cấp', 590000, 75, 4),
    ('Giày cao gót 7cm', 'Mũi nhọn đen', 680000, 60, 4),
    ('Khăn choàng cổ nữ', 'Lụa tơ tằm', 350000, 120, 4),
    ('Set đồ bộ nữ mặc nhà', 'Thun cotton', 290000, 150, 4),
    ('Áo len cổ lọ nữ', 'Dày dặn mùa đông', 390000, 100, 4),
    ('Quần ống suông nữ', 'Cạp cao', 450000, 110, 4),
    ('Bông tai bạc 925', 'Hình trái tim', 179000, 180, 4),
    ('Váy công sở dáng A', 'Xanh pastel', 520000, 80, 4),

-- 5. Mỹ phẩm & Chăm sóc da (10 sản phẩm)
    ('Son 3CE Velvet Lip Tint', 'Màu đỏ gạch', 350000, 200, 5),
    ('Kem chống nắng Skin1004', '50ml SPF50+', 289000, 180, 5),
    ('Mặt nạ dưỡng da Mediheal', 'Combo 10 miếng', 250000, 150, 5),
    ('Nước tẩy trang Bioderma 500ml', 'Hồng dành da nhạy cảm', 420000, 120, 5),
    ('Serum Vitamin C Some By Mi', '20% Vitamin C', 389000, 100, 5),
    ('Kem dưỡng ẩm Cetaphil 453g', 'Da khô', 450000, 90, 5),
    ('Mascara Maybelline Sky High', 'Làm dài mi', 259000, 200, 5),
    ('Phấn nước Cushion Laneige', 'Tone 21', 790000, 70, 5),
    ('Xịt khoáng Evian 300ml', 'Nhập khẩu Pháp', 220000, 150, 5),
    ('Toner Some By Mi AHA-BHA 150ml', 'Se khít lỗ chân lông', 320000, 130, 5);
-- ORDERS (with dates across multiple months in 2024)
INSERT INTO orders (user_id, total_amount, status, created_at)
VALUES
-- January 2024
(2, 200000, 'COMPLETED', '2024-01-15'),
(3, 150000, 'COMPLETED', '2024-01-20'),
-- April 2024
(4, 180000, 'COMPLETED', '2024-04-10'),
(5, 220000, 'COMPLETED', '2024-04-25'),
-- July 2024
(2, 195000, 'COMPLETED', '2024-07-08'),
(3, 310000, 'COMPLETED', '2024-07-22'),
-- October 2024
(4, 250000, 'COMPLETED', '2024-10-05'),
(5, 165000, 'COMPLETED', '2024-10-18'),
-- Current month (November/December 2024)
(2, 95000, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 220000, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 155000, 'CREATED', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ORDER ITEMS (Fixed NULL values and additional items)
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES
-- Order 1 (Jan)
(1, 1, 2, 100000),
-- Order 2 (Jan)
(2, 3, 2, 50000),
(2, 5, 1, 30000),
-- Order 3 (Apr)
(3, 1, 1, 100000),
(3, 2, 1, 150000),
-- Order 4 (Apr)
(4, 4, 2, 80000),
(4, 7, 1, 35000),
-- Order 5 (Jul)
(5, 6, 1, 75000),
(5, 3, 2.5, 50000),
-- Order 6 (Jul)
(6, 2, 2, 150000),
(6, 4, 1, 80000),
-- Order 7 (Oct)
(7, 1, 2, 100000),
(7, 7, 1, 35000),
-- Order 8 (Oct)
(8, 5, 2, 20000),
(8, 6, 1, 75000),
-- Order 9 (Nov/Dec - Recent)
(9, 1, 1, 75000),
(9, 3, 1, 20000),
-- Order 10 (Nov/Dec - Recent)
(10, 2, 1, 150000),
(10, 4, 0.5, 40000),
-- Order 11 (Pending - not enough items)
(11, 1, 1, 100000);

-- PAYMENTS (with dates matching orders and various payment methods/statuses)
INSERT INTO payments (order_id, amount, payment_method, status, paid_at, created_at)
VALUES
-- January 2024
(1, 200000, 'CREDIT_CARD', 'SUCCESS', '2024-01-15', '2024-01-15'),
(2, 150000, 'BANK_TRANSFER', 'SUCCESS', '2024-01-20', '2024-01-20'),
-- April 2024
(3, 180000, 'E_WALLET', 'SUCCESS', '2024-04-10', '2024-04-10'),
(4, 220000, 'CREDIT_CARD', 'SUCCESS', '2024-04-25', '2024-04-25'),
-- July 2024
(5, 195000, 'BANK_TRANSFER', 'SUCCESS', '2024-07-08', '2024-07-08'),
(6, 310000, 'E_WALLET', 'SUCCESS', '2024-07-22', '2024-07-22'),
-- October 2024
(7, 250000, 'CREDIT_CARD', 'SUCCESS', '2024-10-05', '2024-10-05'),
(8, 165000, 'CASH', 'SUCCESS', '2024-10-18', '2024-10-18'),
-- Recent (Current month)
(9, 95000, 'CREDIT_CARD', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 220000, 'BANK_TRANSFER', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
-- Failed payment
(11, 155000, 'CREDIT_CARD', 'FAILED', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- Additional failed payment for analytics
(1, 200000, 'E_WALLET', 'FAILED', NULL, '2024-01-14');

-- SHIPMENTS (Cập nhật theo schema mới với dữ liệu tiếng Việt - Tất cả TP.HCM)
INSERT INTO shipments (order_id, shipped_by, address, method, fee, status, shipped_at, delivered_at, notes, created_at)
VALUES
-- Tháng 1/2024 - Đơn hàng đã hoàn thành
(1, 1, '123 Đường Nguyễn Du, Phường Bến Nghé, Quận 1, TP.HCM', 'STANDARD', 25000.00, 'COMPLETED', '2024-01-16 10:00:00', '2024-01-17 14:30:00', 'Giao hàng thành công, khách hài lòng', '2024-01-15 09:00:00'),
(2, 1, '456 Đường Lê Lợi, Phường 4, Quận Tân Bình, TP.HCM', 'STANDARD', 30000.00, 'COMPLETED', '2024-01-21 11:15:00', '2024-01-22 16:45:00', 'Khách nhận hàng tại nhà', '2024-01-20 08:30:00'),
-- Tháng 4/2024 - Đơn hàng đã hoàn thành
(3, 1, '789 Đường Trần Hưng Đạo, Phường Cô Giang, Quận 1, TP.HCM', 'EXPRESS', 45000.00, 'COMPLETED', '2024-04-11 09:30:00', '2024-04-13 13:20:00', 'Giao hàng đúng hẹn', '2024-04-10 14:00:00'),
(4, 1, '321 Đường Hoàng Quốc Việt, Phường Phú Trung, Quận Tân Phú, TP.HCM', 'STANDARD', 28000.00, 'COMPLETED', '2024-04-26 08:45:00', '2024-04-28 12:10:00', 'Khách hàng feedback tốt', '2024-04-25 10:15:00'),
-- Tháng 7/2024 - Đơn hàng đã hoàn thành
(5, 1, '654 Đường Phạm Ngũ Lão, Phường Phạm Ngũ Lão, Quận 1, TP.HCM', 'EXPRESS', 42000.00, 'COMPLETED', '2024-07-09 15:20:00', '2024-07-10 11:30:00', 'Giao hàng nhanh chóng', '2024-07-08 13:45:00'),
(6, 1, '987 Đường Lý Thường Kiệt, Phường 7, Quận Tân Bình, TP.HCM', 'STANDARD', 31000.00, 'COMPLETED', '2024-07-23 10:00:00', '2024-07-24 17:15:00', 'Hoàn thành xuất sắc', '2024-07-22 09:30:00'),
-- Tháng 10/2024 - Đơn hàng đã hoàn thành
(7, 1, '654 Đường Bà Triệu, Phường Đa Kao, Quận 1, TP.HCM', 'EXPRESS', 48000.00, 'COMPLETED', '2024-10-06 14:30:00', '2024-10-08 10:45:00', 'Khách hàng rất hài lòng', '2024-10-05 11:20:00'),
(8, 1, '789 Đường Võ Văn Tần, Phường 5, Quận 3, TP.HCM', 'STANDARD', 26000.00, 'COMPLETED', '2024-10-19 09:15:00', '2024-10-20 16:20:00', 'Giao hàng đúng địa chỉ', '2024-10-18 08:00:00'),
-- Đơn hàng gần đây (tháng hiện tại)
(9, 1, '123 Đường Cách Mạng Tháng 8, Phường Bến Thành, Quận 1, TP.HCM', 'EXPRESS', 50000.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 HOUR), 'Đơn hàng giao thành công', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 1, '456 Đường Nguyễn Thị Minh Khai, Phường 6, Quận 3, TP.HCM', 'STANDARD', 35000.00, 'SHIPPING', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, 'Đang vận chuyển đến khách hàng', DATE_SUB(NOW(), INTERVAL 2 DAY)),
-- Đơn hàng mới tạo - chưa giao
(11, NULL, '789 Đường Hùng Vương, Phường Nguyễn Thái Bình, Quận 1, TP.HCM', 'STANDARD', 0.00, 'CREATED', NULL, NULL, 'Chờ xử lý đơn hàng', DATE_SUB(NOW(), INTERVAL 1 DAY));




