# Quy trình tạo module mới

[← Mục lục tài liệu](README.md)

Tài liệu liệt kê các bước tạo một module mới và bảng kiểm trước khi push.

## Trong repo `react-native-module`

| Bước | Thao tác |
| :--- | :--- |
| 1 | Tạo thư mục `<tên-module>/` với `package.json`, `expo-module.config.json`, `react-native.config.js`, `index.ts`, `ios/`, `android/` |
| 2 | Viết podspec, đặt `s.name` trùng tên pod mong muốn |
| 3 | Viết class module trong `ios/<Tên>Module.swift`; tên class trùng `apple.modules` |
| 4 | Tách kiểu dữ liệu, giao diện, lớp đọc dữ liệu ra file riêng |
| 4a | Viết phần Android trong `android/` (module Java, `ReactPackage`, `build.gradle`) theo [Tầng native Android](08-android-layer.md), cùng tên module và tên hàm với iOS |
| 5 | Viết `index.ts` theo khuôn mẫu tại [Tầng TypeScript](04-typescript-layer.md), tách nền tảng bằng `Platform.OS` |
| 6 | Viết `README.md` của module |
| 7 | Bổ sung module vào bảng danh sách và mục ví dụ của `README.md` ở gốc repo |

## Trong ứng dụng dùng để thử

| Bước | Thao tác |
| :--- | :--- |
| 1 | Cài tạm bằng `pnpm add "file:<đường-dẫn-tới-repo>/<tên-module>"` |
| 2 | Thêm tên module vào `transformIgnorePatterns` trong `jest.config.js`, nếu ứng dụng dùng Jest |
| 3 | Tạo một màn hình gọi từng hàm của module và hiển thị kết quả |
| 4 | Chạy `pod install`, build và kiểm thử trên simulator iOS |
| 4a | Xóa `android/build/generated/autolinking`, build và kiểm thử trên thiết bị hoặc emulator Android |
| 5 | Kiểm thử trên thiết bị thật các phần simulator không hỗ trợ |
| 6 | Push module, rồi chuyển ứng dụng sang cài qua Git |

Cách cài và cập nhật được mô tả tại [Tích hợp vào ứng dụng](05-integration.md).

## Bảng kiểm trước khi push

| Hạng mục | Tiêu chí |
| :--- | :--- |
| Tên | `Name(...)`, `getName()`, tên hàm và tên sự kiện khớp giữa Swift, Java và TypeScript |
| Luồng thực thi | Mọi lời gọi UIKit chạy trên main thread; mọi lời gọi View của Android chạy trên UI thread |
| Tham số | Giá trị không hợp lệ được báo lỗi bằng mã cụ thể |
| Không khả dụng | `isAvailable` là `false` và các hàm có giá trị dự phòng trên nền tảng khác |
| Sự kiện | Listener hủy được; việc theo dõi dừng khi không còn cần; Android có `addListener`, `removeListeners` và gỡ nguồn sự kiện trong `invalidate()` |
| Khác biệt nền tảng | README ghi bảng khác biệt iOS và Android |
| Tài liệu | README ghi rõ phần nào đã kiểm thử và phần nào chưa |
