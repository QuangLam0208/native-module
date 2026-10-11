# Cấu trúc và khai báo module

[← Mục lục tài liệu](README.md)

Tài liệu mô tả các file bắt buộc của một module và vai trò của từng file.

Mỗi module là một thư mục trong repo `react-native-module`:

```text
react-native-module/
├── README.md
├── native-toast/
│   ├── expo-module.config.json   Khai báo module cho Autolinking (iOS)
│   ├── react-native.config.js    Khai báo module cho Autolinking (Android)
│   ├── package.json              Tên, phiên bản, entry point, danh sách file phân phối, script build
│   ├── tsconfig.json             Cấu hình biên dịch TypeScript
│   ├── .gitignore                Bỏ qua `node_modules`
│   ├── src/index.ts              API TypeScript (nguồn, sửa ở đây)
│   ├── build/                    JS đã biên dịch từ `src/` (sinh bằng `npm run build`, được commit)
│   ├── README.md                 Cách dùng và API của module
│   ├── CHANGELOG.md              Lịch sử phiên bản
│   ├── RELEASING.md              Các bước phát hành module
│   ├── ios/
│   │   ├── NativeToast.podspec       Khai báo pod
│   │   ├── NativeToastModule.swift   Khai báo module và hàm `show`
│   │   ├── ToastType.swift           Các loại toast và màu nền
│   │   └── ToastView.swift           Giao diện và hiệu ứng của toast
│   └── android/
│       ├── build.gradle              Thư viện Android, namespace, SDK
│       └── src/main/
│           ├── AndroidManifest.xml       Quyền hệ thống (nếu cần), gộp tự động vào ứng dụng
│           ├── res/                      Tài nguyên riêng (nếu cần, ví dụ theme)
│           └── java/com/nativetoast/
│               ├── NativeToastModule.java    Khai báo module và hàm `show`
│               └── NativeToastPackage.java   ReactPackage đăng ký module
├── native-alert/
├── native-date-picker/
├── native-device-helper/
└── native-network-status/
```

> **Lưu ý:** `ios/` và `android/` trong cấu trúc trên là mã nguồn của module, khác với thư mục `ios/` và `android/` của ứng dụng (do prebuild sinh ra). Cấu trúc phần Android được mô tả tại [Tầng native Android](08-android-layer.md).

**Quy ước chia file Swift:** một file khai báo module (`...Module.swift`), các file còn lại mỗi file một trách nhiệm (kiểu dữ liệu, giao diện, lớp đọc dữ liệu). Class nằm ở file riêng phải để mức truy cập `internal` (mặc định) để file module gọi được; chỉ class module cần `public`.

## Các file bắt buộc và tùy chọn

| File hoặc thư mục | Bắt buộc | Vai trò |
| :--- | :--- | :--- |
| `package.json` | Có | Tên, phiên bản, `main`, `files`, script `build` |
| `src/index.ts` | Có | API TypeScript, nơi sửa code JS |
| `build/` | Có | JS biên dịch sẵn; `main` của `package.json` trỏ vào đây. Phải commit |
| `tsconfig.json` | Có | Cấu hình `tsc`: đọc `src/`, ghi ra `build/` |
| `expo-module.config.json` | Có | Khai báo module iOS cho Autolinking của Expo |
| `ios/` (podspec và Swift) | Có | Phần iOS |
| `react-native.config.js` | Có nếu có Android | Khai báo module Android cho Autolinking của React Native |
| `android/` (gradle, Java, manifest) | Có nếu có Android | Phần Android |
| `README.md` | Có | Cách dùng, API, phạm vi đã kiểm thử |
| `CHANGELOG.md`, `RELEASING.md` | Nên có | Lịch sử phiên bản và các bước phát hành |
| `.gitignore` | Nên có | Bỏ `node_modules` để không commit nhầm |

## `package.json`

`name` là tên dùng khi import. `main` và `types` trỏ vào `build/`, là JavaScript đã biên dịch từ `src/` nên Jest và mọi công cụ đọc được mà không cần cấu hình thêm. `files` phải liệt kê `build`, `ios`, `android`, `expo-module.config.json` và `react-native.config.js`, thiếu thì bản cài về không chạy hoặc không liên kết được native. Script `prepack` chạy `tsc` khi đóng gói (`npm pack`, `npm publish`); cài trực tiếp từ git thì không chạy bước này, nên `build/` phải được commit sẵn.

```json
{
  "name": "native-toast",
  "version": "1.0.0",
  "description": "Native toast for iOS and Android",
  "main": "build/index.js",
  "types": "build/index.d.ts",
  "files": [
    "build",
    "ios",
    "android",
    "expo-module.config.json",
    "react-native.config.js"
  ],
  "scripts": {
    "build": "tsc",
    "prepack": "tsc"
  },
  "peerDependencies": {
    "expo": "*"
  },
  "devDependencies": {
    "typescript": "^5.9.0"
  },
  "license": "UNLICENSED",
  "private": true
}
```

`peerDependencies.expo` được `npm install` tự cài khi chạy build trong thư mục module, nên không cần khai báo thêm `react-native` hay kiểu của chúng. Số `version` là nguồn chính của phiên bản; số `s.version` trong podspec phải khớp (xem [Phát hành](09-releasing.md)).

## `tsconfig.json`

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "Node16",
    "moduleResolution": "Node16",
    "lib": ["ES2020"],
    "jsx": "react",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "declaration": true,
    "rootDir": "src",
    "outDir": "build"
  },
  "include": ["src"]
}
```

`module: Node16` cho ra CommonJS (`require`), nên Jest đọc được file trong `node_modules`. `declaration: true` sinh `build/index.d.ts` để editor vẫn gợi ý kiểu. Cả năm module dùng cùng một `tsconfig.json`.

Sinh `build/` bằng:

```bash
npm install
```

```bash
npm run build
```

`npm install` chỉ cần lần đầu. Không sửa tay `build/`; sửa `src/index.ts` rồi build lại.

## `react-native.config.js`

Báo cho Autolinking của React Native biết module chỉ có Android (iOS đã do Expo quản lý):

```js
module.exports = {
  dependency: {
    platforms: {
      ios: null,
    },
  },
};
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
