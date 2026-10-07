# native-module

Bộ native module iOS & Android cho ứng dụng React Native.
- **iOS:** Viết bằng Swift với Expo Modules API.
- **Android:** Viết bằng Java với React Native Bridge (`ReactContextBaseJavaModule`).

Mỗi module là một thư mục độc lập trong repo này và được cài riêng. Ứng dụng chỉ cần cài module mình dùng.

## Danh sách module

| Module | Chức năng | Nền tảng hỗ trợ | Tài liệu |
| :--- | :--- | :--- | :--- |
| `native-toast` | Hiển thị toast bốn loại: normal, success, warning, error | iOS · Android | [README](native-toast/README.md) |
| `native-alert` | Hộp thoại alert hai hoặc ba nút, mỗi nút một callback | iOS · Android | [README](native-alert/README.md) |
| `native-date-picker` | Chọn ngày, giờ, ngày giờ, hoặc một khoảng từ – đến | iOS | [README](native-date-picker/README.md) |
| `native-device-helper` | Model máy, phiên bản hệ điều hành, RAM, pin, sự kiện pin | iOS · Android | [README](native-device-helper/README.md) |
| `native-network-status` | Trạng thái kết nối mạng và sự kiện khi mạng thay đổi | iOS · Android | [README](native-network-status/README.md) |

Bảng dưới đây xếp các module theo kiểu tương tác giữa JavaScript và native, để tiện tra cứu khi viết module mới:

| Kiểu tương tác | Module minh họa | Hàm tiêu biểu |
| :--- | :--- | :--- |
| Lấy dữ liệu | `native-device-helper`, `native-network-status` | `getHardwareInfo()`, `getStatus()` |
| Thực thi hành động và nhận kết quả | `native-alert`, `native-date-picker` | `show()`, `showRange()` |
| Nhận sự kiện từ native | `native-device-helper`, `native-network-status` | `addBatteryListener()`, `addListener()` |
| Gọi mà không cần chờ kết quả | `native-toast` | `show()` |

## Tài liệu phát triển

Thư mục [docs](docs/README.md) mô tả cách viết một native module theo cách của repo này: kiến trúc, tầng Swift, tầng Java, tầng TypeScript, cách tích hợp vào ứng dụng, quy trình tạo module mới và xử lý lỗi.

## Yêu cầu

| Hạng mục | Giá trị |
| :--- | :--- |
| Nền tảng | iOS 15.1 trở lên · Android SDK 24 (Android 7.0) trở lên |
| Mô hình dự án | Expo Prebuild |
| Môi trường chạy | Development build hoặc bản release. Expo Go không hỗ trợ. |
| Đã kiểm thử với | Expo SDK 57, React Native 0.86 |

Trên nền tảng chưa được module hỗ trợ, module báo `isAvailable` là `false` và các hàm không làm gì, ứng dụng không phát sinh lỗi.

## Cài đặt

Mỗi module nằm trong một thư mục con của repo, nên lệnh cài cần chỉ rõ đường dẫn bằng `path:`. Thay `<tên-module>` bằng tên trong bảng trên:

```bash
pnpm add "github:loikimtrang/native-module#path:/<tên-module>"
```

Ví dụ:

```bash
pnpm add "github:loikimtrang/native-module#path:/native-toast"
```

Khi phát triển cục bộ:

```bash
pnpm add "file:<đường-dẫn-tới-repo>/<tên-module>"
```

Sau khi cài, sinh lại thư mục native và build:

```bash
npx expo prebuild --clean
```

```bash
# Chạy iOS:
npx expo run:ios

# Chạy Android:
npx expo run:android
```

Module được liên kết tự động qua Autolinking, không cần chỉnh sửa thủ công `Podfile`, `AppDelegate`, `settings.gradle` hay `MainApplication`.

**Dự án dùng Jest** cần thêm tên các module đã cài vào danh sách ngoại lệ của `transformIgnorePatterns` trong `jest.config.js`, vì module phân phối dưới dạng mã TypeScript.

**Cập nhật module:** chạy lại lệnh cài, sau đó prebuild lại hoặc chạy `pod install` (cho iOS) và build lại native. Khởi động lại Metro nếu đang chạy.

## Ví dụ sử dụng

Mỗi module có một ví dụ ngắn dưới đây. Tài liệu đầy đủ nằm trong README của từng module.

### native-toast

Hiển thị một toast báo thành công:

```tsx
import { NativeToast } from "native-toast"

NativeToast.show("Lưu thành công", NativeToast.Type.Success)
```

### native-alert

Hỏi xác nhận trước khi xóa:

```tsx
import { NativeAlert } from "native-alert"

NativeAlert.show({
  title: "Xóa mục này?",
  message: "Thao tác này không thể hoàn tác.",
  confirmText: "Xóa",
  cancelText: "Giữ lại",
  onConfirm: () => deleteItem(),
})
```

### native-date-picker

Chọn một khoảng ngày để lọc dữ liệu (iOS):

```tsx
import { NativeDatePicker } from "native-date-picker"

NativeDatePicker.showRange({
  mode: NativeDatePicker.Mode.Date,
  startLabel: "Từ",
  endLabel: "Đến",
  confirmText: "Xong",
  cancelText: "Hủy",
  onConfirm: ({ start, end }) => setFilter({ from: start, to: end }),
})
```

### native-device-helper

Đọc thông tin thiết bị và phần cứng (iOS & Android):

```tsx
import { DeviceHelper } from "native-device-helper"

console.log(DeviceHelper.model, DeviceHelper.osVersion)

const info = await DeviceHelper.getHardwareInfo()
if (info) {
  console.log(info.totalRamMb, info.batteryLevel, info.isCharging)
}
```

### native-network-status

Hiện thông báo khi mất mạng (iOS & Android):

```tsx
import { NetworkStatus } from "native-network-status"

const unsubscribe = NetworkStatus.addListener((state) => {
  if (!state.isConnected) {
    showOfflineBanner()
  }
})
```

## Quy ước chung của các module

- **Một đối tượng cho mỗi module.** Toàn bộ API nằm trong một đối tượng (`NativeToast`, `NativeAlert`, `NativeDatePicker`, `DeviceHelper`, `NetworkStatus`), nên chỉ cần import một tên.
- **Hằng số thay cho chuỗi.** Các giá trị cố định được cung cấp dưới dạng hằng số, ví dụ `NativeToast.Type.Success`, để được gợi ý khi gõ và tránh sai chính tả.
- **Kiểm tra khả dụng.** Mọi module đều có `isAvailable`, dùng để ẩn hoặc vô hiệu hóa giao diện khi module không có trong bản build.
- **Callback và Promise.** Các hàm chờ người dùng thao tác (`NativeAlert.show`, `NativeDatePicker.show`) vừa nhận callback vừa trả về Promise, dùng cách nào cũng được.

## Cấu trúc một module

```text
<tên-module>/
├── expo-module.config.json   Khai báo module cho Autolinking (iOS + Android)
├── package.json              Tên package, entry point, danh sách file phân phối
├── index.ts                  API TypeScript
├── README.md
├── ios/
│   ├── <TênPod>.podspec      Khai báo pod
│   └── *.swift               Mã native iOS, mỗi file một trách nhiệm
└── android/
    ├── build.gradle          Cấu hình Gradle thư viện Android
    └── src/main/
        ├── AndroidManifest.xml
        └── java/.../
            ├── *Module.java   Mã native Android
            └── *Package.java  ReactPackage cho Autolinking
```

Khi thêm module mới, tạo một thư mục theo cấu trúc trên, rồi bổ sung module vào bảng danh sách và mục ví dụ của tài liệu này.
