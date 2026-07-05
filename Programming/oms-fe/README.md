# OMS-FE – Online Media Store (Frontend)

## Giới thiệu tổng quan

**OMS-FE** là giao diện người dùng của hệ thống **OMS (Online Media Store)** – một nền tảng thương mại điện tử chuyên kinh doanh các sản phẩm truyền thông vật lý gồm Sách, Báo, CD và DVD.

Hệ thống hỗ trợ ba nhóm người dùng:
- **Khách hàng**: Tìm kiếm, xem sản phẩm, quản lý giỏ hàng, đặt hàng và thanh toán (VietQR / PayPal).
- **Quản lý sản phẩm**: Quản lý danh mục sản phẩm và xử lý đơn hàng.
- **Quản trị viên**: Quản lý tài khoản nhân viên và phân quyền hệ thống.

### Công nghệ sử dụng

| Thành phần | Công nghệ |
|-----------|----------|
| Framework | Angular 21 (Standalone Components) |
| Ngôn ngữ | TypeScript |
| UI | CSS thuần + ng-zorro-antd |
| Giao tiếp API | Angular HttpClient |
| Backend | Spring Boot (port `8080`, prefix `/oms/api`) |

---

## Hướng dẫn chạy dự án

### Yêu cầu cần có

- **Node.js** ≥ 18.x và **npm** ≥ 9.x
- **Angular CLI**: `npm install -g @angular/cli`
- **Backend OMS** đang chạy tại `http://localhost:8080`

### Các bước thực hiện

#### 1. Cài đặt dependencies

```bash
npm install
```

#### 2. Khởi động server phát triển

```bash
npm start
```

hoặc:

```bash
ng serve
```

Ứng dụng sẽ chạy tại: **http://localhost:4200**

Ứng dụng tự động tải lại khi bạn chỉnh sửa mã nguồn.

#### 3. Cấu hình URL backend

URL backend được khai báo trực tiếp trong các service (ví dụ `src/services/product.service.ts`):

```
http://localhost:8080/oms/api/...
```

Nếu backend chạy trên cổng khác, hãy cập nhật `baseUrl` trong các file service tương ứng.

---

## Các lệnh hữu ích

| Lệnh | Mô tả |
|------|-------|
| `npm start` | Khởi động server phát triển (port 4200) |
| `ng build` | Biên dịch production vào thư mục `dist/` |
| `ng test` | Chạy kiểm thử đơn vị (Vitest) |
| `ng generate component <name>` | Tạo component mới |

---

## Tài nguyên tham khảo

- [Angular CLI Documentation](https://angular.dev/tools/cli)
- [Angular Standalone Components](https://angular.dev/guide/components)
