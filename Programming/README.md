# OMS – Online Media Store (Backend)

## Giới thiệu tổng quan

**OMS** là hệ thống backend của nền tảng thương mại điện tử **Online Media Store** – chuyên kinh doanh các sản phẩm truyền thông vật lý gồm Sách, Báo, CD và DVD.

Backend cung cấp các REST API phục vụ:
- **Khách hàng**: Tra cứu sản phẩm, đặt hàng, thanh toán qua VietQR và PayPal.
- **Quản lý sản phẩm**: CRUD danh mục sản phẩm, xử lý và phê duyệt đơn hàng.
- **Quản trị viên**: Quản lý tài khoản nhân viên, phân quyền, đặt lại mật khẩu.

### Công nghệ sử dụng

| Thành phần | Công nghệ |
|-----------|----------|
| Framework | Spring Boot 3.5 |
| Ngôn ngữ | Java 21 |
| Cơ sở dữ liệu | PostgreSQL (port `5435`) |
| Bảo mật | JWT + Spring Security |
| ORM | Spring Data JPA / Hibernate |
| Thanh toán | VietQR API, PayPal REST API (Sandbox) |
| Build tool | Maven Wrapper (`mvnw`) |

---

## Hướng dẫn chạy dự án

### Yêu cầu cần có

- **Java** 21+
- **Maven** (hoặc dùng `mvnw` đi kèm – không cần cài riêng)
- **PostgreSQL** đang chạy tại port `5435`
- File `.env` được cấu hình đúng (xem mục bên dưới)

### Các bước thực hiện

#### 1. Cấu hình biến môi trường

Tạo file `.env` trong thư mục gốc của dự án backend (`oms/`) với nội dung:

```properties
# Database
POSTGRES_DB=oms
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_ACCESS_EXPIRATION_MS=3600000
JWT_REFRESH_EXPIRATION_DAYS=7

# VietQR (lấy từ dev.vietqr.org)
VIETQR_USERNAME=your_username
VIETQR_PASSWORD=your_password
VIETQR_BANK_CODE=your_bank_code
VIETQR_BANK_ACCOUNT=your_account_number
VIETQR_USER_BANK_NAME=your_bank_name

# PayPal Sandbox (lấy từ developer.paypal.com)
PAYPAL_CLIENT_ID=your_paypal_client_id
PAYPAL_SECRET_KEY=your_paypal_secret_key
```

#### 2. Khởi động backend

```bash
./mvnw spring-boot:run
```

Sau khi khởi động thành công, backend chạy tại:

```
http://localhost:8080/oms
```

Tất cả REST API có tiền tố:

```
http://localhost:8080/oms/api/
```

**Ví dụ:**

```
POST http://localhost:8080/oms/api/auth/login
GET  http://localhost:8080/oms/api/products
POST http://localhost:8080/oms/api/orders
```

#### 3. (Tuỳ chọn) Build artifact

```bash
./mvnw clean install
```

---

## Cấu trúc thư mục chính

```
src/main/java/com/example/aims/
├── controller/        ← Các REST Controller (API endpoints)
├── service/           ← Logic nghiệp vụ
├── entity/            ← Các lớp JPA Entity (Product, Order, User, ...)
├── repository/        ← Spring Data JPA Repositories
├── dto/               ← Data Transfer Objects
├── security/          ← JWT, Spring Security config
├── subsystempaypal/   ← Tích hợp PayPal
└── subsystemvietqr/   ← Tích hợp VietQR
```

---

## Lưu ý

- Hệ thống sử dụng **PostgreSQL cổng 5435** (không phải 5432 mặc định).
- Đảm bảo database `oms` đã được tạo trước khi chạy backend.
- Hibernate tự động cập nhật schema (`ddl-auto: update`) khi khởi động.
