# Tài liệu phát triển native module

Bộ tài liệu mô tả cách phát triển, phân phối và tích hợp native module cho iOS (Swift, Expo Modules API) và Android (Java, React Native module), dựa trên các module trong repo này.

| Hạng mục | Lựa chọn |
| :--- | :--- |
| Ngôn ngữ | Swift (iOS), Java (Android) |
| Framework | Expo Modules API (`expo-modules-core`) cho iOS, `ReactContextBaseJavaModule` cho Android |
| Mô hình dự án | Expo Prebuild (Continuous Native Generation) |
| Nền tảng tối thiểu | iOS 15.1, Android `minSdkVersion` 24 |
| Phân phối module | Dependency qua Git, mỗi module một thư mục trong repo |
| Đã kiểm thử với | Expo SDK 57, React Native 0.86, simulator iOS 18.6 và iOS 27.0, Samsung Galaxy J6+ (Android 10) |

## Mục lục

| Tài liệu | Nội dung |
| :--- | :--- |
| [1. Kiến trúc và các kiểu tương tác](01-architecture.md) | Ba tầng của một lời gọi, ba loại thread, bốn kiểu tương tác giữa JavaScript và native |
| [2. Cấu trúc và khai báo module](02-module-structure.md) | Thư mục của một module, `package.json`, `expo-module.config.json`, podspec |
| [3. Tầng native Swift](03-swift-layer.md) | API của Expo Modules, tên dạng chuỗi, Promise, Record, Enumerable, sự kiện, luồng thực thi, báo lỗi |
| [4. Tầng TypeScript](04-typescript-layer.md) | Khuôn mẫu `index.ts`, quy ước chung, callback kết hợp Promise, bọc sự kiện |
| [5. Tích hợp vào ứng dụng](05-integration.md) | Cài qua Git, cấu hình Jest, cập nhật module, tương thích iOS 27 |
| [6. Quy trình tạo module mới](06-creating-a-module.md) | Các bước thực hiện và bảng kiểm trước khi push |
| [7. Kiểm thử và xử lý lỗi](07-testing-and-troubleshooting.md) | Lệnh kiểm thử, giới hạn của simulator, tình trạng kiểm thử, lỗi thường gặp |
| [8. Tầng native Android](08-android-layer.md) | Module Java, `@ReactMethod`, luồng UI, dialog, sự kiện, thư viện và quyền |

## Thứ tự đọc đề xuất

- **Người dùng module trong ứng dụng:** đọc [README của repo](../README.md) và tài liệu 5.
- **Người viết module mới:** đọc lần lượt từ tài liệu 1 đến 6; viết phần Android thì đọc thêm tài liệu 8.
- **Người gặp lỗi khi build hoặc chạy:** đọc tài liệu 7.

Cách sử dụng và API của từng module nằm trong README của module đó, liệt kê tại [README của repo](../README.md).
