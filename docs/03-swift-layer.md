# Tầng native Swift

[← Mục lục tài liệu](README.md)

Tài liệu mô tả cách viết phần Swift của module bằng Expo Modules API, với mã nguồn trích từ các module trong repo.

## Bảng API của Expo Modules

| Chức năng | API |
| :--- | :--- |
| Tên module phía JavaScript | `Name("NativeToast")` |
| Giá trị cố định, đọc trực tiếp | `Constant("model") { ... }` |
| Hàm trả kết quả ngay | `AsyncFunction("name") { ... -> Kiểu in ... }` |
| Hàm trả kết quả sau, qua callback | `AsyncFunction("name") { (..., promise: Promise) in ... }` |
| Thực thi trên main thread | `.runOnQueue(.main)` gắn sau `AsyncFunction` |
| Khai báo sự kiện | `Events("onBatteryChange")` |
| Phát sự kiện | `sendEvent("onBatteryChange", data)` |
| Biết khi có hoặc hết listener | `OnStartObserving { }`, `OnStopObserving { }` |
| Biết khi module được tạo hoặc hủy | `OnCreate { }`, `OnDestroy { }` |
| Nhận một object từ JavaScript | `struct ...: Record` với các trường `@Field` |
| Nhận một chuỗi thuộc tập giá trị cố định | `enum ...: String, Enumerable` |
| Báo lỗi về JavaScript | `throw Exception(name:description:)` hoặc `promise.reject(...)` |

## Tên dạng chuỗi

JavaScript không nhìn thấy tên hàm của Swift. Mỗi hàm, sự kiện và module được đăng ký kèm một tên dạng chuỗi, và JavaScript tra cứu theo chuỗi đó lúc ứng dụng chạy.

```swift
AsyncFunction("getStatus") { (promise: Promise) in ... }
```

Dòng trên đăng ký một closure dưới tên `getStatus`. Phía TypeScript gọi `nativeModule.getStatus()`.

Ba cặp tên phải khớp chính xác giữa hai tầng. Trình biên dịch không phát hiện được sai lệch, lỗi chỉ xuất hiện khi chạy:

| Phía Swift | Phía TypeScript |
| :--- | :--- |
| `Name("NetworkStatus")` | `requireOptionalNativeModule("NetworkStatus")` |
| `AsyncFunction("getStatus")` | `nativeModule.getStatus()` |
| `Events("onStatusChange")` | `nativeModule.addListener("onStatusChange", ...)` |

## Lấy dữ liệu

`native-device-helper` minh họa hai cách: `Constant` cho giá trị không đổi trong suốt vòng đời ứng dụng, `AsyncFunction` cho giá trị thay đổi theo thời gian.

```swift
public class DeviceHelperModule: Module {
  public func definition() -> ModuleDefinition {
    Name("DeviceHelper")

    Constant("osVersion") { DeviceInfo.osVersion }
    Constant("model") { DeviceInfo.model }

    // UIDevice is main-thread only, so the read runs on the main queue.
    AsyncFunction("getHardwareInfo") { () -> [String: Any] in
      HardwareReader.read()
    }
    .runOnQueue(.main)
  }
}
```

Hàm trả về `[String: Any]`, phía JavaScript nhận được một object thường. Giá trị không xác định được quy ước là `-1` thay vì bỏ trống, để kiểu dữ liệu phía TypeScript luôn là `number`.

## Thực thi hành động và trả kết quả qua Promise

Khi kết quả chưa có lúc thân hàm chạy xong, hàm nhận thêm tham số `promise: Promise` và gọi `promise.resolve(...)` vào lúc có kết quả. `native-alert` mở hộp thoại rồi kết thúc ngay; Promise chỉ hoàn tất khi người dùng chạm một nút.

```swift
public class NativeAlertModule: Module {
  public func definition() -> ModuleDefinition {
    Name("NativeAlert")

    // Resolves with "confirm", "neutral" or "cancel" once the user taps a button.
    // `title` and `neutralText` are optional: nil or empty leaves that part out.
    // UIKit work, so it runs on the main queue.
    AsyncFunction("show") { (title: String?, message: String, confirmText: String, neutralText: String?, cancelText: String, promise: Promise) in
      guard let presenter = AlertPresenter.topViewController() else {
        promise.reject(Exception(name: "NO_VIEW_CONTROLLER", description: "No view controller to present the alert on"))
        return
      }
      AlertPresenter.present(
        title: title, message: message, confirmText: confirmText, neutralText: neutralText,
        cancelText: cancelText, from: presenter
      ) { button in
        promise.resolve(button.rawValue)
      }
    }
    .runOnQueue(.main)
  }
}
```

So sánh hai cách viết `AsyncFunction`:

| Cách viết | Khi nào dùng | Ví dụ |
| :--- | :--- | :--- |
| Trả kết quả bằng giá trị của closure | Tính xong ngay trong thân hàm | `DeviceHelper.getHardwareInfo` |
| Nhận tham số `promise: Promise` | Kết quả đến sau, qua một callback | `NativeAlert.show`, `NativeDatePicker.show`, `NetworkStatus.getStatus` |

Phía JavaScript không phân biệt được hai cách này: cả hai đều cho ra một Promise.

## Nhận tham số có cấu trúc bằng Record

Khi hàm có nhiều tùy chọn, khai báo một `struct` tuân theo `Record`. Expo tự chuyển object từ JavaScript thành struct, theo tên trường. `native-date-picker` dùng cách này:

```swift
/// The options object JS passes to `show` and `showRange`. Field names match the JS keys.
/// Dates cross the bridge as milliseconds since 1970, the same unit as JS `Date.getTime()`.
struct DatePickerOptions: Record {
  @Field var mode: DatePickerMode = .date
  /// The selected date, or the start of the range.
  @Field var value: Double?
  /// The end of the range. Only read by `showRange`.
  @Field var endValue: Double?
  @Field var minimumDate: Double?
  @Field var maximumDate: Double?
  /// Step of the minute wheel. Must divide 60.
  @Field var minuteInterval: Int = 1
  @Field var theme: DatePickerTheme = .system
  @Field var title: String?
  @Field var startLabel: String = "From"
  @Field var endLabel: String = "To"
  @Field var confirmText: String = "Done"
  @Field var cancelText: String = "Cancel"
}
```

```swift
AsyncFunction("show") { (options: DatePickerOptions, promise: Promise) in
  Self.present(options, isRange: false, promise: promise) { selection in
    selection.start.milliseconds
  }
}
.runOnQueue(.main)
```

**Quy ước:**
- Trường không bắt buộc khai báo kiểu optional (`Double?`) hoặc có giá trị mặc định.
- Ngày giờ truyền qua lại dưới dạng mili giây kể từ năm 1970, cùng đơn vị với `Date.getTime()` của JavaScript. Tầng TypeScript chịu trách nhiệm đổi giữa `Date` và số.
- Dùng tham số rời khi hàm có ít tham số (`NativeToast.show`), dùng `Record` khi có nhiều tùy chọn (`NativeDatePicker.show`).

## Nhận giá trị thuộc tập cố định bằng Enumerable

Enum kế thừa `String` và `Enumerable` được Expo tự chuyển từ chuỗi JavaScript. Chuỗi không hợp lệ phát sinh lỗi ở tầng JavaScript trước khi vào thân hàm.

```swift
/// Mirrors the app's JS toast types (app/utils/Toast.ts) and its Toasty-style palette.
enum ToastType: String, Enumerable {
  case normal
  case success
  case warning
  case error

  var backgroundColor: UIColor {
    switch self {
    case .normal: return UIColor(red: 0x35 / 255, green: 0x3A / 255, blue: 0x3E / 255, alpha: 1)
    case .success: return UIColor(red: 0x38 / 255, green: 0x8E / 255, blue: 0x3C / 255, alpha: 1)
    case .warning: return UIColor(red: 0xFF / 255, green: 0xA9 / 255, blue: 0x00 / 255, alpha: 1)
    case .error: return UIColor(red: 0xD5 / 255, green: 0x00 / 255, blue: 0x00 / 255, alpha: 1)
    }
  }
}
```

Enum dùng được cho cả tham số rời (`type: ToastType`) và trường của `Record` (`@Field var mode: DatePickerMode`).

## Phát sự kiện

Sự kiện gồm ba phần: khai báo tên, bắt đầu và dừng theo dõi, phát dữ liệu. `native-device-helper` chỉ theo dõi pin trong lúc có listener:

```swift
public class DeviceHelperModule: Module {
  private static let batteryChangeEvent = "onBatteryChange"

  private var batteryObservers: [NSObjectProtocol] = []

  public func definition() -> ModuleDefinition {
    Name("DeviceHelper")

    Events(Self.batteryChangeEvent)

    // Battery notifications are only posted while a JS listener is attached.
    OnStartObserving {
      DispatchQueue.main.async { self.startObservingBattery() }
    }

    OnStopObserving {
      DispatchQueue.main.async { self.stopObservingBattery() }
    }
  }

  private func startObservingBattery() {
    guard batteryObservers.isEmpty else { return }
    UIDevice.current.isBatteryMonitoringEnabled = true

    let names = [UIDevice.batteryLevelDidChangeNotification, UIDevice.batteryStateDidChangeNotification]
    batteryObservers = names.map { name in
      NotificationCenter.default.addObserver(forName: name, object: nil, queue: .main) { [weak self] _ in
        self?.sendEvent(Self.batteryChangeEvent, HardwareReader.battery())
      }
    }
  }

  private func stopObservingBattery() {
    batteryObservers.forEach(NotificationCenter.default.removeObserver)
    batteryObservers = []
  }
}
```

**Luồng của một sự kiện:**

| Bước | Diễn biến |
| :--- | :--- |
| 1 | Ứng dụng gọi `addBatteryListener(fn)` |
| 2 | Expo gọi `OnStartObserving` vì có listener đầu tiên; Swift bắt đầu theo dõi |
| 3 | Hệ điều hành báo trạng thái pin thay đổi |
| 4 | Swift gọi `sendEvent` với tên sự kiện và dữ liệu |
| 5 | Expo chuyển dữ liệu sang JS thread và gọi `fn` |
| 6 | Bước 3 đến 5 lặp lại mỗi lần có thay đổi |
| 7 | Ứng dụng hủy đăng ký; Expo gọi `OnStopObserving` khi hết listener; Swift dừng theo dõi |

`native-network-status` chọn cách khác: theo dõi mạng suốt vòng đời module bằng `OnCreate` và `OnDestroy`, vì việc theo dõi là thụ động và giúp `getStatus()` trả kết quả ngay.

```swift
public class NetworkStatusModule: Module {
  private static let statusChangeEvent = "onStatusChange"

  private let monitor = NetworkMonitor()

  public func definition() -> ModuleDefinition {
    Name("NetworkStatus")

    Events(Self.statusChangeEvent)

    OnCreate {
      monitor.onChange = { [weak self] status in
        self?.sendEvent(Self.statusChangeEvent, status)
      }
      monitor.start()
    }

    OnDestroy {
      monitor.stop()
    }

    // Resolves with the current status. Waits for the system's first report if it has not arrived yet.
    AsyncFunction("getStatus") { (promise: Promise) in
      monitor.currentStatus { status in
        promise.resolve(status)
      }
    }
  }
}
```

| Cách theo dõi | Khi nào dùng | Module |
| :--- | :--- | :--- |
| `OnStartObserving` / `OnStopObserving` | Việc theo dõi tốn tài nguyên, chỉ nên chạy khi có listener | `native-device-helper` |
| `OnCreate` / `OnDestroy` | Việc theo dõi thụ động, và hàm đọc cần sẵn dữ liệu | `native-network-status` |

## Luồng thực thi

| Khai báo | Chạy trên | Module đang dùng |
| :--- | :--- | :--- |
| `AsyncFunction` | Background thread (mặc định của Expo) | `NetworkStatus.getStatus` |
| `AsyncFunction(...).runOnQueue(.main)` | Main thread | `NativeToast.show`, `NativeAlert.show`, `NativeDatePicker.show`, `DeviceHelper.getHardwareInfo` |
| `Function` (đồng bộ) | JS thread, chặn JavaScript cho tới khi xong | Chưa dùng |

**Quy định:**
- Mọi lời gọi UIKit (`UIView`, `UIViewController`, `UIAlertController`, `UIDevice`) phải chạy trên main thread.
- `.runOnQueue(.main)` chỉ khả dụng trên `AsyncFunction`, không dùng được với `Function`.
- Callback của hệ điều hành có thể chạy trên thread bất kỳ. `sendEvent` và `promise.resolve` gọi được từ mọi thread.

## Báo lỗi

| Cách viết hàm | Cách báo lỗi |
| :--- | :--- |
| Trả kết quả bằng giá trị của closure | `throw Exception(name: "MÃ_LỖI", description: "...")` |
| Nhận tham số `promise` | `promise.reject(Exception(name: "MÃ_LỖI", description: "..."))` rồi `return` |

Cả hai cách đều làm Promise phía JavaScript bị reject kèm mã lỗi.

```swift
AsyncFunction("show") { (message: String, type: ToastType, durationMs: Double) in
  guard durationMs > 0 else {
    throw Exception(name: "INVALID_DURATION", description: "durationMs must be greater than 0")
  }
  guard let window = Self.keyWindow() else {
    throw Exception(name: "NO_WINDOW", description: "No key window to present the toast on")
  }
  ToastView.present(message: message, type: type, duration: durationMs / 1000, in: window)
}
.runOnQueue(.main)
```

Kiểm tra tham số ở đầu hàm và báo lỗi bằng mã cụ thể, thay vì để hệ điều hành âm thầm bỏ qua giá trị không hợp lệ.
