# Tích hợp vào ứng dụng

[← Mục lục tài liệu](README.md)

Tài liệu mô tả cách cài module vào một ứng dụng Expo, cách cập nhật, kiểm tra Autolinking cho cả iOS và Android, và yêu cầu riêng của iOS 27.

## Cài đặt

Quy trình chuẩn: **cài package → prebuild → build native**.

```bash
# Module nằm trong thư mục con của repo nên cần chỉ rõ đường dẫn bằng path:
pnpm add "github:loikimtrang/native-module#path:/native-toast"

# Sinh lại thư mục native (Autolinking tự nhận diện podspec và gradle)
npx expo prebuild --clean

# Chạy trên iOS:
npx expo run:ios

# Chạy trên Android:
npx expo run:android
```

Sau khi cài, `package.json` của ứng dụng có thêm dependency:

```json
"native-alert": "github:loikimtrang/native-module#path:/native-alert",
"native-date-picker": "github:loikimtrang/native-module#path:/native-date-picker",
"native-device-helper": "github:loikimtrang/native-module#path:/native-device-helper",
"native-network-status": "github:loikimtrang/native-module#path:/native-network-status",
"native-toast": "github:loikimtrang/native-module#path:/native-toast"
```

pnpm ghi mã commit đã cài vào `pnpm-lock.yaml`, nên mọi máy trong dự án dùng cùng một phiên bản của module.

## Cấu hình Jest

Module phân phối dưới dạng mã TypeScript. Jest mặc định không biên dịch mã trong `node_modules`, nên tên mỗi module phải được thêm vào danh sách ngoại lệ của `transformIgnorePatterns` trong `jest.config.js`:

```js
transformIgnorePatterns: [
  "/node_modules/(?!(.pnpm|react-native|...|native-toast|native-device-helper|native-alert|native-date-picker|native-network-status|mobx))",
],
```

## Quy trình cập nhật module

| Bước | Nơi thực hiện | Thao tác |
| :--- | :--- | :--- |
| 1 | Repo `native-module` | Sửa mã nguồn, commit và push |
| 2 | Ứng dụng | Chạy lại lệnh `pnpm add` để lấy commit mới |
| 3 | Ứng dụng | Chạy prebuild (hoặc `pod install` cho iOS) rồi build lại native |
| 4 | Ứng dụng | Khởi động lại Metro |

Bước 3 là bắt buộc với mọi lần cập nhật, kể cả khi chỉ đổi mã TypeScript: đường dẫn cài đặt của module trong `node_modules/.pnpm` chứa mã commit, nên cấu hình native phải được trỏ lại sang đường dẫn mới.

## Phát triển module trước khi push (Local Dev)

Trong lúc viết module, cài tạm từ thư mục repo trên máy để thử, chạy ổn rồi mới push và chuyển sang cài qua Git:

```bash
pnpm add "file:<đường-dẫn-tới-repo>/native-network-status"
```

Ví dụ trên Windows:
```bash
pnpm add "file:D:/RN/native-module-main/native-device-helper"
```

Sau mỗi lần sửa mã trong repo, chạy lại lệnh trên để sao chép bản mới vào `node_modules`.

## Quy định tích hợp

| Hạng mục | Quy định |
| :--- | :--- |
| Liên kết native | Tự động qua Autolinking dựa trên `expo-module.config.json`. Không chỉnh sửa thủ công `Podfile`, `AppDelegate`, `settings.gradle`, hay `MainApplication`. |
| Quyền hệ thống | Khai báo trong `AndroidManifest.xml` của module (ví dụ `ACCESS_NETWORK_STATE`) hoặc qua config plugin của Expo. |
| Môi trường chạy | Development build hoặc bản release. Expo Go không hỗ trợ. |
| Thay đổi mã Swift / Java | Bắt buộc build lại native (`run:ios` hoặc `run:android`). Reload Metro không áp dụng thay đổi native. |
| Thêm file native mới | Chạy lại prebuild trước khi build. |
| Chỉnh sửa module | Thực hiện tại repo `native-module`. Không sửa trực tiếp trong `node_modules`. |

## Kiểm tra Autolinking đã nhận module

**Cho iOS:**
```bash
npx expo-modules-autolinking resolve --platform apple
```
Kết quả phải có mục của module với tên pod tương ứng, đường dẫn podspec nằm trong `node_modules`.

**Cho Android:**
```bash
npx expo-modules-autolinking resolve --platform android
```
Kết quả phải liệt kê package name của module (ví dụ `com.nativedevicehelper.DeviceHelperPackage`), đường dẫn `build.gradle` nằm trong `node_modules`.

## Tương thích iOS 27

iOS 27 từ chối khởi động ứng dụng chưa dùng UIKit scene life cycle, với thông báo:

```text
Application failed to launch: UIScene life cycle is required for apps built with this SDK.
```

Expo SDK 57 có sẵn lớp `ExpoAppSceneDelegate`, nhưng template prebuild chưa bật lớp này. Ứng dụng cần một config plugin để bật khi prebuild.

**Bước 1.** Tạo file `plugins/withSceneLifecycle.ts` ở gốc ứng dụng:

```ts
import { type ConfigPlugin, withAppDelegate, withInfoPlist } from "@expo/config-plugins"

const SCENE_DELEGATE_CLASS = "EXExpoAppSceneDelegate"
const CLASS_DECLARATION = "class AppDelegate: ExpoAppDelegate {"
const CLASS_DECLARATION_WITH_PROVIDER =
  "class AppDelegate: ExpoAppDelegate, ExpoReactNativeFactoryProvider {"

const WINDOW_SETUP =
  /\n#if os\(iOS\) \|\| os\(tvOS\)\n\s*window = UIWindow\(frame: UIScreen\.main\.bounds\)\n\s*factory\.startReactNative\(\n\s*withModuleName: "main",\n\s*in: window,\n\s*launchOptions: launchOptions\)\n#endif\n/

const withSceneManifest: ConfigPlugin = (config) =>
  withInfoPlist(config, (config) => {
    config.modResults.UIApplicationSceneManifest = {
      UIApplicationSupportsMultipleScenes: false,
      UISceneConfigurations: {
        UIWindowSceneSessionRoleApplication: [
          {
            UISceneConfigurationName: "Default Configuration",
            UISceneDelegateClassName: SCENE_DELEGATE_CLASS,
          },
        ],
      },
    }
    return config
  })

const withSceneAwareAppDelegate: ConfigPlugin = (config) =>
  withAppDelegate(config, (config) => {
    let contents = config.modResults.contents

    if (!contents.includes(CLASS_DECLARATION_WITH_PROVIDER)) {
      if (!contents.includes(CLASS_DECLARATION)) {
        throw new Error("withSceneLifecycle: AppDelegate class declaration not found")
      }
      contents = contents.replace(CLASS_DECLARATION, CLASS_DECLARATION_WITH_PROVIDER)
    }

    if (WINDOW_SETUP.test(contents)) {
      contents = contents.replace(WINDOW_SETUP, "")
    } else if (contents.includes("UIWindow(frame:")) {
      throw new Error("withSceneLifecycle: AppDelegate window setup has an unexpected shape")
    }

    config.modResults.contents = contents
    return config
  })

const withSceneLifecycle: ConfigPlugin = (config) =>
  withSceneAwareAppDelegate(withSceneManifest(config))

export default withSceneLifecycle
```

**Bước 2.** Đăng ký plugin trong `app.config.ts`:

```ts
plugins: [...existingPlugins, "./plugins/withSceneLifecycle"],
```

**Bước 3.** Chạy `npx expo prebuild --clean` rồi build lại.
