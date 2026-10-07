# native-device-helper

Native module đọc thông tin thiết bị và phần cứng trên iOS & Android:
- **iOS:** Viết bằng Swift với Expo Modules API.
- **Android:** Viết bằng Java với React Native Bridge (`ReactContextBaseJavaModule`).

Module cung cấp model máy, phiên bản hệ điều hành, dung lượng RAM, trạng thái pin, và phát sự kiện khi mức pin hoặc trạng thái sạc thay đổi.

## Thông tin chung

| Hạng mục | Giá trị |
| :--- | :--- |
| Nền tảng | iOS 15.1 trở lên · Android SDK 24 trở lên |
| Ngôn ngữ | Swift (iOS) · Java (Android) |
| Framework | Expo Modules API (iOS) · React Native Bridge (Android) |
| Đã kiểm thử với | Expo SDK 57, React Native 0.86 |
| Môi trường chạy | Development build hoặc bản release. Expo Go không hỗ trợ. |
| Quyền hệ thống | Không yêu cầu quyền đặc biệt |

## Cấu trúc

```text
native-device-helper/
├── expo-module.config.json      Khai báo module cho Expo Autolinking (iOS)
├── react-native.config.js       Khai báo module cho React Native CLI Autolinking (Android)
├── package.json
├── index.ts                     API TypeScript
├── ios/
│   ├── DeviceHelper.podspec
│   ├── DeviceHelperModule.swift Khai báo module, hằng số, hàm và sự kiện
│   ├── DeviceInfo.swift         Model và phiên bản hệ điều hành
│   └── HardwareReader.swift     RAM và pin
└── android/
    ├── build.gradle             Cấu hình thư viện Android
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/nativedevicehelper/
            ├── DeviceHelperModule.java   Đọc RAM, Pin, OS, Model và phát sự kiện pin
            └── DeviceHelperPackage.java  ReactPackage cho Autolinking
```

## Cài đặt

Ứng dụng phải dùng Expo Prebuild. Module được liên kết tự động qua Autolinking, không cần chỉnh sửa bất kỳ file native nào.

### Cài qua Git

```bash
pnpm add "github:loikimtrang/native-module#path:/native-device-helper"
```

### Cài trong lúc phát triển (Local Dev)

```bash
pnpm add "file:<đường-dẫn-tới-repo>/native-device-helper"
```

Sau đó sinh lại thư mục native và build:

```bash
npx expo prebuild --clean

# Chạy iOS:
npx expo run:ios

# Chạy Android:
npx expo run:android
```

## Cách sử dụng

```tsx
import { useEffect, useState } from "react"
import { Text, View } from "react-native"
import { DeviceHelper, type BatteryInfo, type HardwareInfo } from "native-device-helper"

export function DeviceCard() {
  const [hardware, setHardware] = useState<HardwareInfo | null>(null)
  const [battery, setBattery] = useState<BatteryInfo | null>(null)

  useEffect(() => {
    // Đọc thông tin phần cứng
    DeviceHelper.getHardwareInfo().then(setHardware)

    // Lắng nghe sự kiện pin thay đổi (tự động cleanup khi unmount)
    const unsubscribe = DeviceHelper.addBatteryListener(setBattery)
    return () => unsubscribe()
  }, [])

  return (
    <View>
      <Text>Hệ điều hành: {DeviceHelper.osVersion}</Text>
      <Text>Thiết bị: {DeviceHelper.model}</Text>
      {hardware && (
        <Text>
          RAM: {hardware.availableRamMb} MB / {hardware.totalRamMb} MB
        </Text>
      )}
      {battery && (
        <Text>
          Pin: {battery.batteryLevel}% {battery.isCharging ? "(Đang sạc)" : ""}
        </Text>
      )}
    </View>
  )
}
```

## Bảng API

| Thuộc tính / Hàm | Kiểu | Mô tả |
| :--- | :--- | :--- |
| `DeviceHelper.isAvailable` | `boolean` | `true` khi native module có trong bản build |
| `DeviceHelper.osVersion` | `string \| null` | Phiên bản hệ điều hành (ví dụ `"18.6.0"` trên iOS, `"14"` trên Android) |
| `DeviceHelper.model` | `string \| null` | Model thiết bị (ví dụ `"iPhone17,1"` trên iOS, `"Pixel 7"` trên Android) |
| `DeviceHelper.getHardwareInfo()` | `Promise<HardwareInfo \| null>` | Đọc RAM và pin bất đồng bộ |
| `DeviceHelper.addBatteryListener(fn)` | `() => void` | Lắng nghe pin, trả về hàm hủy đăng ký |
