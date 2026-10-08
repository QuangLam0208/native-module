# native-device-helper

Native module đọc thông tin thiết bị và phần cứng trên iOS và Android, dùng chung một API TypeScript.

- **iOS:** viết bằng Swift với Expo Modules API.
- **Android:** viết bằng Java theo kiểu module React Native cổ điển.

Module cung cấp model máy, phiên bản hệ điều hành, dung lượng RAM, trạng thái pin, và phát sự kiện khi mức pin hoặc trạng thái sạc thay đổi.

## Thông tin chung

| Hạng mục | Giá trị |
| :--- | :--- |
| Nền tảng | iOS 15.1 trở lên · Android SDK 24 trở lên |
| Ngôn ngữ | Swift (iOS) · Java (Android) |
| Framework | Expo Modules API (iOS) · React Native Bridge (Android) |
| Đã kiểm thử với | Expo SDK 57, React Native 0.86 |
| Môi trường chạy | Development build hoặc bản release. Expo Go không hỗ trợ. |
| Quyền hệ thống | Không yêu cầu trên cả hai nền tảng |

Trên nền tảng khác iOS và Android, module không tồn tại: `DeviceHelper.isAvailable` là `false`, các giá trị trả về `null` và ứng dụng không phát sinh lỗi.

**Phạm vi đã kiểm thử:**

| Nền tảng | Thiết bị | Đã kiểm thử | Chưa kiểm thử |
| :--- | :--- | :--- | :--- |
| iOS | Simulator iOS 18.6 và 27.0 | Model, phiên bản hệ điều hành, tổng RAM | Mức pin, RAM khả dụng, sự kiện pin (simulator không có), thiết bị thật |
| Android | Samsung Galaxy J6+ (SM-J610F), Android 10, bản build `armeabi-v7a` | Model, phiên bản hệ điều hành, tổng RAM và RAM trống, mức pin và trạng thái sạc khớp với `adb shell dumpsys battery`, sự kiện khi mức pin đổi và khi trạng thái sạc đổi | Hủy listener rồi kiểm tra receiver đã dừng, nhiều listener cùng lúc, Android 11 trở lên, thiết bị 64-bit, emulator |

## Cấu trúc

```text
native-device-helper/
├── expo-module.config.json      Khai báo module cho Expo Autolinking (iOS)
├── react-native.config.js       Khai báo module cho React Native Autolinking (Android)
├── package.json
├── index.ts                     API TypeScript, chọn module native theo nền tảng
├── ios/
│   ├── DeviceHelper.podspec
│   ├── DeviceHelperModule.swift   Khai báo module, hằng số, hàm và sự kiện
│   ├── DeviceInfo.swift           Model và phiên bản hệ điều hành
│   └── HardwareReader.swift       RAM và pin
└── android/
    ├── build.gradle               Cấu hình thư viện Android
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/nativedevicehelper/
            ├── DeviceHelperModule.java    Hằng số, hàm đọc phần cứng, sự kiện pin
            └── DeviceHelperPackage.java   Đăng ký module với React Native
```

Hai nền tảng dùng hai cơ chế khác nhau nhưng cùng tên module `DeviceHelper`, cùng tên hàm, cùng tên sự kiện `onBatteryChange`. `index.ts` chọn module theo `Platform.OS`: iOS gọi `requireOptionalNativeModule("DeviceHelper")`, Android gọi `NativeModules.DeviceHelper`.

## Cài đặt

Ứng dụng phải dùng Expo Prebuild. Module được liên kết tự động qua Autolinking, không cần chỉnh sửa `Podfile`, `AppDelegate`, `Info.plist`, `MainApplication` hay `settings.gradle`.

### Cách 1: Cài như một dependency qua Git (khuyến nghị)

Module nằm trong thư mục con `native-device-helper` của repo `react-native-module`, nên cần chỉ rõ đường dẫn bằng `path:`:

```bash
pnpm add "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#path:/native-device-helper"
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
import { DeviceHelper } from "native-device-helper"
```

**Cập nhật lên phiên bản mới:** chạy lại lệnh cài để pnpm lấy commit mới nhất, sau đó chạy `pod install` trong thư mục `ios/` của ứng dụng (hoặc prebuild lại) và build lại native. Nếu Metro đang chạy, khởi động lại Metro sau khi cài.

### Cách 2: Module cục bộ trong ứng dụng

Dùng khi cần chỉnh sửa mã nguồn module ngay trong ứng dụng. Clone repo rồi sao chép thư mục `native-device-helper` vào `modules/` ở gốc ứng dụng:

```bash
git clone https://git.itzsolution.com/scm/tsa/react-native-module.git
```

```bash
cp -R react-native-module/native-device-helper <thư-mục-ứng-dụng>/modules/native-device-helper
```

Sau đó prebuild và build như Cách 1. Import bằng đường dẫn tương đối:

```ts
import { DeviceHelper } from "../modules/native-device-helper"
```

> Cách 2 đã được kiểm thử trên `react-native-base`. Cách 1 đã được kiểm thử với pnpm 11.13.0 khi module còn nằm ở một repo GitHub; lệnh cài từ repo này chưa được kiểm thử. Không dùng đồng thời hai cách trong cùng một ứng dụng.

### Yêu cầu riêng của Android

- **Liên kết khi module nằm trong thư mục `modules/` của ứng dụng:** Expo quét thư mục này cho cả Android, không cần khai báo trong `package.json`. Đã kiểm thử trên `react-native-base`.
- **Nhớ đệm danh sách module Android.** Expo lưu danh sách module vào `android/build/generated/autolinking/autolinking.json`, khóa theo `package.json`. Module mới thêm vào `modules/` sẽ không được nhận cho tới khi xóa thư mục `android/build/generated/autolinking` (hoặc chạy `npx expo prebuild --clean`) rồi build lại.
- **Khởi động lại Metro** sau khi thêm hoặc đổi file của module. Metro đang chạy có thể vẫn dùng bản `index.ts` cũ; dấu hiệu là `TypeError: undefined is not a function` khi mở màn dùng module. Chạy `npx expo start --dev-client --clear`.
- **Kiến trúc CPU:** để build nhẹ hơn, chỉ build kiến trúc của thiết bị đang dùng, ví dụ `ORG_GRADLE_PROJECT_reactNativeArchitectures=armeabi-v7a npx expo run:android`.

## Sử dụng

Toàn bộ API nằm trong một đối tượng `DeviceHelper`:

```tsx
import { DeviceHelper } from "native-device-helper"

// Thông tin tĩnh, đọc trực tiếp
DeviceHelper.model      // "iPhone17,1"
DeviceHelper.osVersion  // "18.6.0"

// Thông tin phần cứng tại thời điểm gọi
const info = await DeviceHelper.getHardwareInfo()
if (info) {
  console.log(info.totalRamMb, info.availableRamMb, info.batteryLevel, info.isCharging)
}

// Lắng nghe thay đổi pin
const unsubscribe = DeviceHelper.addBatteryListener((battery) => {
  console.log(battery.batteryLevel, battery.isCharging)
})

// Hủy lắng nghe khi không còn dùng
unsubscribe()
```

Trong component React, đăng ký và hủy trong `useEffect`:

```tsx
useEffect(() => DeviceHelper.addBatteryListener(setBattery), [])
```

## API

### `DeviceHelper.isAvailable`

`boolean`. `true` khi module native đã được liên kết vào bản build đang chạy.

### `DeviceHelper.model`

`string | null`. Mã định danh phần cứng: ví dụ `"iPhone17,1"` trên iOS (trên simulator là model đang được giả lập), `"SM-J610F"` trên Android (`Build.MODEL`). `null` khi module không khả dụng.

### `DeviceHelper.osVersion`

`string | null`. Phiên bản hệ điều hành: ví dụ `"18.6.0"` trên iOS, `"10"` trên Android (`Build.VERSION.RELEASE`). Chuỗi không kèm tên hệ điều hành, ứng dụng tự ghép. `null` khi module không khả dụng.

### `DeviceHelper.getHardwareInfo()`

Trả về `Promise<HardwareInfo | null>`. Kết quả là `null` khi module không khả dụng.

| Trường | Kiểu | Mô tả |
| :--- | :--- | :--- |
| `totalRamMb` | `number` | Tổng RAM vật lý, tính bằng MB |
| `availableRamMb` | `number` | RAM còn trống, tính bằng MB. **Nghĩa khác nhau theo nền tảng**, xem bên dưới. `-1` khi không xác định |
| `batteryLevel` | `number` | Phần trăm pin từ 0 đến 100. `-1` khi không xác định |
| `isCharging` | `boolean` | `true` khi đang sạc hoặc đã sạc đầy |

`availableRamMb` khác nghĩa giữa hai nền tảng:

| Nền tảng | Ý nghĩa |
| :--- | :--- |
| iOS | Phần bộ nhớ **riêng của ứng dụng** còn được phép dùng trước khi bị hệ thống dừng. iOS không cung cấp RAM trống của toàn hệ thống |
| Android | RAM trống của **toàn hệ thống** (`ActivityManager.MemoryInfo.availMem`) |

Không so sánh trực tiếp giá trị này giữa hai nền tảng. Trên Android, hàm có thể bị reject với mã `HARDWARE_ERROR` khi không đọc được phần cứng.

### `DeviceHelper.addBatteryListener(listener)`

Gọi `listener` mỗi khi mức pin hoặc trạng thái sạc thay đổi. `listener` nhận một đối tượng `BatteryInfo` gồm `batteryLevel` và `isCharging`.

Trả về hàm hủy đăng ký. Module chỉ theo dõi pin trong lúc còn ít nhất một listener: iOS bật theo dõi pin của `UIDevice`, Android đăng ký một `BroadcastReceiver` cho `ACTION_BATTERY_CHANGED`, và cả hai dừng khi listener cuối cùng bị hủy.

Sự kiện chỉ được gửi khi **mức pin hoặc trạng thái sạc thật sự thay đổi** so với lần báo trước. Hệ thống Android còn phát thông báo khi chỉ đổi nhiệt độ hoặc điện áp pin; những thay đổi đó không tạo sự kiện.

### Kiểu dữ liệu

```ts
import type { BatteryInfo, HardwareInfo } from "native-device-helper"
```

## Khác biệt giữa iOS và Android

| Hạng mục | iOS | Android |
| :--- | :--- | :--- |
| `model` | Mã phần cứng, ví dụ `iPhone17,1` | `Build.MODEL`, ví dụ `SM-J610F` |
| `osVersion` | Ví dụ `18.6.0` | Ví dụ `10` |
| `availableRamMb` | Bộ nhớ riêng của ứng dụng | RAM trống của toàn hệ thống |
| Mức pin trên emulator hoặc simulator | `-1` | Do emulator giả lập |
| Lỗi đọc phần cứng | Không có | Reject với `HARDWARE_ERROR` |
| Theo dõi pin | Thông báo của `UIDevice` | `BroadcastReceiver` |

## Giới hạn trên simulator

| Giá trị | Trên simulator | Nguyên nhân |
| :--- | :--- | :--- |
| `batteryLevel` | `-1` | Simulator không có pin |
| `availableRamMb` | `-1` | Tiến trình trên simulator không bị giới hạn bộ nhớ |
| `totalRamMb` | RAM của máy Mac | Simulator dùng chung bộ nhớ với máy chủ |
| Sự kiện pin | Không phát | Trạng thái pin không thay đổi |

Các giá trị này cần kiểm tra trên thiết bị thật.

**Kiểm tra sự kiện pin trên thiết bị Android thật mà không cần xả hoặc sạc pin:** dùng `adb` để giả lập, rồi trả về trạng thái thật khi xong.

```bash
adb shell dumpsys battery set level 55     # giả lập mức pin 55%
adb shell dumpsys battery set status 3     # giả lập trạng thái không sạc
adb shell dumpsys battery reset            # trả về trạng thái thật
```

Lệnh `dumpsys battery unplug` chỉ đổi nguồn điện mà không đổi trạng thái pin báo cáo, nên không tạo sự kiện.

## Lỗi thường gặp

| Hiện tượng | Nguyên nhân | Cách xử lý |
| :--- | :--- | :--- |
| `DeviceHelper.isAvailable` là `false` trên iOS hoặc Android | Chưa prebuild lại hoặc chưa build native sau khi thêm module | Chạy `npx expo prebuild --clean` rồi build lại |
| `batteryLevel` luôn là `-1` | Đang chạy trên simulator iOS | Kiểm tra trên thiết bị thật |
| Màn dùng module báo `TypeError: undefined is not a function` | Metro đang dùng bản `index.ts` cũ sau khi module đổi | Khởi động lại Metro với `--clear` |
| Android không nhận module dù đã chép vào `modules/` | Nhớ đệm `android/build/generated/autolinking` còn danh sách cũ | Xóa thư mục đó rồi build lại |
| Không nhận được sự kiện pin | Đang chạy trên simulator, hoặc listener đã bị hủy | Kiểm tra trên thiết bị thật; giữ listener trong suốt vòng đời màn hình |
| Jest báo `Cannot use import statement outside a module` tại `index.ts` của module | Jest không biên dịch mã TypeScript nằm trong `node_modules` | Thêm `native-device-helper` vào danh sách ngoại lệ của `transformIgnorePatterns` trong `jest.config.js` |
| Sửa file Swift hoặc Java nhưng không thấy thay đổi | Reload Metro không áp dụng thay đổi native | Build lại bằng `npx expo run:ios` hoặc `npx expo run:android` |
| `pod install` lỗi `Unicode Normalization not appropriate for ASCII-8BIT` | Shell không dùng mã hóa UTF-8 | Đặt `export LANG=en_US.UTF-8` rồi chạy lại |
