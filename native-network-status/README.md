# native-network-status

Native module theo dõi trạng thái kết nối mạng trên iOS & Android:
- **iOS:** Viết bằng Swift với Expo Modules API (`NWPathMonitor`).
- **Android:** Viết bằng Java với React Native Bridge (`ConnectivityManager.NetworkCallback`).

Module cho biết thiết bị có kết nối mạng hay không, đang dùng loại mạng nào (wifi, cellular, ethernet, other, none), và phát sự kiện mỗi khi trạng thái thay đổi.

## Thông tin chung

| Hạng mục | Giá trị |
| :--- | :--- |
| Nền tảng | iOS 15.1 trở lên · Android SDK 24 trở lên |
| Ngôn ngữ | Swift (iOS) · Java (Android) |
| Framework | Expo Modules API (iOS) · React Native Bridge (Android) |
| Đã kiểm thử với | Expo SDK 57, React Native 0.86 |
| Môi trường chạy | Development build hoặc bản release. Expo Go không hỗ trợ. |
| Quyền hệ thống | Android yêu cầu `ACCESS_NETWORK_STATE` (đã khai báo trong manifest) |

## Cấu trúc

```text
native-network-status/
├── expo-module.config.json       Khai báo module cho Expo Autolinking (iOS)
├── react-native.config.js        Khai báo module cho React Native CLI Autolinking (Android)
├── package.json
├── index.ts                      API TypeScript
├── ios/
│   ├── NetworkStatus.podspec
│   ├── NetworkStatusModule.swift Khai báo module, hàm `getStatus` và sự kiện
│   └── NetworkMonitor.swift      Theo dõi mạng bằng NWPathMonitor
└── android/
    ├── build.gradle              Cấu hình thư viện Android
    └── src/main/
        ├── AndroidManifest.xml   Khai báo ACCESS_NETWORK_STATE
        └── java/com/nativenetworkstatus/
            ├── NetworkStatusModule.java   Theo dõi mạng bằng ConnectivityManager
            └── NetworkStatusPackage.java  ReactPackage cho Autolinking
```

## Cài đặt

### Cài qua Git

```bash
pnpm add "github:loikimtrang/native-module#path:/native-network-status"
```

### Cài trong lúc phát triển (Local Dev)

```bash
pnpm add "file:<đường-dẫn-tới-repo>/native-network-status"
```

Sau đó sinh lại thư mục native và build:

```bash
npx expo prebuild --clean

# Chạy iOS:
npx expo run:ios

# Chạy Android:
npx expo run:android
```

## Ví dụ sử dụng

```tsx
import { useEffect, useState } from "react"
import { Text, View } from "react-native"
import { NetworkStatus, type NetworkState } from "native-network-status"

export function NetworkBanner() {
  const [network, setNetwork] = useState<NetworkState | null>(null)

  useEffect(() => {
    // Đọc trạng thái ban đầu
    NetworkStatus.getStatus().then(setNetwork)

    // Lắng nghe thay đổi kết nối (tự cleanup khi unmount)
    const unsubscribe = NetworkStatus.addListener(setNetwork)
    return () => unsubscribe()
  }, [])

  if (!network) return null

  return (
    <View>
      <Text>Đang kết nối: {network.isConnected ? "Có" : "Mất mạng"}</Text>
      <Text>Loại mạng: {network.type}</Text>
      <Text>Mạng tốn phí: {network.isExpensive ? "Có (3G/4G/Hotspot)" : "Không"}</Text>
    </View>
  )
}
```

## Bảng API

| Thuộc tính / Hàm | Kiểu | Mô tả |
| :--- | :--- | :--- |
| `NetworkStatus.isAvailable` | `boolean` | `true` khi native module có trong bản build |
| `NetworkStatus.Type` | `object` | `None`, `Wifi`, `Cellular`, `Ethernet`, `Other` |
| `NetworkStatus.getStatus()` | `Promise<NetworkState \| null>` | Đọc trạng thái mạng hiện tại |
| `NetworkStatus.addListener(fn)` | `() => void` | Lắng nghe thay đổi trạng thái mạng, trả về hàm hủy đăng ký |
