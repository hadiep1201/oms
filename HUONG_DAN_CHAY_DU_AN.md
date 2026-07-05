# HƯỚNG DẪN CHẠY DỰ ÁN AIMS (Nhóm 6 - ITSS Software Development)

Tài liệu này hướng dẫn bạn từng bước cách cài đặt môi trường, cấu hình cơ sở dữ liệu, import dữ liệu mẫu, và khởi chạy toàn bộ dự án AIMS (gồm cả Backend và Frontend) trên máy tính của bạn.

---

## 📌 Tổng Quan Dự Án
Dự án bao gồm 2 phần chính nằm trong thư mục [Programming](file:///d:/ISD.20252-06/Programming):
1. **Backend ([aims](file:///d:/ISD.20252-06/Programming/aims))**: Phát triển bằng **Spring Boot (Java)**, kết nối tới cơ sở dữ liệu **PostgreSQL**.
2. **Frontend ([aims-fe](file:///d:/ISD.20252-06/Programming/aims-fe))**: Phát triển bằng **Angular 21** và **Ng-Zorro-Antd**.

---

## 🛠️ Kiểm Tra & Chuẩn Bị Môi Trường
Hiện tại trên máy tính của bạn đã cài đặt sẵn:
- **Java**: Phiên bản `23.0.2` (Đã có sẵn, đạt yêu cầu).
- **Node.js**: Phiên bản `24.11.1` và **NPM** `11.6.2` (Đã có sẵn, đạt yêu cầu).

Bạn cần chuẩn bị thêm **PostgreSQL Database Server** theo một trong hai cách dưới đây.

---

## 🗄️ Bước 1: Cài đặt và Cấu hình PostgreSQL

Chọn **một trong hai cách** sau để chuẩn bị PostgreSQL:

### Cách 1: Sử dụng Docker (Khuyên dùng nếu máy đã có Docker)
Nếu máy tính của bạn đã cài đặt **Docker Desktop**:
1. Mở terminal tại thư mục [Programming/aims](file:///d:/ISD.20252-06/Programming/aims).
2. Chạy lệnh sau để khởi động container PostgreSQL chạy ngầm:
   ```bash
   docker compose up -d
   ```
   *Container này sẽ tự động tạo cơ sở dữ liệu `aims_db` chạy trên cổng `5435` theo cấu hình trong file [docker-compose.yml](file:///d:/ISD.20252-06/Programming/aims/docker-compose.yml).*

---

### Cách 2: Cài đặt trực tiếp trên Windows (Nếu không dùng Docker)
Nếu bạn chưa có Docker, hãy cài đặt PostgreSQL trực tiếp trên Windows:
1. **Tải bộ cài**: Truy cập [trang chủ EDB PostgreSQL](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads) và tải về phiên bản PostgreSQL (khuyên dùng bản **16** hoặc **17** cho Windows x86-64).
2. **Cài đặt**: 
   - Chạy file cài đặt vừa tải về.
   - Nhập mật khẩu cho tài khoản admin mặc định (`postgres`). **Hãy ghi nhớ mật khẩu này**.
   - Cổng mặc định của Postgres là `5432`. Bạn có thể giữ nguyên `5432` hoặc đổi sang `5435` cho trùng cấu hình dự án.
3. **Cấu hình File `.env`**:
   - Mở file [.env](file:///d:/ISD.20252-06/Programming/aims/.env) tại thư mục backend.
   - Nếu bạn cài PostgreSQL chạy ở cổng `5432` (mặc định), hãy mở file [application.yaml](file:///d:/ISD.20252-06/Programming/aims/src/main/resources/application.yaml) và sửa dòng `url: jdbc:postgresql://localhost:5435/${POSTGRES_DB}` thành `localhost:5432`.
   - Sửa thông tin tài khoản đăng nhập DB trong file [.env](file:///d:/ISD.20252-06/Programming/aims/.env) cho đúng với tài khoản Postgres trên máy của bạn:
     - `POSTGRES_USER=postgres` (hoặc user bạn tự tạo).
     - `POSTGRES_PASSWORD=mật_khẩu_của_bạn`.
4. **Tạo Cơ Sở Dữ Liệu**:
   - Sử dụng **pgAdmin 4** (được cài đi kèm PostgreSQL) hoặc **DBeaver**, **Navicat** để kết nối tới PostgreSQL Server.
   - Tạo mới một database tên là `aims_db`.

---

## ☕ Bước 2: Chạy Backend (aims)

1. Mở một terminal mới (PowerShell) và di chuyển vào thư mục backend:
   ```powershell
   cd d:\ISD.20252-06\Programming\aims
   ```
2. Khởi chạy ứng dụng Spring Boot bằng Maven Wrapper:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   *Ở lần chạy đầu tiên này, Spring Boot sử dụng cơ chế `hibernate.ddl-auto: update` để **tự động sinh ra cấu trúc các bảng** trong database `aims_db` của bạn. Hãy đợi cho đến khi terminal báo ứng dụng đã khởi động thành công trên cổng `8080` (Context path: `/aims`).*
3. **Tắt backend** bằng cách nhấn `Ctrl + C` trong terminal để chúng ta import dữ liệu mẫu ở bước tiếp theo.

---

## 📊 Bước 3: Import Dữ Liệu Mẫu (sample_data.sql)

Khi database đã có cấu trúc các bảng do Spring Boot tạo ra ở Bước 2, bạn cần nạp dữ liệu mẫu (sách, đĩa CD/DVD, tài khoản người dùng, v.v.):

1. Mở công cụ quản lý database (pgAdmin 4, DBeaver, v.v.) và kết nối vào cơ sở dữ liệu `aims_db`.
2. Mở file [sample_data.sql](file:///d:/ISD.20252-06/Programming/aims/sample_data.sql) bằng công cụ SQL Editor của phần mềm quản lý database đó.
3. Thực thi toàn bộ file script SQL này để nạp dữ liệu mẫu vào các bảng.
4. **Khởi động lại Backend**: Quay lại terminal của backend và chạy lại lệnh khởi động:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   *Bây giờ ứng dụng backend đã có đầy đủ dữ liệu mẫu để sẵn sàng phục vụ frontend.*

---

## 💻 Bước 4: Chạy Frontend (aims-fe)

1. Mở một cửa sổ terminal mới độc lập (PowerShell) và đi tới thư mục frontend:
   ```powershell
   cd d:\ISD.20252-06\Programming\aims-fe
   ```
2. Thực hiện cài đặt lại cấu trúc script chạy cho Windows (do dự án được đóng gói từ macOS nên cần tái sinh command):
   ```powershell
   npm install --legacy-peer-deps
   ```
3. Khởi chạy server Angular phát triển:
   ```powershell
   npm start
   ```
   *Terminal sẽ biên dịch dự án và khởi chạy Local Development Server.*
4. Mở trình duyệt web của bạn và truy cập vào địa chỉ:
   ```
   http://localhost:4200/
   ```

---

## 🔑 Tài Khoản Đăng Nhập Mẫu

Dữ liệu mẫu trong file [sample_data.sql](file:///d:/ISD.20252-06/Programming/aims/sample_data.sql) cung cấp sẵn một số tài khoản có phân quyền khác nhau để bạn test dự án:

| Vai trò | Email | Mật khẩu | Chức năng thử nghiệm |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | `admin@aims.com` | `password123` | Quản lý người dùng, phân quyền hệ thống |
| **Quản lý sản phẩm (Product Manager)** | `manager@aims.com` | `password123` | Quản lý kho hàng, cập nhật danh sách đĩa CD, DVD, sách |
| **Khách hàng thường (User)** | `testuser1@aims.com` | `password123` | Xem sản phẩm, thêm vào giỏ hàng, đặt hàng |

---

## ⚠️ Khắc Phục Một Số Lỗi Thường Gặp (Troubleshooting)

1. **Lỗi DB Connection (Không kết nối được cơ sở dữ liệu)**:
   - Hãy chắc chắn PostgreSQL Server đang chạy và cổng cấu hình trong file [application.yaml](file:///d:/ISD.20252-06/Programming/aims/src/main/resources/application.yaml) (`5435` hoặc `5432`) trùng khớp với cổng Postgres đang mở trên máy bạn.
   - Kiểm tra xem mật khẩu và tên đăng nhập trong file [.env](file:///d:/ISD.20252-06/Programming/aims/.env) đã trùng khớp với cấu hình tài khoản cục bộ của bạn chưa.
2. **Lỗi `ng` không nhận diện được lệnh**:
   - Nếu bạn gặp lỗi `'ng' is not recognized as an internal or external command`, hãy chắc chắn bạn đã chạy lệnh `npm install --legacy-peer-deps` để NPM sinh ra các file thực thi `.cmd` và `.ps1` cho Windows trong thư mục `node_modules\.bin`.
   - Luôn sử dụng lệnh `npm start` thay vì chạy trực tiếp `ng serve`.

Chúc bạn chạy và thử nghiệm dự án thành công!
