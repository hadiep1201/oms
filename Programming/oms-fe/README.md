# OMS-FE

Dự án này được tạo bằng [Angular CLI](https://github.com/angular/angular-cli) phiên bản 21.2.10.

## Máy chủ phát triển (Development server)

Để khởi động máy chủ phát triển cục bộ, hãy chạy:

```bash
ng serve
```

Sau khi máy chủ khởi chạy, hãy mở trình duyệt và truy cập `http://localhost:4200/`. Ứng dụng sẽ tự động tải lại mỗi khi bạn thay đổi các tệp nguồn.

## Tạo mã nguồn mẫu (Code scaffolding)

Angular CLI cung cấp các công cụ tạo mã nguồn mạnh mẽ. Để tạo một component mới, hãy chạy:

```bash
ng generate component component-name
```

Để xem danh sách đầy đủ các sơ đồ có sẵn (chẳng hạn như `components`, `directives`, hoặc `pipes`), hãy chạy:

```bash
ng generate --help
```

## Biên dịch (Building)

Để biên dịch dự án, hãy chạy:

```bash
ng build
```

Lệnh này sẽ biên dịch dự án và lưu trữ các sản phẩm biên dịch trong thư mục `dist/`. Theo mặc định, bản biên dịch production sẽ tối ưu hóa ứng dụng để đạt hiệu năng và tốc độ tải tốt nhất.

## Chạy kiểm thử đơn vị (Running unit tests)

Để thực hiện kiểm thử đơn vị với trình chạy thử [Vitest](https://vitest.dev/), hãy sử dụng lệnh sau:

```bash
ng test
```

## Chạy kiểm thử đầu cuối (Running end-to-end tests)

Để chạy kiểm thử đầu cuối (e2e), hãy chạy:

```bash
ng e2e
```

Angular CLI không đi kèm sẵn khung kiểm thử đầu cuối mặc định. Bạn có thể chọn khung kiểm thử phù hợp nhất với nhu cầu của mình.

## Tài nguyên bổ sung (Additional Resources)

Để biết thêm thông tin về cách sử dụng Angular CLI, bao gồm các tài liệu tham khảo chi tiết về lệnh, hãy truy cập trang [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli).
