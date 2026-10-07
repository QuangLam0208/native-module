package com.nativetoast;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;

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
   * Hiển thị Android Toast.
   *
   * @param message    Nội dung cần hiển thị.
   * @param type       Một trong: "normal", "success", "warning", "error".
   *                   Android Toast không phân biệt loại bằng màu — tham số này
   *                   nhận vào để giữ cùng API với iOS, nhưng hiện tại bỏ qua.
   * @param durationMs Thời lượng tính bằng mili giây.
   *                   ≤ 2 500 → Toast.LENGTH_SHORT, > 2 500 → Toast.LENGTH_LONG.
   */
  @ReactMethod
  public void show(String message, String type, double durationMs) {
    int toastLength = durationMs > 2500 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
    UiThreadUtil.runOnUiThread(() ->
      Toast.makeText(reactContext, message, toastLength).show()
    );
  }
}
