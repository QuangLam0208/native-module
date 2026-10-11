"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.DeviceHelper = void 0;
const expo_1 = require("expo");
const react_native_1 = require("react-native");
const nativeModuleIOS = (0, expo_1.requireOptionalNativeModule)("DeviceHelper");
// ─── Android (ReactContextBaseJavaModule) ─────────────────────────────────
// getName() trả về "DeviceHelper" — cùng key với iOS để index.ts dùng chung tên.
const nativeModuleAndroid = react_native_1.Platform.OS === "android"
    ? (react_native_1.NativeModules.DeviceHelper ?? null)
    : null;
let eventEmitterAndroid = null;
if (nativeModuleAndroid) {
    eventEmitterAndroid = new react_native_1.NativeEventEmitter(nativeModuleAndroid);
}
// ─── Unified API ──────────────────────────────────────────────────────────
const isIOS = react_native_1.Platform.OS === "ios";
const isAndroid = react_native_1.Platform.OS === "android";
exports.DeviceHelper = {
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
    async getHardwareInfo() {
        if (isIOS)
            return nativeModuleIOS?.getHardwareInfo() ?? null;
        if (isAndroid)
            return nativeModuleAndroid?.getHardwareInfo() ?? null;
        return null;
    },
    /**
     * Calls `listener` whenever the battery level or charging state changes.
     * Returns the unsubscribe function.
     *
     * On Android the event name is "onBatteryChange", same as iOS.
     * Call removeListeners on the module side is handled automatically by NativeEventEmitter.
     */
    addBatteryListener(listener) {
        if (isIOS && nativeModuleIOS) {
            const subscription = nativeModuleIOS.addListener("onBatteryChange", listener);
            return () => subscription.remove();
        }
        if (isAndroid && eventEmitterAndroid) {
            const subscription = eventEmitterAndroid.addListener("onBatteryChange", (event) => listener(event));
            return () => subscription.remove();
        }
        return () => { };
    },
};
