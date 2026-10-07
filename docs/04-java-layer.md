# Tầng native Java (Android)

[← Mục lục tài liệu](README.md)

Tài liệu mô tả cách viết phần Java của module cho Android bằng React Native Bridge.

## Hai class bắt buộc

Mỗi module Android cần hai class Java:

| Class | Vai trò |
| :--- | :--- |
| `<Tên>Module.java` | Logic native: hàm, sự kiện, constants |
| `<Tên>Package.java` | Đăng ký module với Autolinking |

## Bảng API của React Native Bridge (Android)

| Chức năng | Cơ chế Java |
| :--- | :--- |
| Tên module phía JavaScript | `getName()` trả về chuỗi |
| Giá trị cố định, đọc trực tiếp | `getConstants()` trả về `Map<String, Object>` |
| Hàm bất đồng bộ trả kết quả | `@ReactMethod void myFn(Promise promise)` |
| Hàm không cần kết quả | `@ReactMethod void myFn(String arg)` |
| Thực thi trên UI thread | `UiThreadUtil.runOnUiThread(Runnable)` |
| Phát sự kiện lên JS | `reactContext.getJSModule(RCTDeviceEventEmitter.class).emit(name, data)` |
| Bắt buộc cho NativeEventEmitter | `addListener(String)` và `removeListeners(Integer)` |
| Đóng gói dữ liệu gửi lên JS | `Arguments.createMap()` → `WritableMap` |
| Báo lỗi | `promise.reject("MÃ_LỖI", "Mô tả", exception)` |

## Cấu trúc module Android

```java
package com.nativetoast;

import androidx.annotation.NonNull;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import android.widget.Toast;

public class NativeToastModule extends ReactContextBaseJavaModule {

  // Tên này phải khớp với NativeModules.<Tên> bên TypeScript
  static final String NAME = "NativeToast";

  private final ReactApplicationContext reactContext;

  public NativeToastModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @NonNull
  @Override
  public String getName() { return NAME; }

  @ReactMethod
  public void show(String message, String type, double durationMs) {
    int length = durationMs > 2500 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
    // Mọi lời gọi Android UI phải chạy trên UI thread
    UiThreadUtil.runOnUiThread(() ->
      Toast.makeText(reactContext, message, length).show()
    );
  }
}
```

## Đăng ký module: ReactPackage

```java
package com.nativetoast;

import androidx.annotation.NonNull;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NativeToastPackage implements ReactPackage {

  @NonNull
  @Override
  public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
    List<NativeModule> modules = new ArrayList<>();
    modules.add(new NativeToastModule(reactContext));
    return modules;
  }

  @NonNull
  @Override
  public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
    return Collections.emptyList();
  }
}
```

Class `ReactPackage` là đầu mối Autolinking của Android đọc qua `expo-module.config.json`.

## Hằng số đồng bộ (getConstants)

Giá trị trả về từ `getConstants()` được đọc ngay khi module khởi tạo, không cần gọi hàm async:

```java
@Override
public Map<String, Object> getConstants() {
  Map<String, Object> constants = new HashMap<>();
  constants.put("osVersion", Build.VERSION.RELEASE);
  constants.put("model", Build.MODEL);
  return constants;
}
```

Phía TypeScript đọc: `NativeModules.DeviceHelper.osVersion`.

## Hàm bất đồng bộ qua Promise

```java
@ReactMethod
public void getHardwareInfo(Promise promise) {
  try {
    WritableMap map = Arguments.createMap();
    map.putDouble("totalRamMb", ...);
    map.putBoolean("isCharging", ...);
    promise.resolve(map);
  } catch (Exception e) {
    promise.reject("HARDWARE_ERROR", "Lỗi đọc phần cứng: " + e.getMessage(), e);
  }
}
```

Mọi nhánh của hàm phải kết thúc bằng `promise.resolve()` hoặc `promise.reject()`.

## Phát sự kiện lên JavaScript

Ba phần bắt buộc:

**1. Khai báo biến đếm listener** để tránh gửi event khi không có ai nhận:
```java
private int listenerCount = 0;

@ReactMethod
public void addListener(String eventName) { listenerCount++; }

@ReactMethod
public void removeListeners(Integer count) {
  listenerCount = Math.max(0, listenerCount - count);
}
```

**2. Phát sự kiện khi có dữ liệu:**
```java
void emitBatteryChange(int batteryLevel, boolean isCharging) {
  if (listenerCount == 0) return;
  WritableMap data = Arguments.createMap();
  data.putInt("batteryLevel", batteryLevel);
  data.putBoolean("isCharging", isCharging);
  reactContext
    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
    .emit("onBatteryChange", data);
}
```

**3. Phía TypeScript** dùng `NativeEventEmitter` để nhận:
```typescript
const emitter = new NativeEventEmitter(NativeModules.DeviceHelper)
const sub = emitter.addListener("onBatteryChange", listener)
// Cleanup:
sub.remove()
```

## Quy tắc về thread

| Công việc | Thread bắt buộc | Cơ chế |
| :--- | :--- | :--- |
| Toast, Dialog, View | UI thread | `UiThreadUtil.runOnUiThread(...)` |
| Đọc file, mạng, tính toán | Bất kỳ | Background thread mặc định của `@ReactMethod` |
| Phát sự kiện | Bất kỳ | `emit()` gọi được từ mọi thread |
| Gọi `promise.resolve/reject` | Bất kỳ | An toàn từ mọi thread |

## So sánh iOS (Expo Modules) ↔ Android (Bridge)

| Khái niệm | iOS (Swift) | Android (Java) |
| :--- | :--- | :--- |
| Tên module | `Name("NativeToast")` | `getName()` trả về `"NativeToast"` |
| Hàm | `AsyncFunction("show") { ... }` | `@ReactMethod void show(...)` |
| Constants | `Constant("model") { ... }` | `getConstants()` trả Map |
| UI thread | `.runOnQueue(.main)` | `UiThreadUtil.runOnUiThread(...)` |
| Phát sự kiện | `sendEvent("onX", data)` | `emit("onX", data)` qua RCTDeviceEventEmitter |
| Báo lỗi | `throw Exception(name:description:)` | `promise.reject("CODE", "msg", e)` |
| Đăng ký Autolinking | `expo-module.config.json → apple.modules` | `expo-module.config.json → android.modules` |
