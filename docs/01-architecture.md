# Kiến trúc và các kiểu tương tác

[← Mục lục tài liệu](README.md)

Tài liệu mô tả ba tầng của một native module và bốn kiểu tương tác giữa JavaScript và native.

## Ba tầng của một lời gọi

Một lời gọi từ màn hình React Native đi qua ba tầng:

```text
Màn hình của ứng dụng               NativeToast.show("Đã lưu", NativeToast.Type.Success)
        │
        ▼
Tầng TypeScript (index.ts)          Đặt giá trị mặc định, đổi kiểu dữ liệu, gọi callback,
        │                           chọn module iOS hoặc Android
        ▼
Tầng native:
  ├─ iOS (ios/*.swift)              Thực thi bằng API của iOS (Swift / Expo Modules API)
  └─ Android (android/src/.../*.java) Thực thi bằng Android SDK (Java / React Native Bridge)
```

| Tầng | Vị trí | Trách nhiệm |
| :--- | :--- | :--- |
| Màn hình | Ứng dụng | Gọi API của module, không biết gì về native |
| TypeScript | `index.ts` của module | API công khai: kiểu dữ liệu, hằng số, giá trị mặc định, callback, chọn tầng native tương ứng |
| Native | `ios/` và `android/` của module | Làm việc trực tiếp với hệ điều hành (iOS hoặc Android) |

**Các loại thread liên quan:**

| Thread | Vai trò |
| :--- | :--- |
| JS thread | Chạy mã JavaScript của ứng dụng |
| Main / UI thread | Vẽ giao diện native, mọi lời gọi UIKit (iOS) hoặc Android View/Toast/Dialog (Android) |
| Background thread | Công việc không liên quan giao diện (tính toán, đọc dữ liệu, I/O) |

Kết quả của hàm native và dữ liệu của sự kiện luôn được chuyển về JS thread trước khi tới mã JavaScript. JS thread không bị chặn trong lúc chờ.

## Bốn kiểu tương tác giữa JavaScript và native

Mọi hàm của các module đều thuộc một trong bốn kiểu sau. Hai tiêu chí phân biệt là bên nào khởi xướng và JavaScript có cần kết quả hay không.

| Kiểu | Bên khởi xướng | JavaScript nhận lại | Số lần |
| :--- | :--- | :--- | :--- |
| Lấy dữ liệu | JavaScript | Dữ liệu | Một lần cho mỗi lần gọi |
| Thực thi hành động | JavaScript | Kết quả của hành động | Một lần, có thể sau một khoảng thời gian dài |
| Nhận sự kiện | Native | Dữ liệu mới | Nhiều lần, không biết trước thời điểm |
| Gọi không chờ kết quả | JavaScript | Không cần | Không có |

| Kiểu | Module minh họa | Hàm tiêu biểu | Cơ chế Swift (iOS) | Cơ chế Java (Android) |
| :--- | :--- | :--- | :--- | :--- |
| Lấy dữ liệu | `native-device-helper`, `native-network-status` | `getHardwareInfo()`, `getStatus()` | `Constant`, `AsyncFunction` | `getConstants()`, `@ReactMethod` nhận `Promise` |
| Thực thi hành động | `native-alert`, `native-date-picker` | `show()`, `showRange()` | `AsyncFunction` nhận `Promise` | `@ReactMethod` nhận `Promise` |
| Nhận sự kiện | `native-device-helper`, `native-network-status` | `addBatteryListener()`, `addListener()` | `Events`, `sendEvent` | `RCTDeviceEventEmitter.emit` |
| Gọi không chờ kết quả | `native-toast` | `show()` | `AsyncFunction` | `@ReactMethod` void |

**Cách chọn kiểu khi viết module mới:**
- Chỉ đọc một giá trị: lấy dữ liệu.
- Native thực hiện một việc và JavaScript cần biết kết quả: thực thi hành động.
- Giá trị tự thay đổi theo thời gian, không do JavaScript gây ra: nhận sự kiện.
- Native thực hiện một việc, kết quả không quan trọng: gọi không chờ kết quả.

Một module có thể kết hợp nhiều kiểu. `native-network-status` dùng `getStatus()` để đọc trạng thái lúc mở màn hình và `addListener()` để theo dõi các thay đổi sau đó.
