# Tầng TypeScript

[← Mục lục tài liệu](README.md)

Tài liệu mô tả khuôn mẫu chung của file `index.ts` và các quy ước mà mọi module tuân theo.

`index.ts` là API công khai của module, dùng chung cho cả iOS và Android.

## Khuôn mẫu chung — module chỉ iOS (Expo Modules API)

Dùng cho module chưa có phần Android:

```typescript
import { requireOptionalNativeModule } from "expo"

const nativeModule = requireOptionalNativeModule<NativeToastModule>("NativeToast")

export const NativeToast = {
  isAvailable: nativeModule != null,
  show(...): Promise<void> {
    return nativeModule?.show(...) ?? Promise.resolve()
  },
} as const
```

## Khuôn mẫu chung — module hỗ trợ cả iOS + Android

Khi module có phần Android, `index.ts` kết hợp hai cơ chế khác nhau phía sau cùng một API:

```typescript
import { requireOptionalNativeModule } from "expo"
import { NativeModules, NativeEventEmitter, Platform } from "react-native"

// iOS: Expo Modules API
const nativeModuleIOS = Platform.OS === "ios"
  ? requireOptionalNativeModule<MyModule>("MyModule")
  : null

// Android: ReactContextBaseJavaModule — getName() phải trả cùng chuỗi "MyModule"
const nativeModuleAndroid = Platform.OS === "android"
  ? (NativeModules.MyModule ?? null)
  : null

// Chọn module theo nền tảng
const nativeModule = (nativeModuleIOS ?? nativeModuleAndroid) as MyModule | null

export const MyAPI = {
  isAvailable: nativeModule != null,
  doSomething(): Promise<void> {
    return nativeModule?.doSomething() ?? Promise.resolve()
  },
} as const
```

> **Ràng buộc quan trọng:** `getName()` bên Java **phải trả về cùng chuỗi** với `Name(...)` bên Swift
> và cùng key của `NativeModules.<key>`. Tất cả phải là `"NativeToast"`, không phải `"NativeToastModule"`.

## Quy ước của tầng TypeScript

| Quy ước | Nội dung | Lý do |
| :--- | :--- | :--- |
| Một đối tượng cho mỗi module | Toàn bộ API nằm trong một đối tượng (`NativeToast`) | Phía gọi chỉ import một tên |
| Hằng số thay cho chuỗi | `NativeToast.Type.Success` thay cho `"success"` | Được gợi ý khi gõ, tránh sai chính tả |
| Object `as const` thay cho `enum` | Giá trị truyền xuống native là chuỗi thường | Khớp với `Enumerable` phía Swift và String phía Java |
| `requireOptionalNativeModule` | Trả về `null` khi bản build không có module (iOS) | Ứng dụng không dừng khi module không khả dụng |
| `NativeModules.<Tên> ?? null` | Trả về `null` khi module không được link (Android) | Ứng dụng không dừng khi module không khả dụng |
| `isAvailable` | `true` khi module native có trong bản build | Để ẩn hoặc vô hiệu hóa giao diện liên quan |
| Giá trị dự phòng khi không khả dụng | Hàm trả `null` hoặc không làm gì, không gọi callback | Phía gọi không cần bọc `try/catch` cho trường hợp này |
| Đổi kiểu dữ liệu tại đây | `Date` thành mili giây và ngược lại | Tầng native chỉ làm việc với kiểu đơn giản |

## Xử lý sự kiện có NativeEventEmitter (Android)

`addListener` của Expo (iOS) trả về `{ remove() }`. Với Android, dùng `NativeEventEmitter`:

```typescript
let eventEmitterAndroid: NativeEventEmitter | null = null
if (nativeModuleAndroid) {
  eventEmitterAndroid = new NativeEventEmitter(nativeModuleAndroid)
}

addBatteryListener(listener: (info: BatteryInfo) => void): () => void {
  if (isIOS && nativeModuleIOS) {
    const sub = nativeModuleIOS.addListener("onBatteryChange", listener)
    return () => sub.remove()
  }
  if (isAndroid && eventEmitterAndroid) {
    const sub = eventEmitterAndroid.addListener("onBatteryChange", (e: any) => listener(e))
    return () => sub.remove()
  }
  return () => {}
},
```

Cả hai nền tảng trả về cùng `() => void` để màn hình gọi trong `useEffect` cleanup.

## Callback kết hợp Promise (iOS)

Hàm native chỉ trả về một giá trị. Tầng TypeScript dựa vào giá trị đó để gọi callback tương ứng:

```typescript
async show(options: NativeAlertOptions): Promise<NativeAlertButton | null> {
  if (!nativeModule) return null
  const button = await nativeModule.show(...)
  if (button === Button.Confirm) options.onConfirm?.()
  else if (button === Button.Neutral) options.onNeutral?.()
  else options.onCancel?.()
  return button
},
```
