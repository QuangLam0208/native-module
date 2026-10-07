# native-toast

Native module hiển thị toast trên iOS & Android:
- **iOS:** Viết bằng Swift với Expo Modules API (custom animated toast view).
- **Android:** Viết bằng Java với React Native Bridge (`Toast.makeText` chạy trên UI thread).

Toast là một thông báo ngắn, không chặn thao tác của người dùng và tự ẩn sau một khoảng thời gian ngắn.

## Thông tin chung

| Hạng mục | Giá trị |
| :--- | :--- |
| Nền tảng | iOS 15.1 trở lên · Android SDK 24 trở lên |
| Ngôn ngữ | Swift (iOS) · Java (Android) |
| Framework | Expo Modules API (iOS) · React Native Bridge (Android) |
| Đã kiểm thử với | Expo SDK 57, React Native 0.86 |
| Môi trường chạy | Development build hoặc bản release. Expo Go không hỗ trợ. |

## Cấu trúc

```text
native-toast/
├── expo-module.config.json       Khai báo module cho Expo Autolinking (iOS)
├── react-native.config.js        Khai báo module cho React Native CLI Autolinking (Android)
├── package.json
├── index.ts                      API TypeScript
├── ios/
│   ├── NativeToast.podspec
│   ├── NativeToastModule.swift   Khai báo module và hàm `show`
│   ├── ToastType.swift           Các loại toast và màu nền
│   └── ToastView.swift           Giao diện và hiệu ứng của toast
└── android/
    ├── build.gradle              Cấu hình thư viện Android
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/nativetoast/
            ├── NativeToastModule.java    Hiển thị Android Toast
            └── NativeToastPackage.java   ReactPackage cho Autolinking
```

## Cài đặt

### Cài qua Git

```bash
pnpm add "github:loikimtrang/native-module#path:/native-toast"
```

### Cài trong lúc phát triển (Local Dev)

```bash
pnpm add "file:<đường-dẫn-tới-repo>/native-toast"
```

Sau đó sinh lại thư mục native và build:

```bash
npx expo prebuild --clean

# Chạy iOS:
npx expo run:ios

# Chạy Android:
npx expo run:android
```

## Ví dụ sử dụng

```tsx
import { NativeToast } from "native-toast"

// Hiển thị toast thông thường:
NativeToast.show("Đã lưu dữ liệu")

// Hiển thị toast thành công với thời lượng dài:
NativeToast.show("Cập nhật thành công!", NativeToast.Type.Success, NativeToast.Duration.Long)

// Hiển thị toast lỗi:
NativeToast.show("Lỗi kết nối máy chủ", NativeToast.Type.Error)
```

## Bảng API

| Thuộc tính / Hàm | Kiểu | Mô tả |
| :--- | :--- | :--- |
| `NativeToast.isAvailable` | `boolean` | `true` khi native module có trong bản build |
| `NativeToast.Type` | `object` | `Normal`, `Success`, `Warning`, `Error` |
| `NativeToast.Duration` | `object` | `Short` (2200ms), `Long` (3500ms) |
| `NativeToast.show(message, type, durationMs)` | `Promise<void>` | Hiển thị toast trên màn hình |
