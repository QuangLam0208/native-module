/** Toast types, exposed as `NativeToast.Type`. */
declare const Type: {
    readonly Normal: "normal";
    readonly Success: "success";
    readonly Warning: "warning";
    readonly Error: "error";
};
export type NativeToastType = (typeof Type)[keyof typeof Type];
export declare const NativeToast: {
    readonly Type: {
        readonly Normal: "normal";
        readonly Success: "success";
        readonly Warning: "warning";
        readonly Error: "error";
    };
    readonly Duration: {
        readonly Short: 2200;
        readonly Long: 3500;
    };
    /** True when the native module is linked into the running build. */
    readonly isAvailable: boolean;
    /** `durationMs` takes a `NativeToast.Duration` preset or any positive number of milliseconds. */
    readonly show: (message: string, type?: NativeToastType, durationMs?: number) => Promise<void>;
};
export {};
