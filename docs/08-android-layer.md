# Tầng native Android

[← Mục lục tài liệu](README.md)

Tài liệu mô tả cách viết phần Android của module bằng Java, theo cách của React Native module truyền thống. Phần Android không dùng Expo Modules API: iOS và Android là hai cách triển khai khác nhau, nối với nhau bằng cùng một tên module và cùng tên hàm.

## Vì sao khác phía iOS

| Hạng mục | iOS | Android |
| :--- | :--- | :--- |
| Ngôn ngữ | Swift | Java |
| Cách khai báo | `ModuleDefinition` của Expo Modules API | `ReactContextBaseJavaModule` và `@ReactMethod` |
| Đăng ký module | `expo-module.config.json` (`apple.modules`) | Class `ReactPackage`, Autolinking của React Native tìm qua `react-native.config.js` |
| Phía TypeScript lấy module | `requireOptionalNativeModule` | `NativeModules.<Tên>` |
| Sự kiện | `sendEvent` | `DeviceEventManagerModule.RCTDeviceEventEmitter` |

Phía TypeScript tách theo `Platform.OS` và trả về cùng một API (`isAvailable`, tên hàm, kiểu dữ liệu), nên màn hình của ứng dụng không phải phân biệt nền tảng.

## Cấu trúc thư mục `android/`

```text
android/
├── build.gradle                          Khai báo thư viện Android, namespace, SDK
└── src/main/
    ├── AndroidManifest.xml               Quyền hệ thống (nếu cần), được gộp tự động vào ứng dụng
    ├── res/                              Tài nguyên riêng của module (nếu cần)
    └── java/com/<tên>/
        ├── <Tên>Module.java              Khai báo module và các hàm @ReactMethod
        └── <Tên>Package.java             ReactPackage đăng ký module
```

`react-native.config.js` ở gốc module báo cho Autolinking của React Native biết module chỉ có Android:

```js
module.exports = {
  dependency: {
    platforms: {
      ios: null,
    },
  },
};
```

`package.json` phải liệt kê `android` và `react-native.config.js` trong `"files"`. `expo-module.config.json` giữ nguyên `"platforms": ["apple"]`, vì Expo Modules chỉ quản lý phần iOS.

## Module và `@ReactMethod`

```java
public class NativeAlertModule extends ReactContextBaseJavaModule {

  static final String NAME = "NativeAlert";

  @NonNull
  @Override
  public String getName() {
    return NAME;   // Cùng tên với Name("NativeAlert") phía iOS
  }

  @ReactMethod
  public void show(@Nullable String title, String message, String confirmText,
                   @Nullable String neutralText, String cancelText, Promise promise) {
    ...
  }
}
```

| Quy tắc | Nội dung |
| :--- | :--- |
| Tên module | `getName()` trùng tên module phía iOS để `index.ts` dùng chung tên |
| Tên và thứ tự tham số | Trùng với phía iOS; tầng TypeScript gọi hai nền tảng bằng cùng một danh sách tham số |
| Hàm có kết quả | Tham số cuối là `Promise`, gọi `resolve` hoặc `reject` ở mọi nhánh |
| Kiểu số | Mili giây truyền dưới dạng `double`; trả về bằng `(double) millis` |
| Tham số tùy chọn | `ReadableMap` kèm kiểm tra `hasKey` và `isNull` |

## Luồng thực thi

React Native gọi `@ReactMethod` trên một thread nền, không phải UI thread. Lời gọi cần giao diện phải chuyển sang UI thread:

```java
UiThreadUtil.runOnUiThread(() -> {
  Activity activity = getCurrentActivity();
  if (activity == null || activity.isFinishing()) {
    promise.reject("NO_ACTIVITY", "Activity hiện tại không khả dụng.");
    return;
  }
  // Dựng và hiển thị dialog tại đây
});
```

Mã `NO_ACTIVITY` là mã lỗi chung của Android khi chưa có Activity để hiển thị, tương ứng với `NO_VIEW_CONTROLLER` phía iOS.

## Hộp thoại phải kết thúc đúng một lần

Dialog có nhiều đường đóng: nút xác nhận, nút hủy, chạm ra ngoài, nút Back. Dùng một cờ để chỉ hoàn tất Promise một lần và coi mọi đường đóng không xác nhận là hủy:

```java
final boolean[] done = {false};
dialog.setOnCancelListener(d -> {
  if (done[0]) return;
  done[0] = true;
  promise.resolve(null);
});
```

## Sự kiện

Module phát sự kiện bằng `RCTDeviceEventEmitter`. Phía JavaScript dùng `NativeEventEmitter`, vì vậy module phải có hai hàm rỗng `addListener` và `removeListeners`.

Chỉ đăng ký nguồn sự kiện của hệ điều hành khi có listener đầu tiên, và gỡ khi hết listener:

```java
private int listenerCount = 0;

@ReactMethod
public void addListener(String eventName) {
  if (listenerCount++ == 0) startObserving();
}

@ReactMethod
public void removeListeners(double count) {
  listenerCount = Math.max(0, listenerCount - (int) count);
  if (listenerCount == 0) stopObserving();
}

@Override
public void invalidate() {
  stopObserving();   // Gỡ khi React Native bị hủy
}
```

Chỉ phát sự kiện khi dữ liệu thực sự đổi so với lần trước, để khớp hành vi phía iOS. Đã áp dụng trong `native-device-helper` (`BroadcastReceiver` của pin) và `native-network-status` (`registerDefaultNetworkCallback`).

## Thư viện và tài nguyên

| Nhu cầu | Cách làm |
| :--- | :--- |
| Thư viện bên thứ ba | Khai báo trong `android/build.gradle`. Thư viện ở JitPack cần thêm `maven { url "https://jitpack.io" }` vào `repositories` (ví dụ Toasty của `native-toast`) |
| Quyền hệ thống | Khai báo trong `android/src/main/AndroidManifest.xml` của module, được gộp tự động vào ứng dụng (ví dụ `ACCESS_NETWORK_STATE`) |
| Theme riêng | Đặt trong `android/src/main/res/values/styles.xml` của module, không sửa theme của ứng dụng (ví dụ `NativeDatePicker.Calendar`) |

Không cần sửa `MainApplication`, `AndroidManifest.xml` hay `settings.gradle` của ứng dụng: Autolinking tự thêm module khi build.

## Khác biệt giữa hai nền tảng cần ghi vào README

Mỗi README của module có bảng khác biệt iOS và Android. Những điểm thường gặp:

- Giao diện của hệ điều hành khác nhau (sheet bánh xe của iOS, dialog của Android).
- Một tùy chọn chỉ có tác dụng trên một nền tảng (`theme` và `minuteInterval` của `native-date-picker` chỉ có tác dụng trên iOS).
- Một giá trị có nghĩa khác nhau (`availableRamMb` của `native-device-helper`, `isConstrained` của `native-network-status`).
