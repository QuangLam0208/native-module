# native-alert

Native module hiển thị hộp thoại alert trên iOS & Android:
- **iOS:** Viết bằng Swift với Expo Modules API (`UIAlertController`).
- **Android:** Viết bằng Java với React Native Bridge (`AlertDialog.Builder`).

Alert có tiêu đề (không bắt buộc), nội dung và hai hoặc ba nút. Mỗi nút có một callback riêng, và hàm trả về Promise cho biết nút nào được chạm.

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
native-alert/
├── expo-module.config.json       Khai báo module cho Autolinking (iOS + Android)
├── package.json
├── index.ts                      API TypeScript
├── ios/
│   ├── NativeAlert.podspec
│   ├── NativeAlertModule.swift   Khai báo module và hàm `show`
│   └── AlertPresenter.swift      Dựng và hiển thị alert trên iOS
└── android/
    ├── build.gradle              Cấu hình thư viện Android
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/nativealert/
            ├── NativeAlertModule.java    Dựng và hiển thị AlertDialog Android
            └── NativeAlertPackage.java   ReactPackage cho Autolinking
```

## Cài đặt

### Cài qua Git

```bash
pnpm add "github:loikimtrang/native-module#path:/native-alert"
```

### Cài trong lúc phát triển (Local Dev)

```bash
pnpm add "file:<đường-dẫn-tới-repo>/native-alert"
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
import { NativeAlert } from "native-alert"

// Alert hai nút xác nhận xóa:
NativeAlert.show({
  title: "Xóa mục này?",
  message: "Thao tác này không thể hoàn tác.",
  confirmText: "Xóa",
  cancelText: "Hủy",
  onConfirm: () => handleDelete(),
})

// Alert ba nút:
NativeAlert.show({
  title: "Lưu thay đổi?",
  message: "Bạn có muốn lưu các thay đổi trước khi thoát?",
  confirmText: "Lưu",
  neutralText: "Không lưu",
  cancelText: "Hủy",
  onConfirm: () => handleSave(),
  onNeutral: () => handleDiscard(),
})
```

## Bảng API

| Thuộc tính / Hàm | Kiểu | Mô tả |
| :--- | :--- | :--- |
| `NativeAlert.isAvailable` | `boolean` | `true` khi native module có trong bản build |
| `NativeAlert.Button` | `object` | `Confirm`, `Neutral`, `Cancel` |
| `NativeAlert.show(options)` | `Promise<NativeAlertButton \| null>` | Hiển thị alert, gọi callback tương ứng và trả về nút đã chọn |
