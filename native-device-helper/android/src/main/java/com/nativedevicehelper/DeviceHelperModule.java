package com.nativedevicehelper;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
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

  // Theo dõi pin chỉ chạy khi có ít nhất một listener phía JS (giống iOS).
  @Nullable private BroadcastReceiver batteryReceiver;
  private int lastBatteryLevel = Integer.MIN_VALUE;
  private boolean lastIsCharging = false;

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

      // ACTION_BATTERY_CHANGED là broadcast "sticky": đăng ký với receiver null để đọc trạng thái gần nhất.
      Intent batteryStatus = reactContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

      WritableMap map = Arguments.createMap();
      map.putDouble("totalRamMb",    Math.round(totalRamMb));
      map.putDouble("availableRamMb", Math.round(availRamMb));
      map.putInt("batteryLevel", batteryLevel(batteryStatus));
      map.putBoolean("isCharging", isCharging(batteryStatus));
      promise.resolve(map);
    } catch (Exception e) {
      promise.reject("HARDWARE_ERROR", "Lỗi đọc phần cứng: " + e.getMessage(), e);
    }
  }

  // Bắt buộc cho NativeEventEmitter — theo dõi số lượng listener
  @ReactMethod
  public void addListener(String eventName) {
    listenerCount++;
    if (listenerCount == 1) startObservingBattery();
  }

  @ReactMethod
  public void removeListeners(Integer count) {
    listenerCount = Math.max(0, listenerCount - count);
    if (listenerCount == 0) stopObservingBattery();
  }

  @Override
  public void invalidate() {
    stopObservingBattery();
    super.invalidate();
  }

  private synchronized void startObservingBattery() {
    if (batteryReceiver != null) return;

    batteryReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        emitBatteryChangeIfNeeded(intent);
      }
    };

    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    Intent sticky;
    if (Build.VERSION.SDK_INT >= 33) {
      sticky = reactContext.registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    } else {
      sticky = reactContext.registerReceiver(batteryReceiver, filter);
    }

    // Ghi nhớ trạng thái hiện tại: chỉ báo khi có thay đổi so với lúc bắt đầu theo dõi.
    lastBatteryLevel = batteryLevel(sticky);
    lastIsCharging = isCharging(sticky);
  }

  private synchronized void stopObservingBattery() {
    if (batteryReceiver == null) return;
    try {
      reactContext.unregisterReceiver(batteryReceiver);
    } catch (IllegalArgumentException ignored) {
      // Receiver đã bị hủy đăng ký.
    }
    batteryReceiver = null;
  }

  // Hệ thống phát ACTION_BATTERY_CHANGED cả khi chỉ đổi nhiệt độ hay điện áp:
  // chỉ gửi lên JS khi mức pin hoặc trạng thái sạc thật sự đổi.
  private synchronized void emitBatteryChangeIfNeeded(Intent intent) {
    int level = batteryLevel(intent);
    boolean charging = isCharging(intent);
    if (level == lastBatteryLevel && charging == lastIsCharging) return;
    lastBatteryLevel = level;
    lastIsCharging = charging;
    emitBatteryChange(level, charging);
  }

  // Gửi sự kiện pin lên JS
  void emitBatteryChange(int batteryLevel, boolean isCharging) {
    if (listenerCount == 0 || !reactContext.hasActiveReactInstance()) return;
    WritableMap data = Arguments.createMap();
    data.putInt("batteryLevel", batteryLevel);
    data.putBoolean("isCharging", isCharging);
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(EVENT_BATTERY_CHANGE, data);
  }

  // Phần trăm pin từ 0 đến 100, hoặc -1 khi không đọc được.
  private static int batteryLevel(@Nullable Intent batteryStatus) {
    if (batteryStatus == null) return -1;
    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    return (level >= 0 && scale > 0) ? (level * 100) / scale : -1;
  }

  // Đang sạc hoặc đã sạc đầy — cùng định nghĩa với iOS.
  private static boolean isCharging(@Nullable Intent batteryStatus) {
    if (batteryStatus == null) return false;
    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
    return status == BatteryManager.BATTERY_STATUS_CHARGING
        || status == BatteryManager.BATTERY_STATUS_FULL;
  }
}
