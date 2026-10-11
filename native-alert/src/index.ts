import { requireOptionalNativeModule } from "expo"
import { NativeModules, Platform } from "react-native"

/** Which button the user tapped, exposed as `NativeAlert.Button`. */
const Button = {
  Confirm: "confirm",
  Neutral: "neutral",
  Cancel: "cancel",
} as const

export type NativeAlertButton = (typeof Button)[keyof typeof Button]

export interface NativeAlertOptions {
  /** Optional: without it the alert shows the message alone. */
  title?: string
  message: string
  confirmText: string
  /** Optional third button. With it the buttons stack vertically: confirm, neutral, cancel. */
  neutralText?: string
  cancelText: string
  /** Called when the user taps the confirm button. */
  onConfirm?: () => void
  /** Called when the user taps the neutral button. */
  onNeutral?: () => void
  /** Called when the user taps the cancel button. */
  onCancel?: () => void
}

interface NativeAlertModule {
  show(
    title: string | null,
    message: string,
    confirmText: string,
    neutralText: string | null,
    cancelText: string,
  ): Promise<NativeAlertButton>
}

// iOS: Expo Modules API
const nativeModuleIOS = Platform.OS === "ios"
  ? requireOptionalNativeModule<NativeAlertModule>("NativeAlert")
  : null

// Android: ReactContextBaseJavaModule — getName() returns "NativeAlert"
const nativeModuleAndroid = Platform.OS === "android"
  ? (NativeModules.NativeAlert ?? null)
  : null

const nativeModule: NativeAlertModule | null = (nativeModuleIOS ?? nativeModuleAndroid) as NativeAlertModule | null

export const NativeAlert = {
  Button,
  /** True when the native module is linked into the running build. */
  isAvailable: nativeModule != null,
  /**
   * Shows an alert with two or three buttons and resolves with the button the user tapped, after
   * calling the matching callback. Resolves with null, calling no callback,
   * when the module is unavailable.
   */
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
} as const
