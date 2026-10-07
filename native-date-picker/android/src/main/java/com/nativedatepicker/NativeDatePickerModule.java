package com.nativedatepicker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;

import java.util.Calendar;

public class NativeDatePickerModule extends ReactContextBaseJavaModule {

  static final String NAME = "NativeDatePicker";

  public NativeDatePickerModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @NonNull
  @Override
  public String getName() {
    return NAME;
  }

  /**
   * Hiển thị picker cho một thời điểm (date, time, hoặc datetime).
   */
  @ReactMethod
  public void show(ReadableMap options, Promise promise) {
    UiThreadUtil.runOnUiThread(() -> {
      Activity activity = getCurrentActivity();
      if (activity == null || activity.isFinishing()) {
        promise.reject("NO_ACTIVITY", "Activity hiện tại không khả dụng.");
        return;
      }

      String mode = options.hasKey("mode") ? options.getString("mode") : "date";
      long initialMillis = options.hasKey("value") && !options.isNull("value")
          ? (long) options.getDouble("value")
          : System.currentTimeMillis();

      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(initialMillis);

      if ("time".equalsIgnoreCase(mode)) {
        showTimePicker(activity, calendar, promise);
      } else if ("datetime".equalsIgnoreCase(mode)) {
        // Với datetime: chọn ngày trước, chọn xong mở tiếp giờ
        showDatePicker(activity, options, calendar, selectedDateCal -> {
          showTimePicker(activity, selectedDateCal, promise);
        }, () -> promise.resolve(null));
      } else {
        // Mặc định là mode "date"
        showDatePicker(activity, options, calendar, selectedDateCal -> {
          promise.resolve((double) selectedDateCal.getTimeInMillis());
        }, () -> promise.resolve(null));
      }
    });
  }

  /**
   * Hiển thị chọn khoảng thời gian (Từ ngày -> Đến ngày).
   */
  @ReactMethod
  public void showRange(ReadableMap options, Promise promise) {
    UiThreadUtil.runOnUiThread(() -> {
      Activity activity = getCurrentActivity();
      if (activity == null || activity.isFinishing()) {
        promise.reject("NO_ACTIVITY", "Activity hiện tại không khả dụng.");
        return;
      }

      long startInitial = options.hasKey("value") && !options.isNull("value")
          ? (long) options.getDouble("value")
          : System.currentTimeMillis();

      Calendar startCal = Calendar.getInstance();
      startCal.setTimeInMillis(startInitial);

      // Bước 1: Chọn Start Date
      showDatePicker(activity, options, startCal, selectedStartCal -> {
        // Bước 2: Chọn End Date (có minDate = startDate)
        long endInitial = options.hasKey("endValue") && !options.isNull("endValue")
            ? (long) options.getDouble("endValue")
            : selectedStartCal.getTimeInMillis();

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(endInitial);

        showDatePicker(activity, options, endCal, selectedEndCal -> {
          WritableMap result = Arguments.createMap();
          result.putDouble("start", (double) selectedStartCal.getTimeInMillis());
          result.putDouble("end", (double) selectedEndCal.getTimeInMillis());
          promise.resolve(result);
        }, () -> promise.resolve(null));
      }, () -> promise.resolve(null));
    });
  }

  // --- Helper Methods ---

  private interface OnDateSelectedListener {
    void onSelected(Calendar calendar);
  }

  private void showDatePicker(
      Activity activity,
      ReadableMap options,
      Calendar initialCal,
      OnDateSelectedListener onSelected,
      Runnable onCancel) {

    final boolean[] isHandled = {false};

    DatePickerDialog dialog = new DatePickerDialog(
        activity,
        (view, year, month, dayOfMonth) -> {
          if (!isHandled[0]) {
            isHandled[0] = true;
            Calendar result = Calendar.getInstance();
            result.setTimeInMillis(initialCal.getTimeInMillis());
            result.set(Calendar.YEAR, year);
            result.set(Calendar.MONTH, month);
            result.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            onSelected.onSelected(result);
          }
        },
        initialCal.get(Calendar.YEAR),
        initialCal.get(Calendar.MONTH),
        initialCal.get(Calendar.DAY_OF_MONTH)
    );

    // Áp dụng minimumDate và maximumDate nếu có
    if (options.hasKey("minimumDate") && !options.isNull("minimumDate")) {
      dialog.getDatePicker().setMinDate((long) options.getDouble("minimumDate"));
    }
    if (options.hasKey("maximumDate") && !options.isNull("maximumDate")) {
      dialog.getDatePicker().setMaxDate((long) options.getDouble("maximumDate"));
    }

    dialog.setOnCancelListener(d -> {
      if (!isHandled[0]) {
        isHandled[0] = true;
        onCancel.run();
      }
    });

    dialog.show();
  }

  private void showTimePicker(Activity activity, Calendar initialCal, Promise promise) {
    final boolean[] isHandled = {false};

    TimePickerDialog dialog = new TimePickerDialog(
        activity,
        (view, hourOfDay, minute) -> {
          if (!isHandled[0]) {
            isHandled[0] = true;
            Calendar result = Calendar.getInstance();
            result.setTimeInMillis(initialCal.getTimeInMillis());
            result.set(Calendar.HOUR_OF_DAY, hourOfDay);
            result.set(Calendar.MINUTE, minute);
            result.set(Calendar.SECOND, 0);
            promise.resolve((double) result.getTimeInMillis());
          }
        },
        initialCal.get(Calendar.HOUR_OF_DAY),
        initialCal.get(Calendar.MINUTE),
        true // 24h view
    );

    dialog.setOnCancelListener(d -> {
      if (!isHandled[0]) {
        isHandled[0] = true;
        promise.resolve(null);
      }
    });

    dialog.show();
  }
}