package com.nativenetworkstatus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class NetworkStatusModule extends ReactContextBaseJavaModule {

  static final String NAME = "NetworkStatus";
  private static final String EVENT_STATUS_CHANGE = "onStatusChange";

  private final ReactApplicationContext reactContext;
  private final ConnectivityManager connectivityManager;
  private int listenerCount = 0;

  // Theo dõi mạng mặc định, chỉ chạy khi có ít nhất một listener phía JS (giống iOS).
  @Nullable private ConnectivityManager.NetworkCallback networkCallback;
  @Nullable private Network currentNetwork;
  @Nullable private String lastSignature;

  public NetworkStatusModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.connectivityManager = (ConnectivityManager) reactContext.getSystemService(Context.CONNECTIVITY_SERVICE);
  }

  @NonNull
  @Override
  public String getName() {
    return NAME;
  }

  /** Mô tả một mạng từ khả năng của nó. `caps` null nghĩa là không có kết nối. */
  private static WritableMap describe(@Nullable NetworkCapabilities caps) {
    boolean connected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    String type = "none";
    boolean expensive = false;
    boolean constrained = false;

    if (connected) {
      type = "other";
      if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
        type = "wifi";
      } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
        type = "cellular";
      } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
        type = "ethernet";
      }
      expensive = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        constrained = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED);
      }
    }

    WritableMap state = Arguments.createMap();
    state.putBoolean("isConnected", connected);
    state.putString("type", type);
    state.putBoolean("isExpensive", expensive);
    state.putBoolean("isConstrained", constrained);
    return state;
  }

  private static String signature(WritableMap state) {
    return state.getBoolean("isConnected") + "|" + state.getString("type") + "|"
        + state.getBoolean("isExpensive") + "|" + state.getBoolean("isConstrained");
  }

  @Nullable
  private NetworkCapabilities activeCapabilities() {
    if (connectivityManager == null) return null;
    Network active = connectivityManager.getActiveNetwork();
    return active == null ? null : connectivityManager.getNetworkCapabilities(active);
  }

  @ReactMethod
  public void getStatus(Promise promise) {
    try {
      promise.resolve(describe(activeCapabilities()));
    } catch (Exception e) {
      promise.reject("NETWORK_ERROR", e.getMessage(), e);
    }
  }

  @ReactMethod
  public void addListener(String eventName) {
    listenerCount++;
    if (listenerCount == 1) startObserving();
  }

  @ReactMethod
  public void removeListeners(Integer count) {
    listenerCount = Math.max(0, listenerCount - count);
    if (listenerCount == 0) stopObserving();
  }

  @Override
  public void invalidate() {
    stopObserving();
    super.invalidate();
  }

  private synchronized void startObserving() {
    if (networkCallback != null || connectivityManager == null) return;

    // Ghi nhớ trạng thái hiện tại: hệ thống báo ngay mạng đang dùng khi đăng ký, lần báo đó không phải thay đổi.
    lastSignature = signature(describe(activeCapabilities()));

    networkCallback = new ConnectivityManager.NetworkCallback() {
      @Override
      public void onAvailable(@NonNull Network network) {
        currentNetwork = network;
      }

      @Override
      public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities caps) {
        currentNetwork = network;
        emitIfChanged(describe(caps));
      }

      @Override
      public void onLost(@NonNull Network network) {
        // Mạng cũ mất sau khi đã chuyển sang mạng mới thì không phải mất kết nối.
        if (network.equals(currentNetwork)) {
          currentNetwork = null;
          emitIfChanged(describe(null));
        }
      }
    };
    connectivityManager.registerDefaultNetworkCallback(networkCallback);
  }

  private synchronized void stopObserving() {
    if (networkCallback == null || connectivityManager == null) return;
    try {
      connectivityManager.unregisterNetworkCallback(networkCallback);
    } catch (IllegalArgumentException ignored) {
      // Callback đã bị hủy đăng ký.
    }
    networkCallback = null;
    currentNetwork = null;
  }

  // Hệ thống báo cả khi chỉ đổi cường độ sóng: chỉ gửi lên JS khi trạng thái thật sự đổi.
  private synchronized void emitIfChanged(WritableMap state) {
    String signature = signature(state);
    if (signature.equals(lastSignature)) return;
    lastSignature = signature;
    if (listenerCount == 0 || !reactContext.hasActiveReactInstance()) return;
    reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(EVENT_STATUS_CHANGE, state);
  }
}
