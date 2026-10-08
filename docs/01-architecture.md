# Kiến trúc và các kiểu tương tác

[← Mục lục tài liệu](README.md)

Tài liệu mô tả ba tầng của một native module và bốn kiểu tương tác giữa JavaScript và native.

## Ba tầng của một lời gọi

Một lời gọi từ màn hình React Native đi qua ba tầng:

```text
Màn hình của ứng dụng               NativeToast.show("Đã lưu", NativeToast.Type.Success)
        │
        ▼
Tầng TypeScript (index.ts)          Đặt giá trị mặc định, đổi kiểu dữ liệu, gọi callback
        │
        ▼
Tầng native                         Thực thi bằng API của hệ điều hành, trả kết quả hoặc phát sự kiện
  ├─ iOS      ios/*.swift           Swift, Expo Modules API
  └─ Android  android/**/*.java     Java, React Native module
```

| Tầng | Vị trí | Trách nhiệm |
| :--- | :--- | :--- |
| Màn hình | Ứng dụng | Gọi API của module, không biết gì về native |
| TypeScript | `index.ts` của module | API công khai: kiểu dữ liệu, hằng số, giá trị mặc định, callback |
| Native | `ios/` và `android/` của module | Làm việc với hệ điều hành; hai nền tảng dùng cùng tên module và tên hàm |

**Ba loại thread liên quan:**

| Thread | Vai trò |
| :--- | :--- |
| JS thread | Chạy mã JavaScript của ứng dụng |
| Main thread (UI thread trên Android) | Vẽ giao diện native, mọi lời gọi UIKit hoặc View của Android |
| Background thread | Công việc không liên quan giao diện |

Kết quả của hàm native và dữ liệu của sự kiện luôn được chuyển về JS thread trước khi tới mã JavaScript. JS thread không bị chặn trong lúc chờ.

## Bốn kiểu tương tác giữa JavaScript và native

Mọi hàm của các module đều thuộc một trong bốn kiểu sau. Hai tiêu chí phân biệt là bên nào khởi xướng và JavaScript có cần kết quả hay không.

| Kiểu | Bên khởi xướng | JavaScript nhận lại | Số lần |
| :--- | :--- | :--- | :--- |
| Lấy dữ liệu | JavaScript | Dữ liệu | Một lần cho mỗi lần gọi |
| Thực thi hành động | JavaScript | Kết quả của hành động | Một lần, có thể sau một khoảng thời gian dài |
| Nhận sự kiện | Native | Dữ liệu mới | Nhiều lần, không biết trước thời điểm |
| Gọi không chờ kết quả | JavaScript | Không cần | Không có |

| Kiểu | Module minh họa | Hàm tiêu biểu | Cơ chế Swift (iOS) / Java (Android) |
| :--- | :--- | :--- | :--- |
| Lấy dữ liệu | `native-device-helper`, `native-network-status` | `getHardwareInfo()`, `getStatus()` | `Constant`, `AsyncFunction` / `getConstants`, `@ReactMethod` có `Promise` |
| Thực thi hành động | `native-alert`, `native-date-picker` | `show()`, `showRange()` | `AsyncFunction` nhận `Promise` / `@ReactMethod` có `Promise` |
| Nhận sự kiện | `native-device-helper`, `native-network-status` | `addBatteryListener()`, `addListener()` | `Events`, `sendEvent` / `RCTDeviceEventEmitter` |
| Gọi không chờ kết quả | `native-toast` | `show()` | `AsyncFunction` / `@ReactMethod` |

**Cách chọn kiểu khi viết module mới:**
- Chỉ đọc một giá trị: lấy dữ liệu.
- Native thực hiện một việc và JavaScript cần biết kết quả: thực thi hành động.
- Giá trị tự thay đổi theo thời gian, không do JavaScript gây ra: nhận sự kiện.
- Native thực hiện một việc, kết quả không quan trọng: gọi không chờ kết quả.

Một module có thể kết hợp nhiều kiểu. `native-network-status` dùng `getStatus()` để đọc trạng thái lúc mở màn hình và `addListener()` để theo dõi các thay đổi sau đó.
