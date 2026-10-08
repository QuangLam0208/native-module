# native-alert

Native module hiển thị hộp thoại alert trên iOS và Android, dùng chung một API TypeScript.

- **iOS:** viết bằng Swift với Expo Modules API, dùng `UIAlertController`.
- **Android:** viết bằng Java theo kiểu module React Native cổ điển, dùng `AlertDialog` của hệ thống.

Alert có tiêu đề (không bắt buộc), nội dung và hai hoặc ba nút. Mỗi nút có một callback riêng, và hàm trả về Promise cho biết nút nào được chạm.

## Thông tin chung

| Hạng mục | Giá trị |
| :--- | :--- |
| Nền tảng | iOS 15.1 trở lên · Android SDK 24 trở lên |
| Ngôn ngữ | Swift (iOS) · Java (Android) |
| Framework | Expo Modules API (iOS) · React Native Bridge (Android) |
| Đã kiểm thử với | Expo SDK 57, React Native 0.86 |
| Môi trường chạy | Development build hoặc bản release. Expo Go không hỗ trợ. |
| Quyền hệ thống | Không yêu cầu |

Trên nền tảng khác iOS và Android, module không tồn tại: `NativeAlert.isAvailable` là `false`, `NativeAlert.show` không hiển thị gì, trả về `null` và không gọi callback nào.

**Phạm vi đã kiểm thử:**

| Nền tảng | Thiết bị | Đã kiểm thử | Chưa kiểm thử |
| :--- | :--- | :--- | :--- |
| iOS | Simulator iOS 18.6 và 27.0 | Hai nút, không tiêu đề, ba nút, callback của từng nút | Gọi liên tiếp khi alert trước chưa đóng |
| Android | Samsung Galaxy J6+ (SM-J610F), Android 10, bản build `armeabi-v7a` | Alert có tiêu đề, không tiêu đề, ba nút, callback của cả ba nút, chạm ra ngoài, nút Back | Gọi liên tiếp, xoay màn hình khi alert đang mở, Android 11 trở lên, thiết bị 64-bit, emulator |

## Cấu trúc

```text
native-alert/
├── expo-module.config.json       Khai báo module cho Expo Autolinking (iOS)
├── react-native.config.js        Khai báo module cho React Native Autolinking (Android)
├── package.json
├── index.ts                      API TypeScript, chọn module native theo nền tảng
├── ios/
│   ├── NativeAlert.podspec
│   ├── NativeAlertModule.swift   Khai báo module và hàm `show`
│   └── AlertPresenter.swift      Dựng và hiển thị alert
└── android/
    ├── build.gradle              Cấu hình thư viện Android
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/nativealert/
            ├── NativeAlertModule.java    Dựng và hiển thị AlertDialog
            └── NativeAlertPackage.java   Đăng ký module với React Native
```

Hai nền tảng cùng tên module `NativeAlert`, cùng tên hàm `show` và cùng thứ tự tham số. `index.ts` chọn module theo `Platform.OS`.

## Cài đặt

Ứng dụng phải dùng Expo Prebuild. Module được liên kết tự động qua Autolinking, không cần chỉnh sửa `Podfile`, `AppDelegate`, `Info.plist`, `MainApplication` hay `settings.gradle`.

### Cách 1: Cài như một dependency qua Git (khuyến nghị)

Module nằm trong thư mục con `native-alert` của repo `react-native-module`, nên cần chỉ rõ đường dẫn bằng `path:`:

```bash
pnpm add "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#path:/native-alert"
```

Sau đó sinh lại thư mục native và build:

```bash
npx expo prebuild --clean
```

```bash
npx expo run:ios
```

```bash
npx expo run:android
```

Import theo tên package:

```ts
import { NativeAlert } from "native-alert"
```

**Cập nhật lên phiên bản mới:** chạy lại lệnh cài để pnpm lấy commit mới nhất, sau đó chạy `pod install` trong thư mục `ios/` của ứng dụng (hoặc prebuild lại) và build lại native. Nếu Metro đang chạy, khởi động lại Metro sau khi cài.

### Cách 2: Module cục bộ trong ứng dụng

Dùng khi cần chỉnh sửa mã nguồn module ngay trong ứng dụng. Clone repo rồi sao chép thư mục `native-alert` vào `modules/` ở gốc ứng dụng:

```bash
git clone https://git.itzsolution.com/scm/tsa/react-native-module.git
```

```bash
cp -R react-native-module/native-alert <thư-mục-ứng-dụng>/modules/native-alert
```

Sau đó prebuild và build như Cách 1. Import bằng đường dẫn tương đối:

```ts
import { NativeAlert } from "../modules/native-alert"
```

> Cách 2 đã được kiểm thử trên `react-native-base`. Cách 1 đã được kiểm thử với pnpm 11.13.0 khi module còn nằm ở một repo GitHub; lệnh cài từ repo này chưa được kiểm thử. Không dùng đồng thời hai cách trong cùng một ứng dụng.

### Yêu cầu riêng của Android

- **Liên kết khi module nằm trong thư mục `modules/` của ứng dụng:** Expo quét thư mục này cho cả Android. Đã kiểm thử trên `react-native-base`.
- **Nhớ đệm danh sách module Android:** module mới thêm vào `modules/` không được nhận cho tới khi xóa thư mục `android/build/generated/autolinking` (hoặc chạy `npx expo prebuild --clean`) rồi build lại.
- **Khởi động lại Metro** sau khi thêm hoặc đổi file của module: `npx expo start --dev-client --clear`.
- Alert dùng `AlertDialog` gắn vào `Activity` hiện tại, nên chỉ hiển thị được khi ứng dụng đang ở màn hình chính.

## Sử dụng

### Alert hai nút

```tsx
import { NativeAlert } from "native-alert"

NativeAlert.show({
  title: "Xóa mục này?",
  message: "Thao tác này không thể hoàn tác.",
  confirmText: "Xóa",
  cancelText: "Giữ lại",
  onConfirm: () => deleteItem(),
  onCancel: () => console.log("Đã hủy"),
})
```

### Alert không có tiêu đề

Bỏ `title` (hoặc truyền chuỗi rỗng) thì alert chỉ hiển thị nội dung và các nút:

```tsx
NativeAlert.show({
  message: "Thao tác này không thể hoàn tác.",
  confirmText: "Xóa",
  cancelText: "Giữ lại",
  onConfirm: () => deleteItem(),
})
```

### Alert ba nút

Thêm `neutralText` để có nút thứ ba:

```tsx
NativeAlert.show({
  title: "Lưu thay đổi?",
  message: "Bạn có thay đổi chưa lưu.",
  confirmText: "Lưu",
  neutralText: "Không lưu",
  cancelText: "Hủy",
  onConfirm: () => save(),
  onNeutral: () => discard(),
  onCancel: () => {},
})
```

### Dùng Promise thay cho callback

```tsx
const button = await NativeAlert.show({
  title: "Xóa mục này?",
  message: "Thao tác này không thể hoàn tác.",
  confirmText: "Xóa",
  cancelText: "Giữ lại",
})

if (button === NativeAlert.Button.Confirm) {
  deleteItem()
}
```

Callback và Promise dùng đồng thời được. Callback được gọi trước khi Promise hoàn tất.

## API

### `NativeAlert.show(options)`

| Tham số | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `title` | `string` | Không | Tiêu đề. Bỏ qua hoặc chuỗi rỗng thì alert không có tiêu đề |
| `message` | `string` | Có | Nội dung |
| `confirmText` | `string` | Có | Chữ trên nút xác nhận |
| `neutralText` | `string` | Không | Chữ trên nút thứ ba. Bỏ qua hoặc chuỗi rỗng thì alert có hai nút |
| `cancelText` | `string` | Có | Chữ trên nút hủy |
| `onConfirm` | `() => void` | Không | Gọi khi người dùng chạm nút xác nhận |
| `onNeutral` | `() => void` | Không | Gọi khi người dùng chạm nút thứ ba |
| `onCancel` | `() => void` | Không | Gọi khi người dùng chạm nút hủy |

Trả về `Promise<NativeAlertButton | null>`: nút người dùng đã chạm, hoặc `null` khi module không khả dụng.

Promise bị reject khi ứng dụng chưa có màn hình nào để hiển thị alert:

| Nền tảng | Mã lỗi |
| :--- | :--- |
| iOS | `NO_VIEW_CONTROLLER` |
| Android | `NO_ACTIVITY` |

### `NativeAlert.isAvailable`

`boolean`. `true` khi module native đã được liên kết vào bản build đang chạy.

### `NativeAlert.Button`

Hằng số cho giá trị trả về của `show`.

| Hằng số | Giá trị | Nút tương ứng |
| :--- | :--- | :--- |
| `NativeAlert.Button.Confirm` | `"confirm"` | `confirmText` |
| `NativeAlert.Button.Neutral` | `"neutral"` | `neutralText` |
| `NativeAlert.Button.Cancel` | `"cancel"` | `cancelText` |

### Kiểu dữ liệu

```ts
import type { NativeAlertButton, NativeAlertOptions } from "native-alert"
```

## Hành vi

Giao diện do hệ điều hành quyết định, nên khác nhau giữa các nền tảng và giữa các phiên bản.

### Khác biệt giữa iOS và Android

| Hạng mục | iOS | Android |
| :--- | :--- | :--- |
| Hai nút | Nằm ngang, hủy bên trái, xác nhận bên phải | Nằm ngang, hủy bên trái, xác nhận bên phải |
| Ba nút | Xếp dọc: xác nhận, nút thứ ba, hủy | Một hàng: nút thứ ba bên trái, hủy và xác nhận bên phải |
| Chữ trên nút | Giữ nguyên như truyền vào | Hệ thống viết hoa toàn bộ, ví dụ `XÓA` |
| Chạm ra ngoài hộp thoại | Không đóng alert | Đóng alert và tính là `cancel` |
| Nút Back của hệ thống | Không có | Đóng alert và tính là `cancel` |
| Nút xác nhận | In đậm, kích hoạt bằng phím Return | Không in đậm riêng |
| Mã lỗi khi không có màn hình | `NO_VIEW_CONTROLLER` | `NO_ACTIVITY` |
| Hiển thị trên modal đang mở | Có | Phụ thuộc vào `Activity` hiện tại, chưa kiểm thử |

Trên Android, alert đóng bằng chạm ra ngoài hoặc nút Back vẫn gọi `onCancel` và Promise trả về `NativeAlert.Button.Cancel`, nên phía gọi không cần phân biệt hai trường hợp này với việc chạm nút hủy.

## Lỗi thường gặp

| Hiện tượng | Nguyên nhân | Cách xử lý |
| :--- | :--- | :--- |
| `NativeAlert.isAvailable` là `false` trên iOS hoặc Android | Chưa prebuild lại hoặc chưa build native sau khi thêm module | Chạy `npx expo prebuild --clean` rồi build lại |
| Promise reject với `NO_VIEW_CONTROLLER` (iOS) hoặc `NO_ACTIVITY` (Android) | Gọi khi ứng dụng chưa có màn hình hiển thị | Gọi sau khi màn hình đầu tiên đã render |
| Android không nhận module dù đã chép vào `modules/` | Nhớ đệm `android/build/generated/autolinking` còn danh sách cũ | Xóa thư mục đó rồi build lại |
| Màn dùng module báo `TypeError: undefined is not a function` | Metro đang dùng bản `index.ts` cũ | Khởi động lại Metro với `--clear` |
| Alert thứ hai không hiện | Gọi `show` khi alert trước chưa đóng | Chờ Promise của alert trước hoàn tất rồi mới gọi tiếp |
| Jest báo `Cannot use import statement outside a module` tại `index.ts` của module | Jest không biên dịch mã TypeScript nằm trong `node_modules` | Thêm `native-alert` vào danh sách ngoại lệ của `transformIgnorePatterns` trong `jest.config.js` |
| Sửa file Swift hoặc Java nhưng không thấy thay đổi | Reload Metro không áp dụng thay đổi native | Build lại bằng `npx expo run:ios` hoặc `npx expo run:android` |
| `pod install` lỗi `Unicode Normalization not appropriate for ASCII-8BIT` | Shell không dùng mã hóa UTF-8 | Đặt `export LANG=en_US.UTF-8` rồi chạy lại |
