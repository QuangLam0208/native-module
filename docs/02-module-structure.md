# Cấu trúc và khai báo module

[← Mục lục tài liệu](README.md)

Tài liệu mô tả các file bắt buộc của một module và vai trò của từng file.

Mỗi module là một thư mục trong repo `native-module`:

```text
native-module/
├── README.md
├── native-toast/
│   ├── expo-module.config.json   Khai báo module cho Expo Autolinking (iOS)
│   ├── react-native.config.js    Khai báo module cho React Native CLI Autolinking (Android)
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
bundle nên module không cần bước build riêng. `files` phải liệt kê cả `ios`, `android`,
`expo-module.config.json` và `react-native.config.js`, nếu thiếu thì bản cài về không liên kết được native.

```json
{
  "name": "native-toast",
  "version": "1.0.0",
  "description": "Native toast for iOS and Android",
  "main": "index.ts",
  "types": "index.ts",
  "files": [
    "index.ts",
    "ios",
    "android",
    "expo-module.config.json",
    "react-native.config.js"
  ],
  "peerDependencies": {
    "expo": "*"
  },
  "license": "UNLICENSED",
  "private": true
}
```

## `expo-module.config.json`

File này dùng cho **Expo Modules Autolinking (iOS)**.

Vì phía iOS viết bằng Swift kế thừa lớp `Module` của Expo Modules API (`class NativeToastModule: Module`), Expo Autolinking sẽ đọc `apple.modules` để liên kết module.

```json
{
  "platforms": ["apple"],
  "apple": {
    "modules": ["NativeToastModule"]
  }
}
```

> **Lưu ý quan trọng về Android:**
> Phía Android trong repo này dùng **React Native Bridge** thuần (`ReactContextBaseJavaModule` + `ReactPackage`), **không** kế thừa lớp `expo.modules.kotlin.modules.Module`.
> Do đó, **không** khai báo `"android"` trong `expo-module.config.json`. Nếu khai báo class `ReactPackage` vào `android.modules`, compiler Kotlin của Expo (`:expo:compileReleaseKotlin`) sẽ báo lỗi:
> `Type mismatch: inferred type is 'Class<...Package>', but 'Class<out Module>' was expected`
> Việc liên kết Android được tách riêng cho React Native CLI Autolinking xử lý qua `react-native.config.js`.

## `react-native.config.js`

File này cấu hình cho **React Native CLI Autolinking (Android)**.

```javascript
module.exports = {
  dependency: {
    platforms: {
      ios: null, // Vô hiệu hóa autolink RN CLI trên iOS để tránh xung đột với Expo Autolinking
    },
  },
};
```

- Phía Android: React Native CLI sẽ tự động quét các class triển khai `ReactPackage` trong thư mục `android/src/main/java/` và đăng ký vào `PackageList.java` khi biên dịch ứng dụng.
- Phía iOS: Đặt `ios: null` để ngăn React Native CLI cố gắng autolink podspec trên iOS (việc này đã do Expo Autolinking đảm nhiệm qua `expo-module.config.json`).

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
