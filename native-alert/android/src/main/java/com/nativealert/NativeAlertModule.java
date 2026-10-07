package com.nativealert;

import android.app.Activity;
import android.app.AlertDialog;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;

public class NativeAlertModule extends ReactContextBaseJavaModule {

  static final String NAME = "NativeAlert";

  public NativeAlertModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @NonNull
  @Override
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void show(
      String title,
      String message,
      String confirmText,
      String neutralText,
      String cancelText,
      Promise promise) {
    UiThreadUtil.runOnUiThread(() -> {
      Activity activity = getCurrentActivity();
      if (activity == null || activity.isFinishing()) {
        promise.reject("NO_ACTIVITY", "Current activity is not available to present the alert");
        return;
      }

      AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      if (title != null && !title.isEmpty()) {
        builder.setTitle(title);
      }
      builder.setMessage(message);

      builder.setPositiveButton(confirmText, (dialog, which) -> {
        promise.resolve("confirm");
      });

      if (neutralText != null && !neutralText.isEmpty()) {
        builder.setNeutralButton(neutralText, (dialog, which) -> {
          promise.resolve("neutral");
        });
      }

      builder.setNegativeButton(cancelText, (dialog, which) -> {
        promise.resolve("cancel");
      });

      builder.setOnCancelListener(dialog -> {
        promise.resolve("cancel");
      });

      builder.show();
    });
  }
}
