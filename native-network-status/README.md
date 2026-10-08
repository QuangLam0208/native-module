# native-network-status

Native module theo dõi trạng thái kết nối mạng trên iOS (Swift, Expo Modules API) và Android (Java, React Native module).

Module cho biết thiết bị có kết nối mạng hay không, đang dùng loại mạng nào, và phát sự kiện mỗi khi trạng thái thay đổi.

## Thông tin chung

| Hạng mục | Giá trị |
| :--- | :--- |
| Nền tảng | iOS 15.1 trở lên, Android |
| Ngôn ngữ | Swift (iOS), Java (Android) |
| Framework | Expo Modules API, `NWPathMonitor` (iOS), `ConnectivityManager` (Android) |
| Đã kiểm thử với | Expo SDK 57, React Native 0.86, simulator iOS 27.0, Samsung Galaxy J6+ (Android 10) |
| Môi trường chạy | Development build hoặc bản release. Expo Go không hỗ trợ. |
| Quyền hệ thống | Không yêu cầu từ người dùng. Android cần `ACCESS_NETWORK_STATE`, module tự khai báo trong manifest và được gộp tự động |

Trên nền tảng khác, module không tồn tại: `NetworkStatus.isAvailable` là `false`, `getStatus` trả về `null` và listener không bao giờ được gọi.

**Phạm vi đã kiểm thử trên Android:** `getStatus` (có mạng qua Wi-Fi), sự kiện khi tắt Wi-Fi (mất mạng) và bật lại (có mạng qua Wi-Fi), mỗi lần đúng một sự kiện. Chưa kiểm thử mạng di động, `isExpensive` và `isConstrained`.

**Phạm vi đã kiểm thử trên iOS:** `getStatus` đã được kiểm thử trên simulator, trả về đúng trạng thái có mạng qua Wi-Fi. Sự kiện thay đổi trạng thái, trạng thái mất mạng, mạng di động, `isExpensive` và `isConstrained` chưa được kiểm thử.

## Cấu trúc

```text
native-network-status/
├── expo-module.config.json     Khai báo module cho Autolinking
├── package.json
├── index.ts                    API TypeScript
├── react-native.config.js      Cấu hình Autolinking cho Android
├── android/                    Phần Android (Java): `NetworkStatusModule.java`, `NetworkStatusPackage.java`
└── ios/
    ├── NetworkStatus.podspec
    ├── NetworkStatusModule.swift   Khai báo module, hàm `getStatus` và sự kiện
    └── NetworkMonitor.swift        Theo dõi mạng bằng NWPathMonitor
```

## Cài đặt

Ứng dụng phải dùng Expo Prebuild. Module được liên kết tự động qua Autolinking, không cần chỉnh sửa `Podfile`, `AppDelegate` hay `Info.plist`.

### Cách 1: Cài như một dependency qua Git (khuyến nghị)

Module nằm trong thư mục con `native-network-status` của repo `react-native-module`, nên cần chỉ rõ đường dẫn bằng `path:`:

```bash
pnpm add "git+https://git.itzsolution.com/scm/tsa/react-native-module.git#path:/native-network-status"
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
import { NetworkStatus } from "native-network-status"
```

**Cập nhật lên phiên bản mới:** chạy lại lệnh cài để pnpm lấy commit mới nhất, sau đó chạy `pod install` trong thư mục `ios/` của ứng dụng (hoặc prebuild lại) và build lại native. Nếu Metro đang chạy, khởi động lại Metro sau khi cài.

### Cách 2: Module cục bộ trong ứng dụng

Dùng khi cần chỉnh sửa mã nguồn module ngay trong ứng dụng. Clone repo rồi sao chép thư mục `native-network-status` vào `modules/` ở gốc ứng dụng:

```bash
git clone https://git.itzsolution.com/scm/tsa/react-native-module.git
```

```bash
cp -R react-native-module/native-network-status <thư-mục-ứng-dụng>/modules/native-network-status
```

Sau đó prebuild và build như Cách 1. Import bằng đường dẫn tương đối:

```ts
import { NetworkStatus } from "../modules/native-network-status"
```

## Sử dụng

### Đọc trạng thái hiện tại

```tsx
import { NetworkStatus } from "native-network-status"

const state = await NetworkStatus.getStatus()
if (state && !state.isConnected) {
  showOfflineBanner()
}
```

### Lắng nghe thay đổi

```tsx
const unsubscribe = NetworkStatus.addListener((state) => {
  console.log(state.isConnected, state.type)
})

// Hủy lắng nghe khi không còn dùng
unsubscribe()
```

Listener chỉ được gọi khi trạng thái thay đổi, không được gọi với trạng thái hiện tại lúc đăng ký. Trong component React, đọc trạng thái ban đầu bằng `getStatus` rồi đăng ký listener:

```tsx
const [state, setState] = useState<NetworkState | null>(null)

useEffect(() => {
  void NetworkStatus.getStatus().then(setState)
  return NetworkStatus.addListener(setState)
}, [])
```

### Chỉ tải dữ liệu nặng khi dùng Wi-Fi

```tsx
const state = await NetworkStatus.getStatus()
if (state?.type === NetworkStatus.Type.Wifi && !state.isConstrained) {
  downloadLargeFile()
}
```

## API

### `NetworkStatus.getStatus()`

Trả về `Promise<NetworkState | null>`. Kết quả là `null` khi module không khả dụng.

| Trường | Kiểu | Mô tả |
| :--- | :--- | :--- |
| `isConnected` | `boolean` | `true` khi thiết bị có đường kết nối mạng dùng được |
| `type` | `NetworkType` | Loại mạng đang dùng |
| `isExpensive` | `boolean` | `true` với mạng di động và điểm phát sóng cá nhân |
| `isConstrained` | `boolean` | `true` khi người dùng bật Chế độ dữ liệu thấp |

### `NetworkStatus.addListener(listener)`

Gọi `listener` với một `NetworkState` mỗi khi trạng thái thay đổi. Trả về hàm hủy đăng ký.

### `NetworkStatus.isAvailable`

`boolean`. `true` khi module native đã được liên kết vào bản build đang chạy.

### `NetworkStatus.Type`

| Hằng số | Giá trị | Ý nghĩa |
| :--- | :--- | :--- |
| `NetworkStatus.Type.None` | `"none"` | Không có kết nối |
| `NetworkStatus.Type.Wifi` | `"wifi"` | Wi-Fi |
| `NetworkStatus.Type.Cellular` | `"cellular"` | Mạng di động |
| `NetworkStatus.Type.Ethernet` | `"ethernet"` | Mạng dây |
| `NetworkStatus.Type.Other` | `"other"` | Có kết nối qua loại mạng khác |

### Kiểu dữ liệu

```ts
import type { NetworkState, NetworkType } from "native-network-status"
```

## Khác biệt giữa iOS và Android

| Hạng mục | iOS | Android |
| :--- | :--- | :--- |
| Cơ chế | `NWPathMonitor` chạy liên tục từ khi nạp module | `registerDefaultNetworkCallback`, chỉ đăng ký khi có listener đầu tiên và gỡ khi hết listener |
| `isConstrained` | `true` khi bật Chế độ dữ liệu thấp | `true` khi mạng không ở trạng thái `NOT_CONGESTED` (Android 10 trở lên), nghĩa khác iOS. Dưới Android 10 luôn `false` |
| `isExpensive` | Mạng di động và điểm phát sóng cá nhân | Mạng không có `NOT_METERED` |

Cả hai nền tảng đều chỉ phát sự kiện khi trạng thái khác lần trước.

## Hành vi

- **Có kết nối không có nghĩa là vào được internet.** `isConnected` cho biết thiết bị có đường kết nối mạng. Một mạng Wi-Fi chưa đăng nhập cổng xác thực vẫn được báo là có kết nối. Cần chắc chắn thì gọi thử một API của ứng dụng.
- **Sự kiện không lặp.** Hệ điều hành có thể báo cùng một trạng thái nhiều lần liên tiếp; module chỉ phát sự kiện khi trạng thái thực sự khác trước.
- **Thứ tự ưu tiên của `type`:** Wi-Fi, mạng di động, mạng dây, rồi đến loại khác. Khi thiết bị dùng đồng thời nhiều loại, giá trị trả về là loại đứng trước trong thứ tự này.
- **Theo dõi liên tục.** Module theo dõi mạng từ khi được nạp, kể cả khi chưa có listener, để `getStatus` trả kết quả ngay. Việc theo dõi là thụ động và không tốn pin khi mạng ổn định.
- **Trên simulator,** trạng thái phản ánh mạng của máy Mac.

## Lỗi thường gặp

| Hiện tượng | Nguyên nhân | Cách xử lý |
| :--- | :--- | :--- |
| `NetworkStatus.isAvailable` là `false` trên iOS | Chưa prebuild lại hoặc chưa build native sau khi thêm module | Chạy `npx expo prebuild --clean` rồi build lại |
| Listener không được gọi lúc vừa đăng ký | Listener chỉ nhận thay đổi, không nhận trạng thái hiện tại | Đọc trạng thái ban đầu bằng `getStatus` |
| `isConnected` là `true` nhưng gọi API thất bại | Có đường kết nối nhưng không vào được internet | Xử lý lỗi của lời gọi API, không chỉ dựa vào `isConnected` |
| Jest báo `Cannot use import statement outside a module` tại `index.ts` của module | Jest không biên dịch mã TypeScript nằm trong `node_modules` | Thêm `native-network-status` vào danh sách ngoại lệ của `transformIgnorePatterns` trong `jest.config.js` |
| Sửa file Swift nhưng không thấy thay đổi | Reload Metro không áp dụng thay đổi native | Build lại bằng `npx expo run:ios` |
| `pod install` lỗi `Unicode Normalization not appropriate for ASCII-8BIT` | Shell không dùng mã hóa UTF-8 | Đặt `export LANG=en_US.UTF-8` rồi chạy lại |
