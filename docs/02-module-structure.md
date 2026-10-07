# Cấu trúc và khai báo module

[← Mục lục tài liệu](README.md)

Tài liệu mô tả các file bắt buộc của một module và vai trò của từng file.

Mỗi module là một thư mục trong repo `native-module`:

```text
native-module/
├── README.md
├── native-toast/
│   ├── expo-module.config.json   Khai báo module cho Autolinking (iOS + Android)
│   ├── package.json              Tên package, entry point, danh sách file phân phối
│   ├── index.ts                  API TypeScript dùng chung cho cả hai nền tảng
│   ├── README.md
│   ├── ios/
│   │   ├── NativeToast.podspec       Khai báo pod
│   │   ├── NativeToastModule.swift   Khai báo module và hàm `show`
│   │   ├── ToastType.swift           Các loại toast và màu nền
│   │   └── ToastView.swift           Giao diện và hiệu ứng của toast
│   └── android/
│       ├── build.gradle                              Khai báo Gradle cho module
│       └── src/main/
│           ├── AndroidManifest.xml
│           └── java/com/nativetoast/
│               ├── NativeToastModule.java            Logic native Android
│               └── NativeToastPackage.java           ReactPackage cho Autolinking
├── native-device-helper/
├── native-alert/
├── native-date-picker/
└── native-network-status/
```

> **Lưu ý:** `ios/` và `android/` trong cấu trúc trên là mã nguồn của module, khác với thư mục
> `ios/` và `android/` của ứng dụng (do prebuild sinh ra).

## `package.json`

`name` là tên dùng khi import. `main` trỏ thẳng tới mã nguồn TypeScript, Metro biên dịch khi
bundle nên module không cần bước build riêng. `files` phải liệt kê cả `ios`, `android` và
`expo-module.config.json`, nếu thiếu thì bản cài về không liên kết được native.

```json
{
  "name": "native-toast",
  "version": "1.0.0",
  "description": "Native toast for iOS and Android",
  "main": "index.ts",
  "types": "index.ts",
  "files": ["index.ts", "ios", "android", "expo-module.config.json"],
  "peerDependencies": {
    "expo": "*"
  },
  "license": "UNLICENSED",
  "private": true
}
```

## `expo-module.config.json`

Autolinking đọc file này để biết class nào là module cho từng nền tảng.

- Phía iOS: giá trị trong `apple.modules` phải trùng tên **class Swift**.
- Phía Android: giá trị trong `android.modules` phải là **fully-qualified class name** của class `ReactPackage`.

```json
{
  "platforms": ["apple", "android"],
  "apple": { "modules": ["NativeToastModule"] },
  "android": { "modules": ["com.nativetoast.NativeToastPackage"] }
}
```

## `ios/<Tên>.podspec`

```ruby
Pod::Spec.new do |s|
  s.name           = 'NativeToast'
  s.version        = '1.0.0'
  s.summary        = 'Native toast for iOS'
  s.author         = 'DVMS'
  s.platforms      = { :ios => '15.1' }
  s.swift_version  = '5.9'
  s.source         = { git: '' }
  s.static_framework = true

  s.dependency 'ExpoModulesCore'

  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES' }
  s.source_files = '**/*.{h,m,mm,swift}'
end
```

`source_files` bao gồm mọi file Swift trong `ios/`. Khi thêm file mới, không cần sửa podspec.

## `android/build.gradle`

```groovy
apply plugin: "com.android.library"
apply plugin: "com.facebook.react"

android {
  namespace "com.nativetoast"
  compileSdkVersion 36
  defaultConfig { minSdkVersion 24 }
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_17
    targetCompatibility JavaVersion.VERSION_17
  }
}

dependencies {
  implementation "com.facebook.react:react-android"
}
```

`namespace` phải khớp với `package` khai báo ở đầu các file Java.
