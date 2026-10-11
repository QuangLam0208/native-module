"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.NativeDatePicker = void 0;
const expo_1 = require("expo");
const react_native_1 = require("react-native");
/** What the picker lets the user choose, exposed as `NativeDatePicker.Mode`. */
const Mode = {
    Date: "date",
    Time: "time",
    DateTime: "datetime",
};
/** Appearance of the picker sheet, exposed as `NativeDatePicker.Theme`. */
const Theme = {
    /** Follows the device setting. */
    System: "system",
    Light: "light",
    Dark: "dark",
};
// iOS: Expo Modules API
const nativeModuleIOS = react_native_1.Platform.OS === "ios"
    ? (0, expo_1.requireOptionalNativeModule)("NativeDatePicker")
    : null;
// Android: ReactContextBaseJavaModule
const nativeModuleAndroid = react_native_1.Platform.OS === "android"
    ? (react_native_1.NativeModules.NativeDatePicker ?? null)
    : null;
const nativeModule = (nativeModuleIOS ?? nativeModuleAndroid);
function toNativeOptions(options, value, endValue) {
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
    };
}
exports.NativeDatePicker = {
    Mode,
    Theme,
    /** True when the native module is linked into the running build. */
    isAvailable: nativeModule != null,
    /**
     * Shows a picker for one date and resolves with the confirmed date, or null when
     * it is cancelled, after calling the matching callback. Resolves with null,
     * calling no callback, when the module is unavailable.
     */
    async show(options) {
        if (!nativeModule)
            return null;
        const milliseconds = await nativeModule.show(toNativeOptions(options, options.value, undefined));
        if (milliseconds == null) {
            options.onCancel?.();
            return null;
        }
        const date = new Date(milliseconds);
        options.onConfirm?.(date);
        return date;
    },
    /**
     * Shows a picker for a start and an end and resolves with the confirmed range, or
     * null when it is cancelled, after calling the matching callback. Works with every
     * mode: a range of dates, of times, or of dates and times. Resolves with null,
     * calling no callback, when the module is unavailable.
     */
    async showRange(options) {
        if (!nativeModule)
            return null;
        const result = await nativeModule.showRange({
            ...toNativeOptions(options, options.startValue, options.endValue),
            startLabel: options.startLabel,
            endLabel: options.endLabel,
        });
        if (result == null) {
            options.onCancel?.();
            return null;
        }
        const range = { start: new Date(result.start), end: new Date(result.end) };
        options.onConfirm?.(range);
        return range;
    },
};
