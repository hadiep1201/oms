# Dự án PTTKHT OMS - Online Media Store - HK 2025.2

## Giới thiệu tổng quan

**OMS (Online Media Store)** là nền tảng thương mại điện tử chuyên kinh doanh các sản phẩm truyền thông vật lý gồm Sách, Báo, CD và DVD. Hệ thống được phát triển trong khuôn khổ môn học Phân tích thiết kế hệ thống (HK 2025.2).

Hệ thống bao gồm hai thành phần chính:
- **Frontend (OMS-FE)**: Giao diện người dùng xây dựng bằng Angular, hỗ trợ khách hàng mua sắm và quản lý dành cho nhân viên.
- **Backend (OMS)**: API Server xây dựng bằng Spring Boot, cung cấp các dịch vụ quản lý sản phẩm, đơn hàng, người dùng và tích hợp thanh toán (VietQR, PayPal).

---

## Hướng dẫn chạy dự án

### 1. Chạy Backend (Spring Boot)

**Yêu cầu:** Java 21+, PostgreSQL (port `5435`).

1. Mở terminal tại thư mục `Programming/oms`.
2. Đảm bảo đã có file `.env` cấu hình Database, JWT và các cổng thanh toán.
3. Chạy lệnh khởi động:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Backend sẽ chạy tại: `http://localhost:8080/oms`

### 2. Chạy Frontend (Angular)

**Yêu cầu:** Node.js, npm, Angular CLI.

1. Mở terminal tại thư mục `Programming/oms-fe`.
2. Cài đặt các gói phụ thuộc (chỉ cần chạy lần đầu):
   ```bash
   npm install
   ```
3. Khởi động máy chủ phát triển:
   ```bash
   npm start
   ```
4. Mở trình duyệt và truy cập: `http://localhost:4200`

> **Lưu ý:** Đảm bảo Backend (cổng 8080) đang chạy để Frontend có thể gọi được các API dữ liệu.
