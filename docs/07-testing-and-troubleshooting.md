# Kiểm thử và xử lý lỗi

[← Mục lục tài liệu](README.md)

Tài liệu mô tả cách kiểm thử module trên iOS và Android, giới hạn của simulator, tình trạng kiểm thử hiện tại và các lỗi thường gặp.

## Lệnh kiểm thử

```bash
npx tsc --noEmit     # Kiểm tra kiểu TypeScript
npx jest             # Chạy test, nếu ứng dụng dùng Jest
npx expo run:ios     # Build và chạy trên simulator, yêu cầu macOS và Xcode
npx expo run:android # Build và chạy trên thiết bị hoặc emulator Android
```

Kiểm thử Android không cần màn hình, chỉ cần `adb`:

```bash
adb shell dumpsys battery set level 55     # Giả lập mức pin, kích hoạt sự kiện pin
adb shell dumpsys battery set status 3     # Không sạc
adb shell dumpsys battery reset            # Trả về trạng thái thật
adb shell svc wifi disable                 # Tắt Wi-Fi, kích hoạt sự kiện mạng
adb shell svc wifi enable
adb shell dumpsys window | grep mCurrentFocus   # Dialog nào đang hiện
```

Mỗi module cần một màn hình trong ứng dụng để gọi từng hàm và quan sát kết quả. Thay đổi mã TypeScript được Metro nạp lại ngay; thay đổi mã Swift hoặc Java cần build lại.

## Giới hạn của simulator

| Giá trị | Trên simulator | Nguyên nhân |
| :--- | :--- | :--- |
| Mức pin | `-1` | Simulator không có pin |
| RAM ứng dụng còn dùng được | `-1` | Tiến trình trên simulator không bị giới hạn bộ nhớ |
| Tổng RAM | RAM của máy Mac | Simulator dùng chung bộ nhớ với máy chủ |
| Sự kiện pin | Không phát | Trạng thái pin không thay đổi |
| Trạng thái mạng | Mạng của máy Mac | Simulator dùng chung mạng với máy chủ |

## Tình trạng kiểm thử

| Module | Đã kiểm thử trên simulator | Chưa kiểm thử |
| :--- | :--- | :--- |
| `native-toast` | Bốn loại toast, thời lượng mặc định và tùy chỉnh | Lỗi `INVALID_DURATION`, toast không chặn thao tác bên dưới |
| `native-alert` | Hai nút, không tiêu đề, ba nút, callback của từng nút | Gọi liên tiếp khi alert trước chưa đóng |
| `native-date-picker` | Sáu trường hợp chọn, bước nhảy phút, theme tối, ràng buộc khoảng | `minimumDate`, `maximumDate`, theme sáng, vuốt để hủy, iPad |
| `native-device-helper` | Model, phiên bản hệ điều hành, tổng RAM | Mức pin, RAM khả dụng, sự kiện pin (cần thiết bị thật) |
| `native-network-status` | `getStatus` với Wi-Fi | Sự kiện thay đổi, mất mạng, mạng di động |

**Android** (Samsung Galaxy J6+, Android 10):

| Module | Đã kiểm thử | Chưa kiểm thử |
| :--- | :--- | :--- |
| `native-toast` | Bốn loại toast với màu và biểu tượng riêng (Toasty) | Thời lượng tùy chỉnh dài |
| `native-alert` | Hai và ba nút, callback từng nút, chạm ngoài và Back đều là hủy | Gọi liên tiếp khi alert trước chưa đóng |
| `native-date-picker` | Chọn một ngày, khoảng ngày trong một dialog Material, hủy | Giờ, ngày giờ, `minimumDate`, `maximumDate` |
| `native-device-helper` | Model, phiên bản hệ điều hành, RAM, mức pin, sự kiện pin (giả lập bằng `dumpsys battery`) | Pin thật khi cắm và rút sạc |
| `native-network-status` | `getStatus`, tắt và bật Wi-Fi (mỗi lần đúng một sự kiện) | Mạng di động, `isExpensive`, `isConstrained` |

## Lỗi thường gặp

### Cài đặt và liên kết

| Hiện tượng | Nguyên nhân | Cách xử lý |
| :--- | :--- | :--- |
| `Cannot find module '<tên-module>'` khi typecheck hoặc bundle | Chưa cài package | Chạy lệnh `pnpm add` tại [Tích hợp vào ứng dụng](05-integration.md) |
| `isAvailable` là `false` trên iOS | Chưa prebuild lại hoặc chưa build native sau khi cài module | `npx expo prebuild --clean` rồi build lại |
| Module không được Autolinking nhận | Thiếu hoặc sai `expo-module.config.json`; tên class không khớp mục `apple.modules` | Đối chiếu tên class Swift với file cấu hình |
| Bản cài về thiếu thư mục `ios/` | Trường `"files"` trong `package.json` của module không liệt kê `ios` | Bổ sung `ios` và `expo-module.config.json` vào `"files"` |
| Pod không link | Podspec nằm sai chỗ hoặc thiếu `s.dependency 'ExpoModulesCore'` | Đặt podspec trong `ios/`, kiểm tra dependency |
| Build lỗi không tìm thấy file Swift của module sau khi cập nhật | Pod còn trỏ tới đường dẫn của commit cũ | Chạy `pod install` rồi build lại |
| Metro báo `spawn ... ENOENT` sau khi cài hoặc cập nhật package | Tiến trình Metro đang chạy còn giữ đường dẫn cũ trong `node_modules` | Khởi động lại Metro |
| `pod install` lỗi `Unicode Normalization not appropriate for ASCII-8BIT` | Shell không dùng mã hóa UTF-8 | Đặt `export LANG=en_US.UTF-8` rồi chạy lại |
| Jest báo `Cannot use import statement outside a module` tại `index.ts` của module | Jest không biên dịch mã TypeScript nằm trong `node_modules` | Thêm tên module vào `transformIgnorePatterns` |

### Android

| Hiện tượng | Nguyên nhân | Cách xử lý |
| :--- | :--- | :--- |
| `isAvailable` là `false` trên Android sau khi thêm module | `autolinking.json` cũ chưa có module | Xóa `android/build/generated/autolinking` rồi build lại |
| `TypeError: undefined is not a function` khi gọi module | Metro còn cache `index.ts` cũ | Khởi động lại Metro với `--clear` |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Máy đang có bản cùng package ký bằng khóa khác | Gỡ bản cũ bằng `adb uninstall <package>` rồi cài lại |
| `No space left on device` khi build | Ổ đĩa đầy do build nhiều kiến trúc | Giải phóng ổ đĩa, đặt `ORG_GRADLE_PROJECT_reactNativeArchitectures` theo máy thử |
| Không tìm thấy thiết bị theo serial | `expo run:android --device` nhận tên model | Dùng tên model (ví dụ `SM_J610F`) |
| Ứng dụng báo không kết nối được Metro | Thiếu cổng cho thiết bị thật | `adb reverse tcp:8081 tcp:8081` |
| Crash `requires your app theme to be Theme.MaterialComponents` | Dùng thành phần Material với theme ứng dụng là AppCompat | Dùng theme riêng của module (xem `NativeDatePicker.Calendar`) |
| Không tải được thư viện từ JitPack | Thiếu kho `https://jitpack.io` | Thêm vào `repositories` của `android/build.gradle` module |
| Dialog hệ thống không hiện trong `uiautomator dump` | Dump không đọc được cửa sổ dialog khi đang mở | Dùng `dumpsys window` để biết cửa sổ đang lấy focus |

### Biên dịch Swift

| Hiện tượng | Nguyên nhân | Cách xử lý |
| :--- | :--- | :--- |
| `Cannot find '...' in scope` | File Swift mới chưa có trong Pods project, hoặc class khai báo `private` | Chạy lại `pod install`; bỏ `private` ở class dùng chung |
| Lỗi biên dịch tại `.runOnQueue` | Gọi `.runOnQueue` trên `Function` (hàm đồng bộ) | Đổi sang `AsyncFunction` |
| Lỗi `swift_version` khi biên dịch | Thiếu `s.swift_version` trong podspec | Thêm `s.swift_version = '5.9'` |

### Khi chạy

| Hiện tượng | Nguyên nhân | Cách xử lý |
| :--- | :--- | :--- |
| Ứng dụng thoát ngay khi mở trên iOS 27 | Chưa dùng UIKit scene life cycle | Bật plugin `withSceneLifecycle` rồi prebuild lại, xem [Tích hợp vào ứng dụng](05-integration.md#tương-thích-ios-27) |
| Crash `UIKit must be used from main thread` | Lời gọi UIKit chạy ngoài main thread | Khai báo bằng `AsyncFunction` kèm `.runOnQueue(.main)` |
| Gọi hàm báo không tìm thấy hàm | Tên chuỗi phía Swift khác tên gọi phía TypeScript | Đối chiếu ba cặp tên tại [Tầng native Swift](03-swift-layer.md#tên-dạng-chuỗi) |
| Promise không bao giờ hoàn tất | Hàm nhận `promise` nhưng có nhánh không gọi `resolve` hay `reject` | Bảo đảm mọi nhánh đều kết thúc Promise |
| Listener không được gọi lúc vừa đăng ký | Sự kiện chỉ báo thay đổi, không báo trạng thái hiện tại | Đọc trạng thái ban đầu bằng hàm lấy dữ liệu |
| Sửa file Swift nhưng không thấy thay đổi | Reload Metro không áp dụng thay đổi native | Build lại bằng `npx expo run:ios` |
| Sheet sáng trong khi ứng dụng tối | Sheet theo chế độ của thiết bị, ứng dụng có theme riêng | Truyền `theme` cho `NativeDatePicker` |
| Deep link mở nhầm ứng dụng trên simulator | Hai bản build của ứng dụng cùng đăng ký một URL scheme | Gỡ bản build cũ khỏi simulator |
