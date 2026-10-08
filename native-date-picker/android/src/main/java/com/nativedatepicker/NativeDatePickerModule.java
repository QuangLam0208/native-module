package com.nativedatepicker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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

  private interface OnPicked {
    void onPicked(Calendar picked);
  }

  /** Chọn một thời điểm theo `mode`: "date", "time" hoặc "datetime". */
  @ReactMethod
  public void show(ReadableMap options, Promise promise) {
    UiThreadUtil.runOnUiThread(() -> {
      Activity activity = presentableActivity(options, promise);
      if (activity == null) return;

      Calendar initial = calendarOf(options, "value", System.currentTimeMillis());
      pick(activity, options, null, initial, Long.MIN_VALUE,
          picked -> promise.resolve((double) picked.getTimeInMillis()),
          () -> promise.resolve(null));
    });
  }

  /** Chọn một khoảng (bắt đầu rồi kết thúc) theo `mode`. Điểm kết thúc không bao giờ nhỏ hơn điểm bắt đầu. */
  @ReactMethod
  public void showRange(ReadableMap options, Promise promise) {
    UiThreadUtil.runOnUiThread(() -> {
      Activity activity = presentableActivity(options, promise);
      if (activity == null) return;

      String startLabel = stringOr(options, "startLabel", "From");
      String endLabel = stringOr(options, "endLabel", "To");
      Calendar startInitial = calendarOf(options, "value", System.currentTimeMillis());
      Runnable cancel = () -> promise.resolve(null);

      if ("date".equals(stringOr(options, "mode", "date")) && activity instanceof FragmentActivity) {
        showMaterialRange((FragmentActivity) activity, options, startInitial, promise);
        return;
      }

      pick(activity, options, startLabel, startInitial, Long.MIN_VALUE, start -> {
        Calendar endInitial = calendarOf(options, "endValue", start.getTimeInMillis());
        if (endInitial.before(start)) endInitial = (Calendar) start.clone();

        pick(activity, options, endLabel, endInitial, start.getTimeInMillis(), end -> {
          WritableMap result = Arguments.createMap();
          result.putDouble("start", (double) start.getTimeInMillis());
          result.putDouble("end", (double) end.getTimeInMillis());
          promise.resolve(result);
        }, cancel);
      }, cancel);
    });
  }

  /** Chọn cả khoảng ngày trong một hộp thoại duy nhất (Material). Chế độ giờ và ngày giờ vẫn chọn lần lượt. */
  private void showMaterialRange(FragmentActivity activity, ReadableMap options, Calendar startInitial, Promise promise) {
    Calendar endInitial = calendarOf(options, "endValue", startInitial.getTimeInMillis());
    if (endInitial.before(startInitial)) endInitial = (Calendar) startInitial.clone();
    final Calendar endBase = endInitial;

    CalendarConstraints.Builder constraints = new CalendarConstraints.Builder();
    List<CalendarConstraints.DateValidator> validators = new ArrayList<>();
    if (hasNumber(options, "minimumDate")) {
      long min = utcMidnight((long) options.getDouble("minimumDate"));
      constraints.setStart(min);
      validators.add(DateValidatorPointForward.from(min));
    }
    if (hasNumber(options, "maximumDate")) {
      long max = utcMidnight((long) options.getDouble("maximumDate"));
      constraints.setEnd(max);
      validators.add(DateValidatorPointBackward.before(max + 24L * 60 * 60 * 1000 - 1));
    }
    if (!validators.isEmpty()) constraints.setValidator(CompositeDateValidator.allOf(validators));

    MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker()
        .setTheme(R.style.NativeDatePicker_Calendar)
        .setCalendarConstraints(constraints.build())
        .setSelection(new Pair<>(utcMidnight(startInitial.getTimeInMillis()), utcMidnight(endBase.getTimeInMillis())));
    String title = stringOr(options, "title", null);
    String confirm = stringOr(options, "confirmText", null);
    String cancel = stringOr(options, "cancelText", null);
    if (title != null) builder.setTitleText(title);
    if (confirm != null) builder.setPositiveButtonText(confirm);
    if (cancel != null) builder.setNegativeButtonText(cancel);

    MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
    final boolean[] done = {false};
    picker.addOnPositiveButtonClickListener(selection -> {
      if (done[0]) return;
      done[0] = true;
      WritableMap result = Arguments.createMap();
      result.putDouble("start", (double) withLocalDate(startInitial, selection.first));
      result.putDouble("end", (double) withLocalDate(endBase, selection.second));
      promise.resolve(result);
    });
    picker.addOnDismissListener(d -> {
      if (done[0]) return;
      done[0] = true;
      promise.resolve(null);
    });
    picker.show(activity.getSupportFragmentManager(), "NativeDatePickerRange");
  }

  /** Material làm việc với nửa đêm UTC của ngày; đổi từ ngày theo múi giờ máy. */
  private static long utcMidnight(long localMillis) {
    Calendar local = Calendar.getInstance();
    local.setTimeInMillis(localMillis);
    Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    utc.clear();
    utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH));
    return utc.getTimeInMillis();
  }

  /** Giữ giờ phút của `base`, đổi sang ngày mà Material trả về (UTC). */
  private static long withLocalDate(Calendar base, long utcMillis) {
    Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    utc.setTimeInMillis(utcMillis);
    Calendar result = (Calendar) base.clone();
    result.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH));
    return result.getTimeInMillis();
  }

  // --- Kiểm tra tham số ---

  /** Trả về Activity để hiển thị, hoặc null sau khi đã reject Promise. Giữ cùng mã lỗi với iOS khi tham số sai. */
  @Nullable
  private Activity presentableActivity(ReadableMap options, Promise promise) {
    if (hasNumber(options, "minimumDate") && hasNumber(options, "maximumDate")
        && options.getDouble("minimumDate") > options.getDouble("maximumDate")) {
      promise.reject("INVALID_RANGE", "minimumDate must not be after maximumDate");
      return null;
    }
    if (hasNumber(options, "minuteInterval")) {
      int interval = (int) options.getDouble("minuteInterval");
      if (interval < 1 || interval > 30 || 60 % interval != 0) {
        promise.reject("INVALID_MINUTE_INTERVAL", "minuteInterval must divide 60 and be at most 30");
        return null;
      }
    }
    Activity activity = getCurrentActivity();
    if (activity == null || activity.isFinishing()) {
      promise.reject("NO_ACTIVITY", "Activity hiện tại không khả dụng.");
      return null;
    }
    return activity;
  }

  // --- Dựng picker ---

  private void pick(Activity activity, ReadableMap options, @Nullable String label, Calendar initial,
                    long notBefore, OnPicked onPicked, Runnable onCancel) {
    String mode = stringOr(options, "mode", "date");
    if ("time".equals(mode)) {
      pickTime(activity, options, label, initial, notBefore, onPicked, onCancel);
    } else if ("datetime".equals(mode)) {
      pickDate(activity, options, label, initial, notBefore,
          date -> pickTime(activity, options, label, date, notBefore, onPicked, onCancel), onCancel);
    } else {
      pickDate(activity, options, label, initial, notBefore, onPicked, onCancel);
    }
  }

  private void pickDate(Activity activity, ReadableMap options, @Nullable String label, Calendar initial,
                        long notBefore, OnPicked onPicked, Runnable onCancel) {
    final boolean[] done = {false};

    DatePickerDialog dialog = new DatePickerDialog(activity, (view, year, month, day) -> {
      if (done[0]) return;
      done[0] = true;
      Calendar result = (Calendar) initial.clone();
      result.set(year, month, day);
      if (result.getTimeInMillis() < notBefore) result.setTimeInMillis(notBefore);
      onPicked.onPicked(result);
    }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH));

    long min = hasNumber(options, "minimumDate") ? (long) options.getDouble("minimumDate") : Long.MIN_VALUE;
    long lowest = Math.max(min, notBefore);
    if (lowest != Long.MIN_VALUE) dialog.getDatePicker().setMinDate(lowest);
    if (hasNumber(options, "maximumDate")) dialog.getDatePicker().setMaxDate((long) options.getDouble("maximumDate"));

    decorate(dialog, options, label);
    dialog.setOnCancelListener(d -> {
      if (done[0]) return;
      done[0] = true;
      onCancel.run();
    });
    dialog.show();
  }

  private void pickTime(Activity activity, ReadableMap options, @Nullable String label, Calendar initial,
                        long notBefore, OnPicked onPicked, Runnable onCancel) {
    final boolean[] done = {false};

    TimePickerDialog dialog = new TimePickerDialog(activity, (view, hour, minute) -> {
      if (done[0]) return;
      done[0] = true;
      Calendar result = (Calendar) initial.clone();
      result.set(Calendar.HOUR_OF_DAY, hour);
      result.set(Calendar.MINUTE, minute);
      result.set(Calendar.SECOND, 0);
      result.set(Calendar.MILLISECOND, 0);
      if (result.getTimeInMillis() < notBefore) result.setTimeInMillis(notBefore);
      onPicked.onPicked(result);
    }, initial.get(Calendar.HOUR_OF_DAY), initial.get(Calendar.MINUTE), true);

    decorate(dialog, options, label);
    dialog.setOnCancelListener(d -> {
      if (done[0]) return;
      done[0] = true;
      onCancel.run();
    });
    dialog.show();
  }

  /** Áp tiêu đề và chữ trên hai nút. Chữ nút được đặt khi hộp thoại hiện để không thay mất xử lý có sẵn của nút. */
  private void decorate(android.app.AlertDialog dialog, ReadableMap options, @Nullable String label) {
    String title = stringOr(options, "title", null);
    String shown = title != null && label != null ? title + " · " + label : (title != null ? title : label);
    if (shown != null) dialog.setTitle(shown);

    String confirm = stringOr(options, "confirmText", null);
    String cancel = stringOr(options, "cancelText", null);
    dialog.setOnShowListener(d -> {
      if (confirm != null) dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(confirm);
      if (cancel != null) dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText(cancel);
    });
  }

  // --- Đọc tham số ---

  private static boolean hasNumber(ReadableMap map, String key) {
    return map.hasKey(key) && !map.isNull(key);
  }

  @Nullable
  private static String stringOr(ReadableMap map, String key, @Nullable String fallback) {
    if (!map.hasKey(key) || map.isNull(key)) return fallback;
    String value = map.getString(key);
    return value == null || value.isEmpty() ? fallback : value;
  }

  private static Calendar calendarOf(ReadableMap map, String key, long fallbackMillis) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(hasNumber(map, key) ? (long) map.getDouble(key) : fallbackMillis);
    return calendar;
  }
}
