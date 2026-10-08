# native-date-picker

Native module chọn ngày và giờ trên iOS (Swift, Expo Modules API) và Android (Java, React Native module).

Picker hiển thị dạng bánh xe trong một sheet trượt lên từ đáy màn hình. Module hỗ trợ chọn một giá trị hoặc một khoảng (từ – đến), với ba chế độ: ngày, giờ, ngày và giờ.

## Thông tin chung

| Hạng mục | Giá trị |
| :--- | :--- |
| Nền tảng | iOS 15.1 trở lên, Android |
| Ngôn ngữ | Swift (iOS), Java (Android) |
| Framework | Expo Modules API |
| Đã kiểm thử với | Expo SDK 57, React Native 0.86, simulator iOS 27.0, Samsung Galaxy J6+ (Android 10) |
| Môi trường chạy | Development build hoặc bản release. Expo Go không hỗ trợ. |
| Quyền hệ thống | Không yêu cầu |

Trên nền tảng khác, module không tồn tại: `NativeDatePicker.isAvailable` là `false`, `show` và `showRange` không hiển thị gì, trả về `null` và không gọi callback nào.

**Phạm vi đã kiểm thử trên Android:** chọn một ngày, khoảng ngày (một dialog Material, xác nhận và hủy). Các chế độ giờ, ngày giờ, hủy, `minimumDate` và `maximumDate` chưa kiểm thử trên thiết bị.

**Phạm vi đã kiểm thử trên iOS:** sáu trường hợp chọn (một giá trị và khoảng, cho ba chế độ), bước nhảy phút, theme tối, nút hủy, và ràng buộc điểm kết thúc không nhỏ hơn điểm bắt đầu. Chưa kiểm thử: `minimumDate`, `maximumDate`, theme sáng, vuốt sheet để hủy, iPad, và các phiên bản iOS thấp hơn 27.

## Các trường hợp hỗ trợ

| Chế độ | Một giá trị (`show`) | Khoảng (`showRange`) |
| :--- | :--- | :--- |
| `Mode.Date` | Một ngày | Từ ngày – đến ngày |
| `Mode.Time` | Một giờ | Từ giờ – đến giờ, trong cùng một ngày |
| `Mode.DateTime` | Một ngày giờ | Từ ngày giờ – đến ngày giờ |

## Cấu trúc

```text
native-date-picker/
├── expo-module.config.json     Khai báo module cho Autolinking
├── package.json
├── index.ts                    API TypeScript
├── react-native.config.js      Cấu hình Autolinking cho Android
├── android/                    Phần Android (Java): `NativeDatePickerModule.java`, `NativeDatePickerPackage.java`, theme trong `res/values/styles.xml`
└── ios/
    ├── NativeDatePicker.podspec
    ├── NativeDatePickerModule.swift   Khai báo module, hàm `show` và `showRange`
    ├── DatePickerOptions.swift        Kiểu dữ liệu nhận từ JS: chế độ, theme, tùy chọn
    └── DatePickerSheet.swift          Giao diện sheet và logic chọn khoảng
```

## Cài đặt

Ứng dụng phải dùng Expo Prebuild. Module được liên kết tự động qua Autolinking, không cần chỉnh sửa `Podfile`, `AppDelegate` hay `Info.plist`.

### Cách 1: Cài như một dependency qua Git (khuyến nghị)

Module nằm trong thư mục con `native-date-picker` của repo `react-native-module`, nên cần chỉ rõ đường dẫn bằng `path:`:

```bash
pnpm add "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#path:/native-date-picker"
```

Sau đó sinh lại thư mục native và build:

```bash
npx expo prebuild --clean
```

```bash
npx expo run:ios
```

Import theo tên package:

```ts
import { NativeDatePicker } from "native-date-picker"
```

**Cập nhật lên phiên bản mới:** chạy lại lệnh cài để pnpm lấy commit mới nhất, sau đó chạy `pod install` trong thư mục `ios/` của ứng dụng (hoặc prebuild lại) và build lại native. Nếu Metro đang chạy, khởi động lại Metro sau khi cài.

### Cách 2: Module cục bộ trong ứng dụng

Dùng khi cần chỉnh sửa mã nguồn module ngay trong ứng dụng. Clone repo rồi sao chép thư mục `native-date-picker` vào `modules/` ở gốc ứng dụng:

```bash
git clone https://git.itzsolution.com/scm/tsa/react-native-module.git
```

```bash
cp -R react-native-module/native-date-picker <thư-mục-ứng-dụng>/modules/native-date-picker
```

Sau đó prebuild và build như Cách 1. Import bằng đường dẫn tương đối:

```ts
import { NativeDatePicker } from "../modules/native-date-picker"
```

> Cách 2 đã được kiểm thử trên `react-native-base`. Cách 1 đã được kiểm thử với pnpm 11.13.0 khi module còn nằm ở một repo GitHub; lệnh cài từ repo này chưa được kiểm thử. Không dùng đồng thời hai cách trong cùng một ứng dụng.

## Sử dụng

### Chọn một ngày

```tsx
import { NativeDatePicker } from "native-date-picker"

NativeDatePicker.show({
  value: new Date(),
  title: "Ngày sinh",
  confirmText: "Xong",
  cancelText: "Hủy",
  onConfirm: (date) => setBirthday(date),
  onCancel: () => console.log("Đã hủy"),
})
```

### Chọn giờ hoặc ngày giờ

```tsx
NativeDatePicker.show({
  mode: NativeDatePicker.Mode.Time,
  minuteInterval: 15,
  confirmText: "Xong",
  cancelText: "Hủy",
  onConfirm: (date) => setReminderTime(date),
})

NativeDatePicker.show({
  mode: NativeDatePicker.Mode.DateTime,
  minimumDate: new Date(),
  confirmText: "Xong",
  cancelText: "Hủy",
  onConfirm: (date) => setAppointment(date),
})
```

### Chọn một khoảng

`showRange` dùng được với cả ba chế độ.

```tsx
NativeDatePicker.showRange({
  mode: NativeDatePicker.Mode.Date,
  startValue: filter.from,
  endValue: filter.to,
  startLabel: "Từ",
  endLabel: "Đến",
  title: "Khoảng ngày",
  confirmText: "Xong",
  cancelText: "Hủy",
  onConfirm: ({ start, end }) => setFilter({ from: start, to: end }),
})
```

### Theo theme của ứng dụng

Mặc định sheet theo chế độ sáng hoặc tối của thiết bị. Ứng dụng có công tắc theme riêng thì truyền `theme` để sheet khớp với ứng dụng:

```tsx
NativeDatePicker.show({
  theme: isDark ? NativeDatePicker.Theme.Dark : NativeDatePicker.Theme.Light,
  confirmText: "Xong",
  cancelText: "Hủy",
})
```

### Dùng Promise thay cho callback

```tsx
const date = await NativeDatePicker.show({ confirmText: "Xong", cancelText: "Hủy" })
if (date) {
  setBirthday(date)
}

const range = await NativeDatePicker.showRange({
  startLabel: "Từ",
  endLabel: "Đến",
  confirmText: "Xong",
  cancelText: "Hủy",
})
if (range) {
  console.log(range.start, range.end)
}
```

Callback và Promise dùng đồng thời được. Callback được gọi trước khi Promise hoàn tất.

## API

### Tùy chọn chung của `show` và `showRange`

| Tham số | Kiểu | Mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `mode` | `NativeDatePickerMode` | `Mode.Date` | Chế độ chọn |
| `minimumDate` | `Date` | Không giới hạn | Giá trị nhỏ nhất được chọn |
| `maximumDate` | `Date` | Không giới hạn | Giá trị lớn nhất được chọn |
| `minuteInterval` | `number` | `1` | Bước nhảy của bánh xe phút. Phải chia hết 60 và không quá 30 |
| `theme` | `NativeDatePickerTheme` | `Theme.System` | Giao diện sáng hoặc tối của sheet |
| `title` | `string` | Không có | Tiêu đề nằm giữa hai nút |
| `confirmText` | `string` | Bắt buộc | Chữ trên nút xác nhận |
| `cancelText` | `string` | Bắt buộc | Chữ trên nút hủy |
| `onCancel` | `() => void` | Không có | Gọi khi người dùng chạm hủy hoặc vuốt sheet xuống |

### `NativeDatePicker.show(options)`

Chọn một giá trị. Ngoài các tùy chọn chung:

| Tham số | Kiểu | Mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `value` | `Date` | Thời điểm hiện tại | Giá trị đang chọn khi mở |
| `onConfirm` | `(date: Date) => void` | Không có | Gọi với giá trị người dùng xác nhận |

Trả về `Promise<Date | null>`: giá trị đã xác nhận, hoặc `null` khi hủy hay khi module không khả dụng.

### `NativeDatePicker.showRange(options)`

Chọn một khoảng. Ngoài các tùy chọn chung:

| Tham số | Kiểu | Mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `startValue` | `Date` | Thời điểm hiện tại | Điểm bắt đầu khi mở |
| `endValue` | `Date` | Bằng `startValue` | Điểm kết thúc khi mở |
| `startLabel` | `string` | Bắt buộc | Nhãn của nút sửa điểm bắt đầu, ví dụ "Từ" |
| `endLabel` | `string` | Bắt buộc | Nhãn của nút sửa điểm kết thúc, ví dụ "Đến" |
| `onConfirm` | `(range: NativeDateRange) => void` | Không có | Gọi với khoảng người dùng xác nhận |

Trả về `Promise<NativeDateRange | null>`: đối tượng `{ start, end }`, hoặc `null` khi hủy hay khi module không khả dụng. `end` không bao giờ nhỏ hơn `start`.

### `NativeDatePicker.isAvailable`

`boolean`. `true` khi module native đã được liên kết vào bản build đang chạy.

### Hằng số

| Hằng số | Giá trị | Ý nghĩa |
| :--- | :--- | :--- |
| `NativeDatePicker.Mode.Date` | `"date"` | Chọn ngày |
| `NativeDatePicker.Mode.Time` | `"time"` | Chọn giờ |
| `NativeDatePicker.Mode.DateTime` | `"datetime"` | Chọn ngày và giờ |
| `NativeDatePicker.Theme.System` | `"system"` | Theo thiết bị |
| `NativeDatePicker.Theme.Light` | `"light"` | Luôn sáng |
| `NativeDatePicker.Theme.Dark` | `"dark"` | Luôn tối |

### Kiểu dữ liệu

```ts
import type {
  NativeDatePickerMode,
  NativeDatePickerOptions,
  NativeDatePickerTheme,
  NativeDateRange,
  NativeDateRangePickerOptions,
} from "native-date-picker"
```

### Mã lỗi

Promise bị reject trong các trường hợp sau:

| Mã lỗi | Trường hợp |
| :--- | :--- |
| `INVALID_RANGE` | `minimumDate` lớn hơn `maximumDate` |
| `INVALID_MINUTE_INTERVAL` | `minuteInterval` không chia hết 60 hoặc lớn hơn 30 |
| `NO_VIEW_CONTROLLER` | (iOS) Ứng dụng chưa có màn hình nào để hiển thị picker |
| `NO_ACTIVITY` | (Android) Chưa có Activity để hiển thị dialog |

## Khác biệt giữa iOS và Android

| Hạng mục | iOS | Android |
| :--- | :--- | :--- |
| Giao diện | Bánh xe trong sheet trượt từ đáy | `DatePickerDialog` và `TimePickerDialog` của hệ thống |
| `theme` | Áp dụng | Bị bỏ qua, dialog theo theme của ứng dụng |
| `minuteInterval` | Đổi bước nhảy bánh xe phút | Chỉ được kiểm tra hợp lệ, không đổi bước nhảy |
| Khoảng ngày (`showRange`, `Mode.Date`) | Một sheet, hai nút Từ / Đến | **Một dialog Material** chọn cả khoảng: chạm ngày bắt đầu rồi ngày kết thúc. `startLabel` và `endLabel` không dùng |
| Khoảng giờ hoặc ngày giờ | Một sheet | Hai picker nối nhau: chọn điểm bắt đầu rồi điểm kết thúc. Tiêu đề dạng `Tiêu đề · Từ` / `Tiêu đề · Đến` |
| Chế độ `DateTime` | Một sheet | Dialog ngày rồi dialog giờ |
| Điểm kết thúc | Không nhỏ hơn điểm bắt đầu | Dialog kết thúc lấy điểm bắt đầu làm giá trị nhỏ nhất |
| Lỗi `NO_VIEW_CONTROLLER` | Có | Thay bằng `NO_ACTIVITY` |
| Hủy | Nút hủy hoặc vuốt sheet | Nút hủy, chạm ngoài dialog hoặc nút Back. Hủy ở dialog nào cũng hủy cả khoảng |

Khoảng ngày trên Android dùng thư viện `com.google.android.material`, module tự khai báo thư viện và một theme riêng (`NativeDatePicker.Calendar`) nên ứng dụng không cần đổi theme sang Material. Chữ nút bấm được viết hoa theo theme.

## Hành vi

- **Chọn khoảng:** sheet có hai nút phía trên bánh xe, mỗi nút hiển thị nhãn và giá trị đang chọn. Chạm nút nào thì bánh xe sửa giá trị đó.
- **Ràng buộc khoảng:** khi sửa điểm kết thúc, các giá trị trước điểm bắt đầu không chọn được. Khi điểm bắt đầu vượt quá điểm kết thúc, điểm kết thúc được kéo theo. Nếu `endValue` truyền vào nhỏ hơn `startValue`, nó được nâng lên bằng `startValue`.
- **Làm tròn:** khi dùng `minuteInterval`, giá trị ban đầu được làm tròn theo bước nhảy.
- **Hủy:** chạm nút hủy hoặc vuốt sheet xuống đều tính là hủy.
- **Khoảng giờ:** hai giá trị nằm trong cùng một ngày. Khoảng qua đêm (ví dụ 22:00 đến 02:00) cần dùng chế độ `DateTime`.
- **Định dạng:** tên tháng, thứ tự ngày tháng và kiểu 12 hoặc 24 giờ theo cài đặt vùng của thiết bị.
- **Theme:** tùy chọn `theme` áp dụng đầy đủ từ iOS 17. Trên iOS 15 và 16, nền của sheet vẫn theo thiết bị.

## Lỗi thường gặp

| Hiện tượng | Nguyên nhân | Cách xử lý |
| :--- | :--- | :--- |
| `NativeDatePicker.isAvailable` là `false` trên iOS | Chưa prebuild lại hoặc chưa build native sau khi thêm module | Chạy `npx expo prebuild --clean` rồi build lại |
| Sheet sáng trong khi ứng dụng tối | Sheet theo chế độ của thiết bị, ứng dụng có theme riêng | Truyền `theme` theo theme của ứng dụng |
| Tiêu đề bị cắt bằng dấu ba chấm | Tiêu đề dài hơn khoảng trống giữa hai nút | Dùng tiêu đề ngắn, khoảng 20 ký tự trở xuống |
| Promise reject với `INVALID_MINUTE_INTERVAL` | `minuteInterval` không hợp lệ | Dùng một trong các giá trị 1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30 |
| Picker thứ hai không hiện | Gọi `show` khi picker trước chưa đóng | Chờ Promise của picker trước hoàn tất rồi mới gọi tiếp |
| Jest báo `Cannot use import statement outside a module` tại `index.ts` của module | Jest không biên dịch mã TypeScript nằm trong `node_modules` | Thêm `native-date-picker` vào danh sách ngoại lệ của `transformIgnorePatterns` trong `jest.config.js` |
| Sửa file Swift nhưng không thấy thay đổi | Reload Metro không áp dụng thay đổi native | Build lại bằng `npx expo run:ios` |
| `pod install` lỗi `Unicode Normalization not appropriate for ASCII-8BIT` | Shell không dùng mã hóa UTF-8 | Đặt `export LANG=en_US.UTF-8` rồi chạy lại |
