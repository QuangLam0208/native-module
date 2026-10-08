# Cấu trúc và khai báo module

[← Mục lục tài liệu](README.md)

Tài liệu mô tả các file bắt buộc của một module và vai trò của từng file.

Mỗi module là một thư mục trong repo `react-native-module`:

```text
react-native-module/
├── README.md
├── native-toast/
│   ├── expo-module.config.json   Khai báo module cho Autolinking
│   ├── package.json              Tên package, entry point, danh sách file phân phối
│   ├── index.ts                  API TypeScript
│   ├── README.md
│   ├── react-native.config.js    Cấu hình Autolinking của React Native cho Android
│   ├── ios/
│   │   ├── NativeToast.podspec       Khai báo pod
│   │   ├── NativeToastModule.swift   Khai báo module và hàm `show`
│   │   ├── ToastType.swift           Các loại toast và màu nền
│   │   └── ToastView.swift           Giao diện và hiệu ứng của toast
│   └── android/
│       ├── build.gradle              Thư viện Android
│       └── src/main/java/com/nativetoast/
│           ├── NativeToastModule.java    Khai báo module và hàm `show`
│           └── NativeToastPackage.java   ReactPackage đăng ký module
├── native-alert/
├── native-date-picker/
├── native-device-helper/
└── native-network-status/
```

> **Lưu ý:** `ios/` và `android/` trong cấu trúc trên là mã nguồn của module, khác với thư mục `ios/` và `android/` của ứng dụng (do prebuild sinh ra). Cấu trúc phần Android được mô tả tại [Tầng native Android](08-android-layer.md).

**Quy ước chia file Swift:** một file khai báo module (`...Module.swift`), các file còn lại mỗi file một trách nhiệm (kiểu dữ liệu, giao diện, lớp đọc dữ liệu). Class nằm ở file riêng phải để mức truy cập `internal` (mặc định) để file module gọi được; chỉ class module cần `public`.

## `package.json`

`name` là tên dùng khi import. `main` trỏ thẳng tới mã nguồn TypeScript, Metro biên dịch khi bundle nên module không cần bước build riêng. `files` phải liệt kê `ios`, `android`, `expo-module.config.json` và `react-native.config.js`, nếu thiếu thì bản cài về không liên kết được native.

```json
{
  "name": "native-toast",
  "version": "1.0.0",
  "description": "Native toast for iOS and Android",
  "main": "index.ts",
  "types": "index.ts",
  "files": ["index.ts", "ios", "android", "expo-module.config.json", "react-native.config.js"],
  "peerDependencies": {
    "expo": "*"
  },
  "license": "UNLICENSED",
  "private": true
}
```

## `expo-module.config.json`

Autolinking đọc file này để biết class nào là module. Giá trị trong `apple.modules` phải trùng tên class Swift. File này chỉ quản lý iOS; Android được liên kết bằng `react-native.config.js` (nội dung ở [Tầng native Android](08-android-layer.md#cấu-trúc-thư-mục-android)).

```json
{
  "platforms": ["apple"],
  "apple": { "modules": ["NativeToastModule"] }
}
```

## `ios/NativeToast.podspec`

```ruby
Pod::Spec.new do |s|
  s.name           = 'NativeToast'
  s.version        = '1.0.0'
  s.summary        = 'Native toast for iOS'
  s.description    = 'Shows a short, auto-dismissing toast on the key window.'
  s.author         = 'DVMS'
  s.homepage       = 'https://docs.expo.dev/modules/'
  s.platforms      = { :ios => '15.1' }
  s.swift_version  = '5.9'
  s.source         = { git: '' }
  s.static_framework = true

  s.dependency 'ExpoModulesCore'

  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES' }

  s.source_files = '**/*.{h,m,mm,swift}'
end
```

`source_files` bao gồm mọi file Swift trong `ios/`. Khi thêm file mới, không cần sửa podspec, chỉ cần chạy lại `pod install`.
