/** Which button the user tapped, exposed as `NativeAlert.Button`. */
declare const Button: {
    readonly Confirm: "confirm";
    readonly Neutral: "neutral";
    readonly Cancel: "cancel";
};
export type NativeAlertButton = (typeof Button)[keyof typeof Button];
export interface NativeAlertOptions {
    /** Optional: without it the alert shows the message alone. */
    title?: string;
    message: string;
    confirmText: string;
    /** Optional third button. With it the buttons stack vertically: confirm, neutral, cancel. */
    neutralText?: string;
    cancelText: string;
    /** Called when the user taps the confirm button. */
    onConfirm?: () => void;
    /** Called when the user taps the neutral button. */
    onNeutral?: () => void;
    /** Called when the user taps the cancel button. */
    onCancel?: () => void;
}
export declare const NativeAlert: {
    readonly Button: {
        readonly Confirm: "confirm";
        readonly Neutral: "neutral";
        readonly Cancel: "cancel";
    };
    /** True when the native module is linked into the running build. */
    readonly isAvailable: boolean;
    /**
     * Shows an alert with two or three buttons and resolves with the button the user tapped, after
     * calling the matching callback. Resolves with null, calling no callback,
     * when the module is unavailable.
     */
    readonly show: (options: NativeAlertOptions) => Promise<NativeAlertButton | null>;
};
export {};
