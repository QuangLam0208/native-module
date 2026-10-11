import { requireOptionalNativeModule } from "expo"
import { NativeModules, NativeEventEmitter, Platform } from "react-native"

export interface BatteryInfo {
  /** Percentage from 0 to 100, or -1 when unknown (always the case on the simulator). */
  batteryLevel: number
  isCharging: boolean
}

export interface HardwareInfo extends BatteryInfo {
  totalRamMb: number
  /**
   * Memory the app may still allocate, or -1 when unknown.
   * iOS does not report system-wide free RAM; the simulator always returns -1.
   */
  availableRamMb: number
}

// ─── iOS (Expo Modules API) ────────────────────────────────────────────────
interface DeviceHelperModuleIOS {
  osVersion: string
  model: string
  getHardwareInfo(): Promise<HardwareInfo>
  addListener(event: "onBatteryChange", listener: (info: BatteryInfo) => void): { remove(): void }
}

const nativeModuleIOS = requireOptionalNativeModule<DeviceHelperModuleIOS>("DeviceHelper")

// ─── Android (ReactContextBaseJavaModule) ─────────────────────────────────
// getName() trả về "DeviceHelper" — cùng key với iOS để index.ts dùng chung tên.
const nativeModuleAndroid = Platform.OS === "android"
  ? (NativeModules.DeviceHelper ?? null)
  : null

let eventEmitterAndroid: NativeEventEmitter | null = null
if (nativeModuleAndroid) {
  eventEmitterAndroid = new NativeEventEmitter(nativeModuleAndroid)
}

// ─── Unified API ──────────────────────────────────────────────────────────
const isIOS     = Platform.OS === "ios"
const isAndroid = Platform.OS === "android"

export const DeviceHelper = {
  /** True when the native module is linked into the running build. */
  isAvailable: isIOS ? nativeModuleIOS != null : nativeModuleAndroid != null,

  /** OS version string (e.g. "18.6.0" on iOS, "14" on Android), or null when unavailable. */
  osVersion: isIOS
    ? (nativeModuleIOS?.osVersion ?? null)
    : (nativeModuleAndroid?.osVersion ?? null),

  /** Hardware identifier ("iPhone17,1" on iOS, device model string on Android), or null when unavailable. */
  model: isIOS
    ? (nativeModuleIOS?.model ?? null)
    : (nativeModuleAndroid?.model ?? null),

  /** Resolves to null when the module is unavailable. */
  async getHardwareInfo(): Promise<HardwareInfo | null> {
    if (isIOS)     return nativeModuleIOS?.getHardwareInfo() ?? null
    if (isAndroid) return nativeModuleAndroid?.getHardwareInfo() ?? null
    return null
  },

  /**
   * Calls `listener` whenever the battery level or charging state changes.
   * Returns the unsubscribe function.
   *
   * On Android the event name is "onBatteryChange", same as iOS.
   * Call removeListeners on the module side is handled automatically by NativeEventEmitter.
   */
  addBatteryListener(listener: (info: BatteryInfo) => void): () => void {
    if (isIOS && nativeModuleIOS) {
      const subscription = nativeModuleIOS.addListener("onBatteryChange", listener)
      return () => subscription.remove()
    }
    if (isAndroid && eventEmitterAndroid) {
      const subscription = eventEmitterAndroid.addListener(
        "onBatteryChange",
        (event: any) => listener(event)
      )
      return () => subscription.remove()
    }
    return () => {}
  },
} as const
