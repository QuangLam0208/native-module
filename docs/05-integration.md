# Tích hợp vào ứng dụng

[← Mục lục tài liệu](README.md)

Tài liệu mô tả cách cài module vào một ứng dụng Expo cho iOS và Android, cách cập nhật, và yêu cầu riêng của iOS 27.

## Cài đặt

Quy trình chuẩn: **cài package → prebuild → build native**.

```bash
# Module nằm trong thư mục con của repo nên cần chỉ rõ đường dẫn bằng path:
pnpm add "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#path:/native-toast"

npx expo prebuild --clean   # Sinh lại thư mục native
npx expo run:ios
npx expo run:android
```

Lệnh trên cài từ nhánh mặc định của repo là `dev`. Để cài từ một nhánh, tag hoặc commit cụ thể, thêm tên đó vào trước `path:`:

```bash
pnpm add "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#<nhánh-tag-hoặc-commit>&path:/native-toast"
```

Máy cài đặt cần có quyền đọc repo này.

Sau khi cài, `package.json` của ứng dụng có thêm dependency:

```json
"native-alert": "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#path:/native-alert",
"native-date-picker": "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#path:/native-date-picker",
"native-device-helper": "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#path:/native-device-helper",
"native-network-status": "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#path:/native-network-status",
"native-toast": "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#path:/native-toast"
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
| 1 | Repo `react-native-module` | Sửa mã nguồn, commit và push |
| 2 | Ứng dụng | Chạy lại lệnh `pnpm add` để lấy commit mới |
| 3 | Ứng dụng | Chạy `pod install` (hoặc prebuild) rồi build lại native |
| 4 | Ứng dụng | Khởi động lại Metro |

Bước 3 là bắt buộc với mọi lần cập nhật, kể cả khi chỉ đổi mã TypeScript: đường dẫn cài đặt của module trong `node_modules/.pnpm` chứa mã commit, nên pod phải được trỏ lại sang đường dẫn mới.

## Phát triển module trước khi push

Trong lúc viết module, cài tạm từ thư mục repo trên máy để thử, chạy ổn rồi mới push và chuyển sang cài qua Git:

```bash
pnpm add "file:<đường-dẫn-tới-repo>/native-network-status"
```

Sau mỗi lần sửa mã trong repo, chạy lại lệnh trên để sao chép bản mới vào `node_modules`.

## Quy định tích hợp

| Hạng mục | Quy định |
| :--- | :--- |
| Liên kết native | Tự động qua Autolinking dựa trên `expo-module.config.json`. Không chỉnh sửa `Podfile`, `AppDelegate`, `Info.plist`. |
| Quyền hệ thống | Khai báo qua config plugin (iOS) hoặc `AndroidManifest.xml` của module (Android, gộp tự động). Năm module hiện tại không yêu cầu quyền từ người dùng; `native-network-status` thêm `ACCESS_NETWORK_STATE` tự động. |
| Môi trường chạy | Development build. Expo Go không hỗ trợ. |
| Thay đổi mã Swift hoặc Java | Bắt buộc build lại native. Reload Metro không áp dụng thay đổi. |
| Thêm file Swift mới | Chạy lại `pod install` (hoặc prebuild) trước khi build. |
| Thêm module mới trên Android | Xóa `android/build/generated/autolinking` rồi build lại, vì Expo lưu kết quả quét module theo mã băm của `package.json`. |
| Khởi động lại Metro sau khi đổi `index.ts` của module | `npx expo start --dev-client --clear` để Metro bỏ cache cũ. |
| Chỉnh sửa module | Thực hiện tại repo `react-native-module`. Không sửa trực tiếp trong `node_modules`. |

**Kiểm tra Autolinking đã nhận module:**

```bash
npx expo-modules-autolinking resolve --platform apple
```

Kết quả phải có mục của module với tên pod tương ứng, đường dẫn podspec nằm trong `node_modules`. Với Android, mở `android/build/generated/autolinking/autolinking.json` và tìm tên package của module.

## Chạy Android

```bash
npx expo run:android
```

Cần Android SDK (đặt `ANDROID_HOME`) và JDK 17. Trên thiết bị thật, mở cổng cho Metro bằng `adb reverse tcp:8081 tcp:8081`. Máy chỉ chạy kiến trúc 32 bit (ví dụ Galaxy J6+) build nhanh hơn khi giới hạn kiến trúc:

```bash
ORG_GRADLE_PROJECT_reactNativeArchitectures=armeabi-v7a npx expo run:android
```

## Tương thích iOS 27

iOS 27 từ chối khởi động ứng dụng chưa dùng UIKit scene life cycle, với thông báo:

```text
Application failed to launch: UIScene life cycle is required for apps built with this SDK.
```

Expo SDK 57 có sẵn lớp `ExpoAppSceneDelegate`, nhưng template prebuild chưa bật lớp này. Ứng dụng cần một config plugin để bật khi prebuild.

**Bước 1.** Tạo file `plugins/withSceneLifecycle.ts` ở gốc ứng dụng:

```ts
import { type ConfigPlugin, withAppDelegate, withInfoPlist } from "@expo/config-plugins"

/**
 * Adopts the UIKit scene-based life cycle on iOS.
 *
 * iOS 27 refuses to launch an app that has not adopted it ("UIScene life cycle
 * is required for apps built with this SDK"). Expo ships `ExpoAppSceneDelegate`
 * for this, but the SDK 57 prebuild template does not wire it up yet, so this
 * plugin does:
 *  - Info.plist declares a scene whose delegate is `ExpoAppSceneDelegate`.
 *  - AppDelegate hands its React Native factory to that scene delegate and no
 *    longer creates the window itself.
 *
 * Remove this plugin once the Expo template adopts the scene life cycle.
 */

// The Objective-C name of Expo's `ExpoAppSceneDelegate`.
const SCENE_DELEGATE_CLASS = "EXExpoAppSceneDelegate"

const CLASS_DECLARATION = "class AppDelegate: ExpoAppDelegate {"
const CLASS_DECLARATION_WITH_PROVIDER =
  "class AppDelegate: ExpoAppDelegate, ExpoReactNativeFactoryProvider {"

// The window is created by the scene delegate, which also starts React Native in it.
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

Plugin viết bằng TypeScript nên `app.config.ts` cần dòng `import "tsx/cjs"` ở đầu file, và ứng dụng cần có `tsx` và `@expo/config-plugins` trong `devDependencies`.

**Bước 3.** Chạy `npx expo prebuild --clean` rồi build lại.

Khi prebuild, plugin thực hiện hai việc:

| Việc | Nội dung |
| :--- | :--- |
| Sửa `Info.plist` | Khai báo `UIApplicationSceneManifest` với scene delegate là `EXExpoAppSceneDelegate` |
| Sửa `AppDelegate.swift` | Thêm `ExpoReactNativeFactoryProvider` vào khai báo class, bỏ đoạn tự tạo cửa sổ |

Plugin báo lỗi khi `AppDelegate` của template đổi dạng. Gỡ plugin khi template của Expo tự hỗ trợ scene life cycle.

**Ảnh hưởng tới native module:** trong scene life cycle, cửa sổ thuộc về scene, không thuộc về `AppDelegate`. Module cần tìm cửa sổ hoặc màn hình trên cùng phải đi qua `connectedScenes`, như các module hiện tại đang làm:

```swift
static func topViewController() -> UIViewController? {
  let keyWindow = UIApplication.shared.connectedScenes
    .compactMap { $0 as? UIWindowScene }
    .flatMap { $0.windows }
    .first { $0.isKeyWindow }

  var top = keyWindow?.rootViewController
  while let presented = top?.presentedViewController {
    top = presented
  }
  return top
}
```

**Khác biệt giao diện giữa các phiên bản iOS:** alert và sheet do hệ điều hành vẽ, nên hình dạng thay đổi theo phiên bản. Trên iOS 27, nút của alert có dạng bo tròn và ba nút xếp dọc. Module không can thiệp vào các khác biệt này.
