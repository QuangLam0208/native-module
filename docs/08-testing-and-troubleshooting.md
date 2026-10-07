# Kiểm thử và xử lý lỗi

[← Mục lục tài liệu](README.md)

Tài liệu mô tả cách kiểm thử module, giới hạn của emulator / simulator, tình trạng kiểm thử hiện tại và các lỗi thường gặp trên cả iOS và Android.

## Lệnh kiểm thử

```bash
npx tsc --noEmit        # Kiểm tra kiểu TypeScript
npx jest                # Chạy test, nếu ứng dụng dùng Jest
npx expo run:ios        # Build và chạy trên iOS simulator (yêu cầu macOS & Xcode)
npx expo run:android    # Build và chạy trên Android emulator / thiết bị thật
```

Mỗi module cần một màn hình trong ứng dụng để gọi từng hàm và quan sát kết quả. Thay đổi mã TypeScript được Metro nạp lại ngay; thay đổi mã Swift / Java cần build lại native.

## Giới hạn của simulator / emulator

| Giá trị | Trên simulator / emulator | Nguyên nhân |
| :--- | :--- | :--- |
| Mức pin | `-1` hoặc cố định 50%/100% | Máy ảo không có pin vật lý thực tế |
| RAM ứng dụng còn dùng được | `-1` hoặc giá trị ảo | Tiến trình trên máy ảo không bị giới hạn chặt như thiết bị thật |
| Tổng RAM | RAM máy chủ | Máy ảo dùng chung RAM với máy host |
| Sự kiện pin | Không phát tự nhiên | Trạng thái pin không tự thay đổi (có thể giả lập qua adb trên Android) |
| Trạng thái mạng | Mạng máy chủ | Dùng chung card mạng với máy tính |

## Tình trạng kiểm thử

| Module | iOS simulator | Android emulator | Chưa kiểm thử |
| :--- | :--- | :--- | :--- |
| `native-toast` | Bốn loại toast, thời lượng | Toast hiển thị chuẩn UI thread | Lỗi duration âm |
| `native-alert` | Alert hai và ba nút, callbacks | AlertDialog confirm, neutral, cancel | Gọi liên tiếp khi alert trước chưa đóng |
| `native-date-picker` | Sáu trường hợp chọn, theme | Chưa có bản Android | Thiết bị thật Android |
| `native-device-helper` | Model, OS, RAM | Model, OS, RAM, pin, sạc | Sự kiện pin vật lý (cần cắm/rút sạc thật) |
| `native-network-status` | `getStatus` Wi-Fi | `getStatus`, sự kiện mạng | Chuyển đổi mạng 4G/5G thật |

## Lỗi thường gặp

### Cài đặt và liên kết

| Hiện tượng | Nguyên nhân | Cách xử lý |
| :--- | :--- | :--- |
| `Cannot find module '<tên-module>'` khi typecheck hoặc bundle | Chưa cài package | Chạy lệnh `pnpm add` tại [Tích hợp vào ứng dụng](06-integration.md) |
| `isAvailable` là `false` trên iOS hoặc Android | Chưa prebuild lại hoặc chưa build native sau khi cài module | `npx expo prebuild --clean` rồi build lại (`run:ios` hoặc `run:android`) |
| Module không được Autolinking nhận (iOS) | Thiếu hoặc sai `expo-module.config.json`; tên class không khớp mục `apple.modules` | Đối chiếu tên class Swift với file cấu hình |
| Module không được Autolinking nhận (Android) | Thiếu `android.modules` trong `expo-module.config.json` hoặc sai tên package/class `ReactPackage` | Kiểm tra chuỗi fully-qualified trong `android.modules` |
| Bản cài về thiếu thư mục `ios/` hoặc `android/` | Trường `"files"` trong `package.json` của module không liệt kê đủ | Bổ sung `"ios"` và `"android"` vào `"files"` |
| Build lỗi không tìm thấy file native của module sau khi cập nhật | Cấu hình native còn trỏ tới đường dẫn commit cũ | Chạy `npx expo prebuild --clean` rồi build lại |
| Metro báo `spawn ... ENOENT` sau khi cài hoặc cập nhật package | Tiến trình Metro đang chạy còn giữ đường dẫn cũ trong `node_modules` | Khởi động lại Metro |
| Jest báo `Cannot use import statement outside a module` tại `index.ts` của module | Jest không biên dịch mã TypeScript nằm trong `node_modules` | Thêm tên module vào `transformIgnorePatterns` |

### Biên dịch Android (Java / Gradle)

| Hiện tượng | Nguyên nhân | Cách xử lý |
| :--- | :--- | :--- |
| `Duplicate class ... found in modules` | Tồn tại đồng thời cả file Java và Kotlin cùng tên class trong `android/src/main/java/...` | Xóa bỏ file thừa, chỉ giữ một ngôn ngữ cho class đó |
| `package com.xxx does not exist` | Sai `namespace` trong `build.gradle` hoặc sai thư mục package `src/main/java/...` | Đồng nhất namespace và cấu trúc thư mục package |
| `compileSdkVersion` mismatch | App dùng SDK mới hơn thư viện khai báo cố định | Sử dụng hàm helper `getExtOrDefault` trong `build.gradle` |

### Biên dịch iOS (Swift / Xcode)

| Hiện tượng | Nguyên nhân | Cách xử lý |
| :--- | :--- | :--- |
| `Cannot find '...' in scope` | File Swift mới chưa có trong Pods project, hoặc class khai báo `private` | Chạy lại `pod install`; bỏ `private` ở class dùng chung |
| Lỗi biên dịch tại `.runOnQueue` | Gọi `.runOnQueue` trên `Function` (hàm đồng bộ) | Đổi sang `AsyncFunction` |
| Lỗi `swift_version` khi biên dịch | Thiếu `s.swift_version` trong podspec | Thêm `s.swift_version = '5.9'` |

### Khi chạy (Runtime)

| Hiện tượng | Nguyên nhân | Cách xử lý |
| :--- | :--- | :--- |
| Crash Android: `CalledFromWrongThreadException` | Lời gọi UI (Toast, AlertDialog, View) chạy ngoài Main thread | Bọc lời gọi trong `UiThreadUtil.runOnUiThread(...)` |
| Crash iOS: `UIKit must be used from main thread` | Lời gọi UIKit chạy ngoài main thread | Khai báo bằng `AsyncFunction` kèm `.runOnQueue(.main)` |
| Gọi hàm báo không tìm thấy hàm | Tên chuỗi phía Swift/Java khác tên gọi phía TypeScript | Đối chiếu tên hàm và tên module trong `index.ts` |
| Promise không bao giờ hoàn tất | Hàm nhận `promise` nhưng có nhánh không gọi `resolve` hay `reject` | Bảo đảm mọi nhánh `if/else` và `try/catch` đều kết thúc Promise |
| Sửa file Swift / Java nhưng không thấy thay đổi | Reload Metro không áp dụng thay đổi native | Build lại native bằng `npx expo run:ios` hoặc `npx expo run:android` |
