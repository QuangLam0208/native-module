"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.NativeToast = void 0;
const expo_1 = require("expo");
const react_native_1 = require("react-native");
/** Toast types, exposed as `NativeToast.Type`. */
const Type = {
    Normal: "normal",
    Success: "success",
    Warning: "warning",
    Error: "error",
};
/** Preset durations in milliseconds, exposed as `NativeToast.Duration`. */
const Duration = {
    Short: 2200,
    Long: 3500,
};
// iOS: Expo Modules API
const nativeModuleIOS = react_native_1.Platform.OS === "ios"
    ? (0, expo_1.requireOptionalNativeModule)("NativeToast")
    : null;
// Android: ReactContextBaseJavaModule — getName() returns "NativeToast"
const nativeModuleAndroid = react_native_1.Platform.OS === "android"
    ? (react_native_1.NativeModules.NativeToast ?? null)
    : null;
const nativeModule = (nativeModuleIOS ?? nativeModuleAndroid);
exports.NativeToast = {
    Type,
    Duration,
    /** True when the native module is linked into the running build. */
    isAvailable: nativeModule != null,
    /** `durationMs` takes a `NativeToast.Duration` preset or any positive number of milliseconds. */
    show(message, type = Type.Normal, durationMs = Duration.Short) {
        return nativeModule?.show(message, type, durationMs) ?? Promise.resolve();
    },
};
