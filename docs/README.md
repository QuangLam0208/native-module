# Tài liệu phát triển native module iOS & Android

Bộ tài liệu mô tả cách phát triển, phân phối và tích hợp native module cho cả iOS và Android, dựa trên các module trong repo này.

| Hạng mục | Giá trị |
| :--- | :--- |
| Ngôn ngữ | Swift (iOS) · Java (Android) |
| Framework iOS | Expo Modules API (`expo-modules-core`) |
| Framework Android | React Native Bridge (`ReactContextBaseJavaModule`) |
| Mô hình dự án | Expo Prebuild (Continuous Native Generation) |
| Nền tảng tối thiểu | iOS 15.1 · Android SDK 24 |
| Phân phối module | Dependency qua Git, mỗi module một thư mục trong repo |
| Đã kiểm thử với | Expo SDK 57, React Native 0.86 |

## Mục lục

| Tài liệu | Nội dung |
| :--- | :--- |
| [1. Kiến trúc và các kiểu tương tác](01-architecture.md) | Ba tầng của một lời gọi, thread model, bốn kiểu tương tác JavaScript ↔ native |
| [2. Cấu trúc và khai báo module](02-module-structure.md) | Thư mục của một module, `package.json`, `expo-module.config.json`, `react-native.config.js`, podspec, `build.gradle` |
| [3. Tầng native Swift (iOS)](03-swift-layer.md) | API của Expo Modules, Promise, Record, Enumerable, sự kiện, luồng thực thi, báo lỗi |
| [4. Tầng native Java (Android)](04-java-layer.md) | `ReactContextBaseJavaModule`, Constants, Promise, Event Emitter, UI thread |
| [5. Tầng TypeScript](05-typescript-layer.md) | Khuôn mẫu `index.ts`, xử lý đa nền tảng, callback kết hợp Promise, bọc sự kiện |
| [6. Tích hợp vào ứng dụng](06-integration.md) | Cài qua Git, cấu hình Jest, cập nhật module, tương thích iOS 27 |
| [7. Quy trình tạo module mới](07-creating-a-module.md) | Các bước thực hiện và bảng kiểm trước khi push |
| [8. Kiểm thử và xử lý lỗi](08-testing-and-troubleshooting.md) | Lệnh kiểm thử, giới hạn của emulator/simulator, lỗi thường gặp |

## Thứ tự đọc đề xuất

- **Người dùng module trong ứng dụng:** đọc [README của repo](../README.md) và tài liệu 6.
- **Người viết module mới:** đọc lần lượt từ tài liệu 1 đến 7.
- **Người gặp lỗi khi build hoặc chạy:** đọc tài liệu 8.

Cách sử dụng và API của từng module nằm trong README của module đó, liệt kê tại [README của repo](../README.md).
