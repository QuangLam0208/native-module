# Quy trình tạo module mới

[← Mục lục tài liệu](README.md)

Tài liệu liệt kê các bước tạo một module mới, từ khung thư mục tới phát hành, và bảng kiểm trước khi push.

## Trong repo `react-native-module`

| Bước | Thao tác |
| :--- | :--- |
| 1 | Tạo thư mục `<tên-module>/` với các file theo [Cấu trúc và khai báo module](02-module-structure.md): `package.json`, `tsconfig.json`, `.gitignore`, `expo-module.config.json`, `react-native.config.js`, `src/index.ts`, `ios/`, `android/` |
| 2 | Chép `tsconfig.json` và `.gitignore` từ một module có sẵn (cả năm module dùng cùng nội dung) |
| 3 | Viết podspec trong `ios/`, đặt `s.name` trùng tên pod mong muốn |
| 4 | Viết class module trong `ios/<Tên>Module.swift`; tên class trùng `apple.modules` |
| 5 | Tách kiểu dữ liệu, giao diện, lớp đọc dữ liệu ra file riêng |
| 6 | Viết phần Android trong `android/` (module Java, `ReactPackage`, `build.gradle`) theo [Tầng native Android](08-android-layer.md), cùng tên module và tên hàm với iOS |
| 7 | Viết `src/index.ts` theo khuôn mẫu tại [Tầng TypeScript](04-typescript-layer.md), tách nền tảng bằng `Platform.OS` |
| 8 | Trong `package.json`: `main` là `build/index.js`, `types` là `build/index.d.ts`, `files` có `build`, `ios`, `android`, `expo-module.config.json`, `react-native.config.js`; thêm script `build`, `prepack` và `devDependencies.typescript` |
| 9 | Biên dịch: `npm install` rồi `npm run build` (sinh `build/`) |
| 10 | Viết `README.md` của module, thêm `CHANGELOG.md` và `RELEASING.md` (chép và đổi tên module từ module có sẵn) |
| 11 | Bổ sung module vào bảng danh sách và mục ví dụ của `README.md` ở gốc repo |

## Trong ứng dụng dùng để thử

| Bước | Thao tác |
| :--- | :--- |
| 1 | Cài tạm bằng `pnpm add "file:<đường-dẫn-tới-repo>/<tên-module>"` (nhớ `npm run build` trước) |
| 2 | Tạo một màn hình gọi từng hàm của module và hiển thị kết quả |
| 3 | iOS: chạy `pod install`, build và kiểm thử trên simulator |
| 4 | Android: xóa `android/build/generated/autolinking`, build và kiểm thử trên thiết bị hoặc emulator |
| 5 | Kiểm thử trên thiết bị thật các phần simulator không hỗ trợ |
| 6 | Push module, rồi chuyển ứng dụng sang cài qua Git |

Cách cài và cập nhật được mô tả tại [Tích hợp vào ứng dụng](05-integration.md). Cách phát hành và đánh số phiên bản nằm tại [Phát hành và quản lý phiên bản](09-releasing.md).

## Bảng kiểm trước khi push

| Hạng mục | Tiêu chí |
| :--- | :--- |
| Cấu trúc | Đủ các file bắt buộc trong [Cấu trúc và khai báo module](02-module-structure.md) |
| Tên | `Name(...)`, `getName()`, tên hàm và tên sự kiện khớp giữa Swift, Java và TypeScript |
| Luồng thực thi | Mọi lời gọi UIKit chạy trên main thread; mọi lời gọi View của Android chạy trên UI thread |
| Tham số | Giá trị không hợp lệ được báo lỗi bằng mã cụ thể |
| Không khả dụng | `isAvailable` là `false` và các hàm có giá trị dự phòng trên nền tảng khác |
| Sự kiện | Listener hủy được; việc theo dõi dừng khi không còn cần; Android có `addListener`, `removeListeners` và gỡ nguồn sự kiện trong `invalidate()` |
| Khác biệt nền tảng | README ghi bảng khác biệt iOS và Android |
| Biên dịch | `npm run build` không lỗi, `build/` khớp `src/` và đã commit |
| Phiên bản | `package.json` và podspec cùng số; có mục trong `CHANGELOG.md` |
| File thừa | Không commit `node_modules`, `package-lock.json` |
| Tài liệu | README ghi rõ phần nào đã kiểm thử và phần nào chưa |
