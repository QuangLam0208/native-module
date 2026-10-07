import { requireOptionalNativeModule } from "expo"
import { NativeModules, Platform } from "react-native"

/** What the picker lets the user choose, exposed as `NativeDatePicker.Mode`. */
const Mode = {
  Date: "date",
  Time: "time",
  DateTime: "datetime",
} as const

/** Appearance of the picker sheet, exposed as `NativeDatePicker.Theme`. */
const Theme = {
  /** Follows the device setting. */
  System: "system",
  Light: "light",
  Dark: "dark",
} as const

export type NativeDatePickerMode = (typeof Mode)[keyof typeof Mode]
export type NativeDatePickerTheme = (typeof Theme)[keyof typeof Theme]

export interface NativeDateRange {
  start: Date
  end: Date
}

/** Options shared by `show` and `showRange`. */
interface NativeDatePickerBaseOptions {
  /** Defaults to `NativeDatePicker.Mode.Date`. */
  mode?: NativeDatePickerMode
  minimumDate?: Date
  maximumDate?: Date
  /** Step of the minute wheel. Must divide 60, at most 30. Defaults to 1. */
  minuteInterval?: number
  /** Defaults to `NativeDatePicker.Theme.System`. */
  theme?: NativeDatePickerTheme
  /** Optional heading between the two buttons. */
  title?: string
  confirmText: string
  cancelText: string
  /** Called when the user taps cancel or swipes the picker away. */
  onCancel?: () => void
}

export interface NativeDatePickerOptions extends NativeDatePickerBaseOptions {
  /** The date selected when the picker opens. Defaults to now. */
  value?: Date
  /** Called with the date the user confirmed. */
  onConfirm?: (date: Date) => void
}

export interface NativeDateRangePickerOptions extends NativeDatePickerBaseOptions {
  /** The start selected when the picker opens. Defaults to now. */
  startValue?: Date
  /** The end selected when the picker opens. Defaults to the start. */
  endValue?: Date
  /** Label of the button that edits the start, e.g. "From". */
  startLabel: string
  /** Label of the button that edits the end, e.g. "To". */
  endLabel: string
  /** Called with the range the user confirmed. `end` is never before `start`. */
  onConfirm?: (range: NativeDateRange) => void
}

// Dates cross the bridge as milliseconds since 1970.
interface NativeOptions {
  mode: NativeDatePickerMode
  value: number | null
  endValue: number | null
  minimumDate: number | null
  maximumDate: number | null
  minuteInterval: number
  theme: NativeDatePickerTheme
  title: string | null
  startLabel?: string
  endLabel?: string
  confirmText: string
  cancelText: string
}

interface NativeDatePickerModule {
  show(options: NativeOptions): Promise<number | null>
  showRange(options: NativeOptions): Promise<{ start: number; end: number } | null>
}

// iOS: Expo Modules API
const nativeModuleIOS = Platform.OS === "ios"
  ? requireOptionalNativeModule<NativeDatePickerModule>("NativeDatePicker")
  : null

// Android: ReactContextBaseJavaModule
const nativeModuleAndroid = Platform.OS === "android"
  ? (NativeModules.NativeDatePicker ?? null)
  : null

const nativeModule: NativeDatePickerModule | null =
  (nativeModuleIOS ?? nativeModuleAndroid) as NativeDatePickerModule | null

function toNativeOptions(
  options: NativeDatePickerBaseOptions,
  value: Date | undefined,
  endValue: Date | undefined,
): NativeOptions {
  return {
    mode: options.mode ?? Mode.Date,
    value: value?.getTime() ?? null,
    endValue: endValue?.getTime() ?? null,
    minimumDate: options.minimumDate?.getTime() ?? null,
    maximumDate: options.maximumDate?.getTime() ?? null,
    minuteInterval: options.minuteInterval ?? 1,
    theme: options.theme ?? Theme.System,
    title: options.title ?? null,
    confirmText: options.confirmText,
    cancelText: options.cancelText,
  }
}

export const NativeDatePicker = {
  Mode,
  Theme,
  /** True when the native module is linked into the running build. */
  isAvailable: nativeModule != null,
  /**
   * Shows a picker for one date and resolves with the confirmed date, or null when
   * it is cancelled, after calling the matching callback. Resolves with null,
   * calling no callback, when the module is unavailable.
   */
  async show(options: NativeDatePickerOptions): Promise<Date | null> {
    if (!nativeModule) return null

    const milliseconds = await nativeModule.show(toNativeOptions(options, options.value, undefined))
    if (milliseconds == null) {
      options.onCancel?.()
      return null
    }
    const date = new Date(milliseconds)
    options.onConfirm?.(date)
    return date
  },
  /**
   * Shows a picker for a start and an end and resolves with the confirmed range, or
   * null when it is cancelled, after calling the matching callback. Works with every
   * mode: a range of dates, of times, or of dates and times. Resolves with null,
   * calling no callback, when the module is unavailable.
   */
  async showRange(options: NativeDateRangePickerOptions): Promise<NativeDateRange | null> {
    if (!nativeModule) return null

    const result = await nativeModule.showRange({
      ...toNativeOptions(options, options.startValue, options.endValue),
      startLabel: options.startLabel,
      endLabel: options.endLabel,
    })
    if (result == null) {
      options.onCancel?.()
      return null
    }
    const range = { start: new Date(result.start), end: new Date(result.end) }
    options.onConfirm?.(range)
    return range
  },
} as const
