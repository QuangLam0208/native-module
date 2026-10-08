# Tầng TypeScript

[← Mục lục tài liệu](README.md)

Tài liệu mô tả khuôn mẫu chung của file `index.ts` và các quy ước mà mọi module tuân theo.

`index.ts` là API công khai của module. Mọi module tuân theo cùng một khuôn mẫu.

## Khuôn mẫu chung

```typescript
import { requireOptionalNativeModule } from "expo"
import { NativeModules, Platform } from "react-native"

/** Toast types, exposed as `NativeToast.Type`. */
const Type = {
  Normal: "normal",
  Success: "success",
  Warning: "warning",
  Error: "error",
} as const

/** Preset durations in milliseconds, exposed as `NativeToast.Duration`. */
const Duration = {
  Short: 2200,
  Long: 3500,
} as const

export type NativeToastType = (typeof Type)[keyof typeof Type]

interface NativeToastModule {
  show(message: string, type: NativeToastType, durationMs: number): Promise<void>
}

// iOS: Expo module. Android: React Native module with the same name. Elsewhere the module is absent.
const nativeModule: NativeToastModule | null = Platform.OS === "ios"
  ? requireOptionalNativeModule<NativeToastModule>("NativeToast")
  : (NativeModules.NativeToast ?? null)

export const NativeToast = {
  Type,
  Duration,
  /** True when the native module is linked into the running build. */
  isAvailable: nativeModule != null,
  /** `durationMs` takes a `NativeToast.Duration` preset or any positive number of milliseconds. */
  show(
    message: string,
    type: NativeToastType = Type.Normal,
    durationMs: number = Duration.Short,
  ): Promise<void> {
    return nativeModule?.show(message, type, durationMs) ?? Promise.resolve()
  },
} as const
```

## Quy ước của tầng TypeScript

| Quy ước | Nội dung | Lý do |
| :--- | :--- | :--- |
| Một đối tượng cho mỗi module | Toàn bộ API nằm trong một đối tượng (`NativeToast`) | Phía gọi chỉ import một tên |
| Hằng số thay cho chuỗi | `NativeToast.Type.Success` thay cho `"success"` | Được gợi ý khi gõ, tránh sai chính tả |
| Object `as const` thay cho `enum` | Giá trị truyền xuống native là chuỗi thường | Khớp với `Enumerable` phía Swift |
| `requireOptionalNativeModule` (iOS), `NativeModules.<Tên>` (Android) | Trả về `null` khi bản build không có module | Ứng dụng không dừng khi module không khả dụng |
| Tách nền tảng bằng `Platform.OS` | Cùng một API công khai cho cả hai nền tảng | Màn hình của ứng dụng không phân biệt nền tảng |
| `isAvailable` | `true` khi module native có trong bản build | Để ẩn hoặc vô hiệu hóa giao diện liên quan |
| Giá trị dự phòng khi không khả dụng | Hàm trả `null` hoặc không làm gì, không gọi callback | Phía gọi không cần bọc `try/catch` cho trường hợp này |
| Đổi kiểu dữ liệu tại đây | `Date` thành mili giây và ngược lại | Tầng native chỉ làm việc với kiểu đơn giản |

## Callback kết hợp Promise

Hàm native chỉ trả về một giá trị. Tầng TypeScript dựa vào giá trị đó để gọi callback tương ứng, nhờ vậy phía gọi dùng được cả hai cách.

```typescript
async show(options: NativeAlertOptions): Promise<NativeAlertButton | null> {
  if (!nativeModule) return null

  const { title, message, confirmText, neutralText, cancelText } = options
  const button = await nativeModule.show(
    title ?? null,
    message,
    confirmText,
    neutralText ?? null,
    cancelText,
  )
  if (button === Button.Confirm) options.onConfirm?.()
  else if (button === Button.Neutral) options.onNeutral?.()
  else options.onCancel?.()
  return button
},
```

`onConfirm`, `onNeutral` và `onCancel` là hàm JavaScript thông thường do tầng TypeScript gọi. Phía native (Swift và Java) không phát sự kiện nào cho các callback này.

## Bọc sự kiện thành hàm hủy đăng ký

`addListener` của Expo trả về một đối tượng có hàm `remove()`. Tầng TypeScript đổi thành một hàm hủy đăng ký, khớp với cách dùng của `useEffect`.

```typescript
addBatteryListener(listener: (info: BatteryInfo) => void): () => void {
  const subscription = nativeModule?.addListener("onBatteryChange", listener)
  return () => subscription?.remove()
},
```

```tsx
useEffect(() => DeviceHelper.addBatteryListener(setBattery), [])
```

Trên Android, sự kiện đi qua `NativeEventEmitter` bọc quanh module (`new NativeEventEmitter(NativeModules.DeviceHelper)`), cũng trả về `subscription.remove()`. Listener phải được hủy khi màn hình đóng. Nếu không, native tiếp tục gọi vào một màn hình không còn tồn tại.
