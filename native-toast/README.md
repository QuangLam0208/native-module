# native-toast

Native module hiển thị toast trên iOS và Android, dùng chung một API TypeScript.

- **iOS:** viết bằng Swift với Expo Modules API, tự vẽ toast là một thanh màu bo góc gần đáy màn hình.
- **Android:** viết bằng Java theo kiểu module React Native cổ điển, hiển thị toast bằng thư viện [Toasty](https://github.com/GrenderG/Toasty) (`es.dmoral.toasty`).

Toast không chặn thao tác của người dùng và tự ẩn sau một khoảng thời gian ngắn. Có bốn loại: `normal`, `success`, `warning`, `error`.

## Thông tin chung

| Hạng mục | Giá trị |
| :--- | :--- |
| Nền tảng | iOS 15.1 trở lên · Android SDK 24 trở lên |
| Ngôn ngữ | Swift (iOS) · Java (Android) |
| Framework | Expo Modules API (iOS) · React Native Bridge (Android) |
| Thư viện bên thứ ba | Toasty 1.5.2, chỉ trên Android, phát hành trên JitPack |
| Đã kiểm thử với | Expo SDK 57, React Native 0.86 |
| Môi trường chạy | Development build hoặc bản release. Expo Go không hỗ trợ. |

Trên nền tảng khác iOS và Android, module không tồn tại: `NativeToast.isAvailable` là `false` và `NativeToast.show` không làm gì, ứng dụng không phát sinh lỗi.

**Phạm vi đã kiểm thử:**

| Nền tảng | Thiết bị | Đã kiểm thử | Chưa kiểm thử |
| :--- | :--- | :--- | :--- |
| iOS | Simulator iOS 18.6 và 27.0 | Bốn loại toast, thời lượng mặc định và tùy chỉnh | `INVALID_DURATION`, toast không chặn thao tác bên dưới |
| Android | Samsung Galaxy J6+ (SM-J610F), Android 10, bản build `armeabi-v7a` | Bốn loại toast hiện đúng nội dung và đúng màu, module được liên kết khi nằm trong thư mục `modules/` | Thời lượng, gọi liên tiếp, Android 11 trở lên, thiết bị 64-bit, emulator |

## Cấu trúc

```text
native-toast/
├── expo-module.config.json       Khai báo module cho Expo Autolinking (iOS)
├── react-native.config.js        Khai báo module cho React Native Autolinking (Android)
├── package.json
├── index.ts                      API TypeScript, chọn module native theo nền tảng
├── ios/
│   ├── NativeToast.podspec
│   ├── NativeToastModule.swift   Khai báo module và hàm `show`
│   ├── ToastType.swift           Các loại toast và màu nền
│   └── ToastView.swift           Giao diện và hiệu ứng của toast
└── android/
    ├── build.gradle              Cấu hình thư viện, khai báo Toasty và kho JitPack
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/nativetoast/
            ├── NativeToastModule.java    Hiển thị toast bằng Toasty theo `type`
            └── NativeToastPackage.java   Đăng ký module với React Native
```

Hai nền tảng dùng hai cơ chế khác nhau nhưng cùng tên module `NativeToast`, cùng tên hàm `show` và cùng thứ tự tham số. `index.ts` chọn module theo `Platform.OS`: iOS gọi `requireOptionalNativeModule("NativeToast")`, Android gọi `NativeModules.NativeToast`.

## Cài đặt

Ứng dụng phải dùng Expo Prebuild. Module được liên kết tự động qua Autolinking, không cần chỉnh sửa `Podfile`, `AppDelegate`, `Info.plist`, `MainApplication` hay `settings.gradle`.

### Cách 1: Cài như một dependency qua Git (khuyến nghị)

Module nằm trong thư mục con `native-toast` của repo `react-native-module`, nên cần chỉ rõ đường dẫn bằng `path:`:

```bash
pnpm add "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#path:/native-toast"
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
import { NativeToast } from "native-toast"
```

pnpm ghi mã commit đã cài vào `pnpm-lock.yaml`, nên mọi máy trong dự án dùng cùng một phiên bản. Để cố định theo một commit hoặc tag cụ thể:

```bash
pnpm add "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#<commit-hoặc-tag>&path:/native-toast"
```

**Cập nhật lên phiên bản mới:** chạy lại lệnh cài để pnpm lấy commit mới nhất, sau đó chạy `pod install` trong thư mục `ios/` của ứng dụng (hoặc prebuild lại) và build lại native bằng `npx expo run:ios`.

Bước `pod install` là bắt buộc với mọi lần cập nhật, kể cả khi chỉ đổi mã TypeScript: đường dẫn cài đặt của module trong `node_modules/.pnpm` chứa mã commit, nên pod phải được trỏ lại sang đường dẫn mới. Nếu Metro đang chạy, khởi động lại Metro sau khi cài.

### Cách 2: Module cục bộ trong ứng dụng

Dùng khi cần chỉnh sửa mã nguồn module ngay trong ứng dụng. Clone repo rồi sao chép thư mục `native-toast` vào `modules/` ở gốc ứng dụng:

```bash
git clone https://git.itzsolution.com/scm/tsa/react-native-module.git
```

```bash
cp -R react-native-module/native-toast <thư-mục-ứng-dụng>/modules/native-toast
```

Sau đó prebuild và build như Cách 1. Import bằng đường dẫn tương đối:

```ts
import { NativeToast } from "../modules/native-toast"
```

> Cách 2 đã được kiểm thử trên `react-native-base`. Cách 1 đã được kiểm thử với pnpm 11.13.0 khi module còn nằm ở một repo GitHub; lệnh cài từ repo này chưa được kiểm thử. Không dùng đồng thời hai cách trong cùng một ứng dụng.

### Yêu cầu riêng của Android

- **Thư viện Toasty** được tải từ JitPack. `build.gradle` của module đã khai báo kho `https://jitpack.io`. Nếu Gradle báo không tìm thấy `com.github.GrenderG:Toasty`, thêm dòng `maven { url "https://jitpack.io" }` vào khối `repositories` ở `android/build.gradle` của ứng dụng.
- **Liên kết khi module nằm trong thư mục `modules/` của ứng dụng:** Expo quét thư mục này cho cả Android, nên không cần khai báo module trong `package.json`. Điều này đã được kiểm thử trên `react-native-base`.
- **Kiến trúc CPU:** muốn build nhanh hoặc nhẹ, chỉ build kiến trúc của thiết bị đang dùng, ví dụ `ORG_GRADLE_PROJECT_reactNativeArchitectures=armeabi-v7a npx expo run:android`. Bản chỉ có `armeabi-v7a` không chạy trên thiết bị hoặc emulator 64-bit.

## Sử dụng

Toàn bộ API nằm trong một đối tượng `NativeToast`:

```tsx
import { NativeToast } from "native-toast"

// Mặc định: NativeToast.Type.Normal, NativeToast.Duration.Short
await NativeToast.show("Đã sao chép")

// Chọn loại toast
await NativeToast.show("Lưu thành công", NativeToast.Type.Success)
await NativeToast.show("Hãy kiểm tra kết nối", NativeToast.Type.Warning)
await NativeToast.show("Đã có lỗi xảy ra", NativeToast.Type.Error)

// Thời lượng có sẵn
await NativeToast.show("Đang đồng bộ dữ liệu", NativeToast.Type.Normal, NativeToast.Duration.Long)

// Thời lượng tùy chỉnh, tính bằng mili giây
await NativeToast.show("Hiển thị trong 5 giây", NativeToast.Type.Normal, 5000)

// Ẩn hoặc vô hiệu hóa giao diện khi module không khả dụng
<Button disabled={!NativeToast.isAvailable} onPress={() => void NativeToast.show("Xin chào")} />
```

## API

### `NativeToast.show(message, type?, durationMs?)`

| Tham số | Kiểu | Mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `message` | `string` | Bắt buộc | Nội dung hiển thị. iOS giới hạn 4 dòng |
| `type` | `NativeToastType` | `NativeToast.Type.Normal` | Loại toast, quyết định màu nền. Giá trị không nhận ra được xử lý như `normal` trên Android |
| `durationMs` | `number` | `NativeToast.Duration.Short` | Thời gian hiển thị, tính bằng mili giây. Dùng `NativeToast.Duration` hoặc một số dương bất kỳ. Android chỉ có hai mức, xem mục [Khác biệt giữa iOS và Android](#khác-biệt-giữa-ios-và-android) |

Trả về `Promise<void>`. Trên iOS, Promise bị reject trong hai trường hợp:

| Mã lỗi | Trường hợp |
| :--- | :--- |
| `INVALID_DURATION` | `durationMs` nhỏ hơn hoặc bằng 0 |
| `NO_WINDOW` | Ứng dụng chưa có cửa sổ hiển thị |

Android không phát sinh hai lỗi này: Promise luôn hoàn tất ngay khi gọi, không chờ toast hiện lên.

### `NativeToast.isAvailable`

`boolean`. `true` khi module native đã được liên kết vào bản build đang chạy.

### `NativeToast.Type`

Hằng số chứa các loại toast.

| Hằng số | Giá trị | Màu nền trên iOS | Trên Android (Toasty) |
| :--- | :--- | :--- | :--- |
| `NativeToast.Type.Normal` | `"normal"` | `#353A3E` | Nền xám nhạt, chữ tối |
| `NativeToast.Type.Success` | `"success"` | `#388E3C` | Nền xanh lá, có dấu tích |
| `NativeToast.Type.Warning` | `"warning"` | `#FFA900` | Nền cam vàng, có dấu chấm than |
| `NativeToast.Type.Error` | `"error"` | `#D50000` | Nền đỏ, có dấu X |

Trên iOS chữ luôn có màu trắng. Trên Android màu do Toasty quyết định, gần với bảng màu của iOS, riêng `normal` sáng hơn iOS.

### `NativeToast.Duration`

Hằng số thời lượng có sẵn, tính bằng mili giây.

| Hằng số | Giá trị |
| :--- | :--- |
| `NativeToast.Duration.Short` | `2200` |
| `NativeToast.Duration.Long` | `3500` |

### `NativeToastType`

Kiểu TypeScript của tham số `type`, dùng khi cần khai báo kiểu cho biến hoặc tham số:

```ts
import { NativeToast, type NativeToastType } from "native-toast"

const type: NativeToastType = NativeToast.Type.Success
```

## Hành vi

### iOS

- Mỗi thời điểm chỉ có một toast. Toast mới thay thế toast đang hiển thị.
- Toast nằm trên cửa sổ chính của ứng dụng, cách đáy vùng an toàn 24pt, rộng tối đa 90% màn hình.
- Toast không nhận thao tác chạm, các thành phần bên dưới vẫn bấm được.

### Android

- Toast là toast hệ thống do Toasty vẽ, nằm gần đáy màn hình, có biểu tượng riêng cho từng loại trừ `normal`.
- Hàm chạy trên UI thread.
- Cách xử lý khi gọi liên tiếp phụ thuộc vào hệ thống Android và chưa được kiểm thử.

### Khác biệt giữa iOS và Android

| Hạng mục | iOS | Android |
| :--- | :--- | :--- |
| Cách vẽ toast | Tự vẽ bằng `UIView` | Toast hệ thống, do Toasty tô màu |
| Màu theo loại | Bốn màu cố định | Bốn màu của Toasty, `normal` sáng hơn |
| Biểu tượng | Không | Có, trừ `normal` |
| Thời lượng | Đúng theo `durationMs` | Hai mức: `durationMs ≤ 2500` ra mức ngắn (khoảng 2 giây), lớn hơn ra mức dài (khoảng 3,5 giây) |
| Kiểm tra `durationMs` | Từ chối giá trị nhỏ hơn hoặc bằng 0 | Không kiểm tra |
| Lỗi `NO_WINDOW` | Có | Không |
| Thời điểm Promise hoàn tất | Sau khi toast được hiện | Ngay khi gọi |

Với `NativeToast.Duration.Short` (2200) và `NativeToast.Duration.Long` (3500) hai nền tảng cho kết quả tương đương. Giá trị tùy chỉnh như 5000 ms chỉ đúng trên iOS; trên Android toast hiện ở mức dài.

## Lỗi thường gặp

| Hiện tượng | Nguyên nhân | Cách xử lý |
| :--- | :--- | :--- |
| `NativeToast.isAvailable` là `false` trên iOS hoặc Android | Chưa prebuild lại hoặc chưa build native sau khi thêm module | Chạy `npx expo prebuild --clean` rồi build lại |
| Gradle báo không tìm thấy `com.github.GrenderG:Toasty` | Không tải được từ JitPack | Thêm `maven { url "https://jitpack.io" }` vào `repositories` của `android/build.gradle` trong ứng dụng, kiểm tra kết nối mạng |
| Toast Android không có màu theo `type` | Đang dùng bản module cũ dùng `Toast.makeText` thuần, bỏ qua `type` | Cập nhật module lên bản có Toasty rồi build lại Android |
| Toast Android hiện lâu hơn hoặc ngắn hơn mong đợi | Android chỉ có hai mức thời lượng | Dùng `NativeToast.Duration.Short` hoặc `Long` |
| Cài APK báo `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Máy đã có ứng dụng cùng mã gói nhưng ký bằng khóa khác | Đổi mã gói của bản build thử, hoặc gỡ ứng dụng cũ (xóa dữ liệu của ứng dụng đó) |
| Build Android lỗi `No space left on device` | Ổ đĩa đầy trong lúc Gradle biên dịch | Giải phóng ổ đĩa, hoặc chỉ build một kiến trúc CPU (xem Yêu cầu riêng của Android) |
| Jest báo `Cannot use import statement outside a module` tại `index.ts` của module | Jest không biên dịch mã TypeScript nằm trong `node_modules` | Thêm `native-toast` vào danh sách ngoại lệ của `transformIgnorePatterns` trong `jest.config.js` |
| Sửa file Swift hoặc Java nhưng không thấy thay đổi | Reload Metro không áp dụng thay đổi native | Build lại bằng `npx expo run:ios` hoặc `npx expo run:android` |
| `pod install` lỗi `Unicode Normalization not appropriate for ASCII-8BIT` | Shell không dùng mã hóa UTF-8 | Đặt `export LANG=en_US.UTF-8` rồi chạy lại |
| Promise reject với `NO_WINDOW` | Gọi khi ứng dụng chưa có cửa sổ hiển thị | Gọi sau khi màn hình đầu tiên đã render |
