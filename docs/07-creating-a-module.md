# Quy trình tạo module mới

[← Mục lục tài liệu](README.md)

Tài liệu liệt kê các bước tạo một module mới hỗ trợ cả iOS và Android, và bảng kiểm trước khi push.

## Trong repo `native-module`

| Bước | Thao tác |
| :--- | :--- |
| 1 | Tạo thư mục `<tên-module>/` với `package.json`, `expo-module.config.json`, `index.ts`, `ios/`, `android/` |
| 2 | **Cấu hình `package.json`**: `"main": "index.ts"`, `"files": ["index.ts", "ios", "android", "expo-module.config.json"]` |
| 3 | **Cấu hình `expo-module.config.json`**: khai báo cả `"apple"` và `"android"` |
| 4 | **Phần iOS**: Viết podspec trong `ios/<Tên>.podspec` và class Swift trong `ios/<Tên>Module.swift` |
| 5 | **Phần Android**: Viết `android/build.gradle`, `AndroidManifest.xml`, class `<Tên>Module.java` (kế thừa `ReactContextBaseJavaModule`) và `<Tên>Package.java` (triển khai `ReactPackage`) |
| 6 | **Tầng TypeScript**: Viết `index.ts` kết hợp iOS và Android theo khuôn mẫu tại [Tầng TypeScript](05-typescript-layer.md) |
| 7 | Viết `README.md` của module |
| 8 | Bổ sung module vào bảng danh sách và mục ví dụ của `README.md` ở gốc repo |

## Trong ứng dụng dùng để thử (Consumer app)

| Bước | Thao tác |
| :--- | :--- |
| 1 | Cài tạm bằng đường dẫn local: `pnpm add "file:<đường-dẫn-tới-repo>/<tên-module>"` |
| 2 | Thêm tên module vào `transformIgnorePatterns` trong `jest.config.js`, nếu ứng dụng dùng Jest |
| 3 | Tạo màn hình hoặc component gọi từng hàm của module và hiển thị kết quả |
| 4 | Sinh thư mục native: `npx expo prebuild --clean` |
| 5 | Kiểm tra Autolinking: `npx expo-modules-autolinking resolve --platform apple` và `--platform android` |
| 6 | Kiểm thử trên iOS: `npx expo run:ios` |
| 7 | Kiểm thử trên Android: `npx expo run:android` |
| 8 | Commit & Push module lên Git, rồi chuyển app sang cài qua Git: `pnpm add "github:<owner>/<repo>#path:/<tên-module>"` |

Cách cài và cập nhật được mô tả chi tiết tại [Tích hợp vào ứng dụng](06-integration.md).

## Bảng kiểm trước khi push

| Hạng mục | Tiêu chí |
| :--- | :--- |
| **Đồng nhất tên** | Chuỗi `Name(...)` trong Swift, `getName()` trong Java, và tên gọi trong `index.ts` phải khớp 100% |
| **Autolinking config** | `expo-module.config.json` khai báo đúng tên class Swift trong `apple.modules` và fully-qualified package trong `android.modules` |
| **Danh sách file phân phối** | Trường `"files"` trong `package.json` có đủ `"index.ts"`, `"ios"`, `"android"`, `"expo-module.config.json"` |
| **Luồng thực thi** | Mọi lời gọi UI: Swift chạy qua `.runOnQueue(.main)`, Java chạy qua `UiThreadUtil.runOnUiThread(...)` |
| **Tham số & Báo lỗi** | Giá trị không hợp lệ được báo lỗi cụ thể (throw Exception trong Swift, `promise.reject` trong Java) |
| **Không khả dụng** | `isAvailable` là `false` và các hàm có giá trị dự phòng an toàn trên nền tảng chưa hỗ trợ |
| **Sự kiện** | Listener hủy được; Java có hàm `addListener` và `removeListeners`; Swift có `OnStartObserving` / `OnStopObserving` |
| **Tài liệu** | README ghi rõ nền tảng hỗ trợ (iOS/Android) và phần nào đã kiểm thử |
