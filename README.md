# react-native-module

Bộ native module cho ứng dụng React Native trên iOS (Swift, Expo Modules API) và Android (Java).

Mỗi module là một thư mục độc lập trong repo này và được cài riêng. Ứng dụng chỉ cần cài module mình dùng.

## Danh sách module

| Module | Chức năng | Tài liệu |
| :--- | :--- | :--- |
| `native-toast` | Hiển thị toast bốn loại: normal, success, warning, error. **Có cả iOS và Android** | [README](native-toast/README.md) |
| `native-alert` | Hộp thoại alert hai hoặc ba nút, mỗi nút một callback. **Có cả iOS và Android** | [README](native-alert/README.md) |
| `native-date-picker` | Chọn ngày, giờ, ngày giờ, hoặc một khoảng từ – đến. **Có cả iOS và Android** | [README](native-date-picker/README.md) |
| `native-device-helper` | Model máy, phiên bản hệ điều hành, RAM, pin, sự kiện pin. **Có cả iOS và Android** | [README](native-device-helper/README.md) |
| `native-network-status` | Trạng thái kết nối mạng và sự kiện khi mạng thay đổi. **Có cả iOS và Android** | [README](native-network-status/README.md) |

Bảng dưới đây xếp các module theo kiểu tương tác giữa JavaScript và native, để tiện tra cứu khi viết module mới:

| Kiểu tương tác | Module minh họa | Hàm tiêu biểu |
| :--- | :--- | :--- |
| Lấy dữ liệu | `native-device-helper`, `native-network-status` | `getHardwareInfo()`, `getStatus()` |
| Thực thi hành động và nhận kết quả | `native-alert`, `native-date-picker` | `show()`, `showRange()` |
| Nhận sự kiện từ native | `native-device-helper`, `native-network-status` | `addBatteryListener()`, `addListener()` |
| Gọi mà không cần chờ kết quả | `native-toast` | `show()` |

## Tài liệu phát triển

Thư mục [docs](docs/README.md) mô tả cách viết một native module theo cách của repo này: kiến trúc, tầng Swift, tầng Android, tầng TypeScript, cách tích hợp vào ứng dụng, quy trình tạo module mới và xử lý lỗi.

## Yêu cầu

| Hạng mục | Giá trị |
| :--- | :--- |
| Nền tảng | iOS 15.1 trở lên, Android (`minSdkVersion` 24) |
| Mô hình dự án | Expo Prebuild |
| Môi trường chạy | Development build hoặc bản release. Expo Go không hỗ trợ. |
| Đã kiểm thử với | Expo SDK 57, React Native 0.86 |

Cả năm module đều có phần Android, viết bằng Java (`native-toast` dùng thư viện Toasty). Trên nền tảng mà module không có, `isAvailable` là `false` và các hàm không làm gì, ứng dụng không phát sinh lỗi.

## Cài đặt

Mỗi module nằm trong một thư mục con của repo, nên lệnh cài cần chỉ rõ đường dẫn bằng `path:`. Thay `<tên-module>` bằng tên trong bảng trên:

```bash
pnpm add "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#path:/<tên-module>"
```

Ví dụ:

```bash
pnpm add "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#path:/native-toast"
```

Lệnh trên cài từ nhánh mặc định của repo là `dev`. Để cài từ một nhánh, tag hoặc commit cụ thể, thêm tên đó vào trước `path:`:

```bash
pnpm add "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#<nhánh-tag-hoặc-commit>&path:/native-toast"
```

Máy cài đặt cần có quyền đọc repo này.

Sau khi cài, sinh lại thư mục native và build:

```bash
npx expo prebuild --clean
```

```bash
npx expo run:ios
```

```bash
npx expo run:android
```

Module được liên kết tự động qua Autolinking, không cần chỉnh sửa `Podfile`, `AppDelegate`, `Info.plist`, `MainApplication` hay `AndroidManifest.xml`.

**Dự án dùng Jest** cần thêm tên các module đã cài vào danh sách ngoại lệ của `transformIgnorePatterns` trong `jest.config.js`, vì module phân phối dưới dạng mã TypeScript.

**Cập nhật module:** chạy lại lệnh cài, sau đó chạy `pod install` trong thư mục `ios/` của ứng dụng (hoặc prebuild lại) và build lại native. Trên Android, xóa `android/build/generated/autolinking` khi thêm module mới. Khởi động lại Metro (`--clear`) nếu đang chạy.

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

Chọn một khoảng ngày để lọc dữ liệu:

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

Đọc thông tin thiết bị và phần cứng:

```tsx
import { DeviceHelper } from "native-device-helper"

console.log(DeviceHelper.model, DeviceHelper.osVersion)

const info = await DeviceHelper.getHardwareInfo()
if (info) {
  console.log(info.totalRamMb, info.batteryLevel, info.isCharging)
}
```

### native-network-status

Hiện thông báo khi mất mạng:

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
- **Cùng API trên hai nền tảng.** Tên module, tên hàm và kiểu dữ liệu giống nhau trên iOS và Android; khác biệt (nếu có) được ghi trong README của từng module.
- **Callback và Promise.** Các hàm chờ người dùng thao tác (`NativeAlert.show`, `NativeDatePicker.show`) vừa nhận callback vừa trả về Promise, dùng cách nào cũng được.

## Cấu trúc một module

```text
<tên-module>/
├── expo-module.config.json   Khai báo module cho Autolinking
├── package.json              Tên package, entry point, danh sách file phân phối
├── index.ts                  API TypeScript
├── react-native.config.js    Cấu hình Autolinking của React Native cho Android
├── README.md
├── ios/
│   ├── <TênPod>.podspec      Khai báo pod
│   └── *.swift               Mã native iOS, mỗi file một trách nhiệm
└── android/
    ├── build.gradle          Thư viện Android
    └── src/main/java/...     Module Java và ReactPackage
```

Khi thêm module mới, tạo một thư mục theo cấu trúc trên, rồi bổ sung module vào bảng danh sách và mục ví dụ của tài liệu này.
