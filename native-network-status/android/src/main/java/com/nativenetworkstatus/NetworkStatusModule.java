package com.nativenetworkstatus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;

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
  private ConnectivityManager.NetworkCallback networkCallback;
  private int listenerCount = 0;

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

  private WritableMap getCurrentNetworkState() {
    WritableMap state = Arguments.createMap();
    if (connectivityManager == null) {
      state.putBoolean("isConnected", false);
      state.putString("type", "none");
      state.putBoolean("isExpensive", false);
      state.putBoolean("isConstrained", false);
      return state;
    }

    Network activeNetwork = connectivityManager.getActiveNetwork();
    if (activeNetwork == null) {
      state.putBoolean("isConnected", false);
      state.putString("type", "none");
      state.putBoolean("isExpensive", false);
      state.putBoolean("isConstrained", false);
      return state;
    }

    NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(activeNetwork);
    if (caps == null) {
      state.putBoolean("isConnected", false);
      state.putString("type", "none");
      state.putBoolean("isExpensive", false);
      state.putBoolean("isConstrained", false);
      return state;
    }

    boolean isConnected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    String type = "other";
    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
      type = "wifi";
    } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
      type = "cellular";
    } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
      type = "ethernet";
    }

    boolean isExpensive = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
    boolean isConstrained = false;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      isConstrained = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED);
    }

    state.putBoolean("isConnected", isConnected);
    state.putString("type", type);
    state.putBoolean("isExpensive", isExpensive);
    state.putBoolean("isConstrained", isConstrained);
    return state;
  }

  @ReactMethod
  public void getStatus(Promise promise) {
    try {
      promise.resolve(getCurrentNetworkState());
    } catch (Exception e) {
      promise.reject("NETWORK_ERROR", e.getMessage(), e);
    }
  }

  @ReactMethod
  public void addListener(String eventName) {
    listenerCount++;
    if (listenerCount == 1 && networkCallback == null && connectivityManager != null) {
      networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
          emitStatusChange();
        }

        @Override
        public void onLost(@NonNull Network network) {
          emitStatusChange();
        }

        @Override
        public void onAvailable(@NonNull Network network) {
          emitStatusChange();
        }
      };
      NetworkRequest request = new NetworkRequest.Builder().build();
      connectivityManager.registerNetworkCallback(request, networkCallback);
    }
  }

  @ReactMethod
  public void removeListeners(Integer count) {
    listenerCount = Math.max(0, listenerCount - count);
    if (listenerCount == 0 && networkCallback != null && connectivityManager != null) {
      try {
        connectivityManager.unregisterNetworkCallback(networkCallback);
      } catch (Exception ignored) {}
      networkCallback = null;
    }
  }

  private void emitStatusChange() {
    if (listenerCount == 0) return;
    reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(EVENT_STATUS_CHANGE, getCurrentNetworkState());
  }
}
