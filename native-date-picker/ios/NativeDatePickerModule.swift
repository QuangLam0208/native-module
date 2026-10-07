import ExpoModulesCore
import UIKit

public class NativeDatePickerModule: Module {
  public func definition() -> ModuleDefinition {
    Name("NativeDatePicker")

    // Resolves with the confirmed date in milliseconds, or nil when the picker is cancelled.
    // UIKit work, so it runs on the main queue.
    AsyncFunction("show") { (options: DatePickerOptions, promise: Promise) in
      Self.present(options, isRange: false, promise: promise) { selection in
        selection.start.milliseconds
      }
    }
    .runOnQueue(.main)

    // Resolves with { start, end } in milliseconds, or nil when the picker is cancelled.
    AsyncFunction("showRange") { (options: DatePickerOptions, promise: Promise) in
      Self.present(options, isRange: true, promise: promise) { selection in
        ["start": selection.start.milliseconds, "end": selection.end.milliseconds]
      }
    }
    .runOnQueue(.main)
  }

  private static func present(
    _ options: DatePickerOptions, isRange: Bool, promise: Promise,
    result: @escaping (DatePickerSelection) -> Any
  ) {
    if let minimum = options.minimumDate, let maximum = options.maximumDate, minimum > maximum {
      promise.reject(Exception(name: "INVALID_RANGE", description: "minimumDate must not be after maximumDate"))
      return
    }
    // UIDatePicker silently ignores an interval that does not divide 60.
    guard (1...30).contains(options.minuteInterval), 60 % options.minuteInterval == 0 else {
      promise.reject(Exception(name: "INVALID_MINUTE_INTERVAL", description: "minuteInterval must divide 60 and be at most 30"))
      return
    }
    guard let presenter = DatePickerSheet.topViewController() else {
      promise.reject(Exception(name: "NO_VIEW_CONTROLLER", description: "No view controller to present the picker on"))
      return
    }
    DatePickerSheet.present(options, isRange: isRange, from: presenter) { selection in
      promise.resolve(selection.map(result))
    }
  }
}
