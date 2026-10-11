/** The kind of network in use, exposed as `NetworkStatus.Type`. */
declare const Type: {
    /** Not connected. */
    readonly None: "none";
    readonly Wifi: "wifi";
    readonly Cellular: "cellular";
    readonly Ethernet: "ethernet";
    /** Connected over something else, such as a VPN-only path. */
    readonly Other: "other";
};
export type NetworkType = (typeof Type)[keyof typeof Type];
export interface NetworkState {
    /** True when the device has a usable network path. It does not prove the internet is reachable. */
    isConnected: boolean;
    type: NetworkType;
    /** True on cellular and personal hotspots, where data may cost money. */
    isExpensive: boolean;
    /** True when the user turned on Low Data Mode. */
    isConstrained: boolean;
}
export declare const NetworkStatus: {
    readonly Type: {
        /** Not connected. */
        readonly None: "none";
        readonly Wifi: "wifi";
        readonly Cellular: "cellular";
        readonly Ethernet: "ethernet";
        /** Connected over something else, such as a VPN-only path. */
        readonly Other: "other";
    };
    /** True when the native module is linked into the running build. */
    readonly isAvailable: boolean;
    /** Resolves with the current status, or null when the module is unavailable. */
    readonly getStatus: () => Promise<NetworkState | null>;
    /**
     * Calls `listener` whenever the status changes. It is not called with the current
     * status: read that with `getStatus`. Returns the unsubscribe function.
     */
    readonly addListener: (listener: (state: NetworkState) => void) => () => void;
};
export {};
