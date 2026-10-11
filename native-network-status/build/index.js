"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.NetworkStatus = void 0;
const expo_1 = require("expo");
const react_native_1 = require("react-native");
/** The kind of network in use, exposed as `NetworkStatus.Type`. */
const Type = {
    /** Not connected. */
    None: "none",
    Wifi: "wifi",
    Cellular: "cellular",
    Ethernet: "ethernet",
    /** Connected over something else, such as a VPN-only path. */
    Other: "other",
};
// iOS: Expo Modules API
const nativeModuleIOS = react_native_1.Platform.OS === "ios"
    ? (0, expo_1.requireOptionalNativeModule)("NetworkStatus")
    : null;
// Android: ReactContextBaseJavaModule — getName() returns "NetworkStatus"
const nativeModuleAndroid = react_native_1.Platform.OS === "android"
    ? (react_native_1.NativeModules.NetworkStatus ?? null)
    : null;
let eventEmitterAndroid = null;
if (nativeModuleAndroid) {
    eventEmitterAndroid = new react_native_1.NativeEventEmitter(nativeModuleAndroid);
}
const isIOS = react_native_1.Platform.OS === "ios";
const isAndroid = react_native_1.Platform.OS === "android";
exports.NetworkStatus = {
    Type,
    /** True when the native module is linked into the running build. */
    isAvailable: isIOS ? nativeModuleIOS != null : nativeModuleAndroid != null,
    /** Resolves with the current status, or null when the module is unavailable. */
    async getStatus() {
        if (isIOS)
            return nativeModuleIOS?.getStatus() ?? null;
        if (isAndroid)
            return nativeModuleAndroid?.getStatus() ?? null;
        return null;
    },
    /**
     * Calls `listener` whenever the status changes. It is not called with the current
     * status: read that with `getStatus`. Returns the unsubscribe function.
     */
    addListener(listener) {
        if (isIOS && nativeModuleIOS) {
            const subscription = nativeModuleIOS.addListener("onStatusChange", listener);
            return () => subscription.remove();
        }
        if (isAndroid && eventEmitterAndroid) {
            const subscription = eventEmitterAndroid.addListener("onStatusChange", (state) => listener(state));
            return () => subscription.remove();
        }
        return () => { };
    },
};
