/** What the picker lets the user choose, exposed as `NativeDatePicker.Mode`. */
declare const Mode: {
    readonly Date: "date";
    readonly Time: "time";
    readonly DateTime: "datetime";
};
/** Appearance of the picker sheet, exposed as `NativeDatePicker.Theme`. */
declare const Theme: {
    /** Follows the device setting. */
    readonly System: "system";
    readonly Light: "light";
    readonly Dark: "dark";
};
export type NativeDatePickerMode = (typeof Mode)[keyof typeof Mode];
export type NativeDatePickerTheme = (typeof Theme)[keyof typeof Theme];
export interface NativeDateRange {
    start: Date;
    end: Date;
}
/** Options shared by `show` and `showRange`. */
interface NativeDatePickerBaseOptions {
    /** Defaults to `NativeDatePicker.Mode.Date`. */
    mode?: NativeDatePickerMode;
    minimumDate?: Date;
    maximumDate?: Date;
    /** Step of the minute wheel. Must divide 60, at most 30. Defaults to 1. */
    minuteInterval?: number;
    /** Defaults to `NativeDatePicker.Theme.System`. */
    theme?: NativeDatePickerTheme;
    /** Optional heading between the two buttons. */
    title?: string;
    confirmText: string;
    cancelText: string;
    /** Called when the user taps cancel or swipes the picker away. */
    onCancel?: () => void;
}
export interface NativeDatePickerOptions extends NativeDatePickerBaseOptions {
    /** The date selected when the picker opens. Defaults to now. */
    value?: Date;
    /** Called with the date the user confirmed. */
    onConfirm?: (date: Date) => void;
}
export interface NativeDateRangePickerOptions extends NativeDatePickerBaseOptions {
    /** The start selected when the picker opens. Defaults to now. */
    startValue?: Date;
    /** The end selected when the picker opens. Defaults to the start. */
    endValue?: Date;
    /** Label of the button that edits the start, e.g. "From". */
    startLabel: string;
    /** Label of the button that edits the end, e.g. "To". */
    endLabel: string;
    /** Called with the range the user confirmed. `end` is never before `start`. */
    onConfirm?: (range: NativeDateRange) => void;
}
export declare const NativeDatePicker: {
    readonly Mode: {
        readonly Date: "date";
        readonly Time: "time";
        readonly DateTime: "datetime";
    };
    readonly Theme: {
        /** Follows the device setting. */
        readonly System: "system";
        readonly Light: "light";
        readonly Dark: "dark";
    };
    /** True when the native module is linked into the running build. */
    readonly isAvailable: boolean;
    /**
     * Shows a picker for one date and resolves with the confirmed date, or null when
     * it is cancelled, after calling the matching callback. Resolves with null,
     * calling no callback, when the module is unavailable.
     */
    readonly show: (options: NativeDatePickerOptions) => Promise<Date | null>;
    /**
     * Shows a picker for a start and an end and resolves with the confirmed range, or
     * null when it is cancelled, after calling the matching callback. Works with every
     * mode: a range of dates, of times, or of dates and times. Resolves with null,
     * calling no callback, when the module is unavailable.
     */
    readonly showRange: (options: NativeDateRangePickerOptions) => Promise<NativeDateRange | null>;
};
export {};
