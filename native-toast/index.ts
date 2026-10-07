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

// iOS: Expo Modules API
const nativeModuleIOS = Platform.OS === "ios"
  ? requireOptionalNativeModule<NativeToastModule>("NativeToast")
  : null

// Android: ReactContextBaseJavaModule — getName() returns "NativeToast"
const nativeModuleAndroid = Platform.OS === "android"
  ? (NativeModules.NativeToast ?? null)
  : null

const nativeModule: NativeToastModule | null = (nativeModuleIOS ?? nativeModuleAndroid) as NativeToastModule | null

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
