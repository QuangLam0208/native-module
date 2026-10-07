import { requireOptionalNativeModule } from "expo"
import { NativeModules, NativeEventEmitter, Platform } from "react-native"

/** The kind of network in use, exposed as `NetworkStatus.Type`. */
const Type = {
  /** Not connected. */
  None: "none",
  Wifi: "wifi",
  Cellular: "cellular",
  Ethernet: "ethernet",
  /** Connected over something else, such as a VPN-only path. */
  Other: "other",
} as const

export type NetworkType = (typeof Type)[keyof typeof Type]

export interface NetworkState {
  /** True when the device has a usable network path. It does not prove the internet is reachable. */
  isConnected: boolean
  type: NetworkType
  /** True on cellular and personal hotspots, where data may cost money. */
  isExpensive: boolean
  /** True when the user turned on Low Data Mode. */
  isConstrained: boolean
}

interface Subscription {
  remove(): void
}

interface NetworkStatusModuleIOS {
  getStatus(): Promise<NetworkState>
  addListener(event: "onStatusChange", listener: (state: NetworkState) => void): Subscription
}

// iOS: Expo Modules API
const nativeModuleIOS = Platform.OS === "ios"
  ? requireOptionalNativeModule<NetworkStatusModuleIOS>("NetworkStatus")
  : null

// Android: ReactContextBaseJavaModule — getName() returns "NetworkStatus"
const nativeModuleAndroid = Platform.OS === "android"
  ? (NativeModules.NetworkStatus ?? null)
  : null

let eventEmitterAndroid: NativeEventEmitter | null = null
if (nativeModuleAndroid) {
  eventEmitterAndroid = new NativeEventEmitter(nativeModuleAndroid)
}

const isIOS = Platform.OS === "ios"
const isAndroid = Platform.OS === "android"

export const NetworkStatus = {
  Type,
  /** True when the native module is linked into the running build. */
  isAvailable: isIOS ? nativeModuleIOS != null : nativeModuleAndroid != null,
  /** Resolves with the current status, or null when the module is unavailable. */
  async getStatus(): Promise<NetworkState | null> {
    if (isIOS) return nativeModuleIOS?.getStatus() ?? null
    if (isAndroid) return nativeModuleAndroid?.getStatus() ?? null
    return null
  },
  /**
   * Calls `listener` whenever the status changes. It is not called with the current
   * status: read that with `getStatus`. Returns the unsubscribe function.
   */
  addListener(listener: (state: NetworkState) => void): () => void {
    if (isIOS && nativeModuleIOS) {
      const subscription = nativeModuleIOS.addListener("onStatusChange", listener)
      return () => subscription.remove()
    }
    if (isAndroid && eventEmitterAndroid) {
      const subscription = eventEmitterAndroid.addListener("onStatusChange", (state: any) => listener(state))
      return () => subscription.remove()
    }
    return () => {}
  },
} as const
