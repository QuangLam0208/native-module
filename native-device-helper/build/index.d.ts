export interface BatteryInfo {
    /** Percentage from 0 to 100, or -1 when unknown (always the case on the simulator). */
    batteryLevel: number;
    isCharging: boolean;
}
export interface HardwareInfo extends BatteryInfo {
    totalRamMb: number;
    /**
     * Memory the app may still allocate, or -1 when unknown.
     * iOS does not report system-wide free RAM; the simulator always returns -1.
     */
    availableRamMb: number;
}
export declare const DeviceHelper: {
    /** True when the native module is linked into the running build. */
    readonly isAvailable: boolean;
    /** OS version string (e.g. "18.6.0" on iOS, "14" on Android), or null when unavailable. */
    readonly osVersion: any;
    /** Hardware identifier ("iPhone17,1" on iOS, device model string on Android), or null when unavailable. */
    readonly model: any;
    /** Resolves to null when the module is unavailable. */
    readonly getHardwareInfo: () => Promise<HardwareInfo | null>;
    /**
     * Calls `listener` whenever the battery level or charging state changes.
     * Returns the unsubscribe function.
     *
     * On Android the event name is "onBatteryChange", same as iOS.
     * Call removeListeners on the module side is handled automatically by NativeEventEmitter.
     */
    readonly addBatteryListener: (listener: (info: BatteryInfo) => void) => () => void;
};
