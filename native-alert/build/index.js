"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.NativeAlert = void 0;
const expo_1 = require("expo");
const react_native_1 = require("react-native");
/** Which button the user tapped, exposed as `NativeAlert.Button`. */
const Button = {
    Confirm: "confirm",
    Neutral: "neutral",
    Cancel: "cancel",
};
// iOS: Expo Modules API
const nativeModuleIOS = react_native_1.Platform.OS === "ios"
    ? (0, expo_1.requireOptionalNativeModule)("NativeAlert")
    : null;
// Android: ReactContextBaseJavaModule — getName() returns "NativeAlert"
const nativeModuleAndroid = react_native_1.Platform.OS === "android"
    ? (react_native_1.NativeModules.NativeAlert ?? null)
    : null;
const nativeModule = (nativeModuleIOS ?? nativeModuleAndroid);
exports.NativeAlert = {
    Button,
    /** True when the native module is linked into the running build. */
    isAvailable: nativeModule != null,
    /**
     * Shows an alert with two or three buttons and resolves with the button the user tapped, after
     * calling the matching callback. Resolves with null, calling no callback,
     * when the module is unavailable.
     */
    async show(options) {
        if (!nativeModule)
            return null;
        const { title, message, confirmText, neutralText, cancelText } = options;
        const button = await nativeModule.show(title ?? null, message, confirmText, neutralText ?? null, cancelText);
        if (button === Button.Confirm)
            options.onConfirm?.();
        else if (button === Button.Neutral)
            options.onNeutral?.();
        else
            options.onCancel?.();
        return button;
    },
};
