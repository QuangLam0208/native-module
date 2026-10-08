package com.nativetoast;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;

import es.dmoral.toasty.Toasty;

public class NativeToastModule extends ReactContextBaseJavaModule {

  static final String NAME = "NativeToast";

  private final ReactApplicationContext reactContext;

  public NativeToastModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @NonNull
  @Override
  public String getName() {
    return NAME;
  }

  /**
   * Hiển thị toast bằng thư viện Toasty (es.dmoral.toasty), cùng bảng màu với iOS.
   *
   * @param message    Nội dung cần hiển thị.
   * @param type       Một trong: "normal", "success", "warning", "error".
   *                   Giá trị khác được xử lý như "normal".
   * @param durationMs Thời lượng tính bằng mili giây. Toasty chỉ có hai mức của hệ thống:
   *                   ≤ 2 500 → Toast.LENGTH_SHORT, > 2 500 → Toast.LENGTH_LONG.
   */
  @ReactMethod
  public void show(String message, String type, double durationMs) {
    int duration = durationMs > 2500 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
    UiThreadUtil.runOnUiThread(() -> {
      switch (type == null ? "normal" : type) {
        case "success":
          Toasty.success(reactContext, message, duration).show();
          break;
        case "warning":
          Toasty.warning(reactContext, message, duration).show();
          break;
        case "error":
          Toasty.error(reactContext, message, duration).show();
          break;
        default:
          Toasty.normal(reactContext, message, duration).show();
          break;
      }
    });
  }
}
