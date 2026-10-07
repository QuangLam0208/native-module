package com.nativedevicehelper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
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

import java.util.HashMap;
import java.util.Map;

public class DeviceHelperModule extends ReactContextBaseJavaModule {

  static final String NAME = "DeviceHelper";
  private static final String EVENT_BATTERY_CHANGE = "onBatteryChange";

  private final ReactApplicationContext reactContext;
  private int listenerCount = 0;

  public DeviceHelperModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @NonNull
  @Override
  public String getName() {
    return NAME;
  }

  // Hằng số đồng bộ — đọc được ngay khi module khởi tạo
  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    Map<String, Object> constants = new HashMap<>();
    constants.put("osVersion", Build.VERSION.RELEASE);
    constants.put("model", Build.MODEL);
    return constants;
  }

  // Đọc thông tin phần cứng (RAM + pin) qua Promise
  @ReactMethod
  public void getHardwareInfo(Promise promise) {
    try {
      ActivityManager am = (ActivityManager) reactContext.getSystemService(Context.ACTIVITY_SERVICE);
      ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
      if (am != null) am.getMemoryInfo(memInfo);

      double totalRamMb   = memInfo.totalMem / (1024.0 * 1024.0);
      double availRamMb   = memInfo.availMem / (1024.0 * 1024.0);

      IntentFilter filter      = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
      Intent batteryStatus     = reactContext.registerReceiver(null, filter);
      int level    = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
      int scale    = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
      int batteryPct = (level >= 0 && scale > 0) ? (level * 100) / scale : -1;
      int status   = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) : -1;
      boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                        || status == BatteryManager.BATTERY_STATUS_FULL;

      WritableMap map = Arguments.createMap();
      map.putDouble("totalRamMb",    Math.round(totalRamMb));
      map.putDouble("availableRamMb", Math.round(availRamMb));
      map.putInt("batteryLevel", batteryPct);
      map.putBoolean("isCharging",   isCharging);
      promise.resolve(map);
    } catch (Exception e) {
      promise.reject("HARDWARE_ERROR", "Lỗi đọc phần cứng: " + e.getMessage(), e);
    }
  }

  // Bắt buộc cho NativeEventEmitter — theo dõi số lượng listener
  @ReactMethod
  public void addListener(String eventName) {
    listenerCount++;
  }

  @ReactMethod
  public void removeListeners(Integer count) {
    listenerCount = Math.max(0, listenerCount - count);
  }

  // Gửi sự kiện pin lên JS (gọi khi BroadcastReceiver nhận ACTION_BATTERY_CHANGED)
  void emitBatteryChange(int batteryLevel, boolean isCharging) {
    if (listenerCount == 0) return;
    WritableMap data = Arguments.createMap();
    data.putInt("batteryLevel", batteryLevel);
    data.putBoolean("isCharging", isCharging);
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(EVENT_BATTERY_CHANGE, data);
  }
}
