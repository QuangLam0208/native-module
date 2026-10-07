import ExpoModulesCore
import UIKit

/// What the picker lets the user choose. Raw values are the strings JS sends.
enum DatePickerMode: String, Enumerable {
  case date
  case time
  case datetime

  var uiMode: UIDatePicker.Mode {
    switch self {
    case .date: return .date
    case .time: return .time
    case .datetime: return .dateAndTime
    }
  }

  /// Locale-aware template for the short value shown on the range buttons.
  var summaryTemplate: String {
    switch self {
    case .date: return "ddMMyyyy"
    case .time: return "jm"
    case .datetime: return "ddMMjm"
    }
  }
}

/// Light or dark appearance of the sheet. `system` follows the device setting.
enum DatePickerTheme: String, Enumerable {
  case system
  case light
  case dark

  var interfaceStyle: UIUserInterfaceStyle {
    switch self {
    case .system: return .unspecified
    case .light: return .light
    case .dark: return .dark
    }
  }
}

/// The options object JS passes to `show` and `showRange`. Field names match the JS keys.
/// Dates cross the bridge as milliseconds since 1970, the same unit as JS `Date.getTime()`.
struct DatePickerOptions: Record {
  @Field var mode: DatePickerMode = .date
  /// The selected date, or the start of the range.
  @Field var value: Double?
  /// The end of the range. Only read by `showRange`.
  @Field var endValue: Double?
  @Field var minimumDate: Double?
  @Field var maximumDate: Double?
  /// Step of the minute wheel. Must divide 60.
  @Field var minuteInterval: Int = 1
  @Field var theme: DatePickerTheme = .system
  @Field var title: String?
  @Field var startLabel: String = "From"
  @Field var endLabel: String = "To"
  @Field var confirmText: String = "Done"
  @Field var cancelText: String = "Cancel"
}

extension Date {
  init(milliseconds: Double) {
    self.init(timeIntervalSince1970: milliseconds / 1000)
  }

  var milliseconds: Double {
    (timeIntervalSince1970 * 1000).rounded()
  }
}
