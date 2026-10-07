import ExpoModulesCore
import UIKit

public class NativeAlertModule: Module {
  public func definition() -> ModuleDefinition {
    Name("NativeAlert")

    // Resolves with "confirm", "neutral" or "cancel" once the user taps a button.
    // `title` and `neutralText` are optional: nil or empty leaves that part out.
    // UIKit work, so it runs on the main queue.
    AsyncFunction("show") { (title: String?, message: String, confirmText: String, neutralText: String?, cancelText: String, promise: Promise) in
      guard let presenter = AlertPresenter.topViewController() else {
        promise.reject(Exception(name: "NO_VIEW_CONTROLLER", description: "No view controller to present the alert on"))
        return
      }
      AlertPresenter.present(
        title: title, message: message, confirmText: confirmText, neutralText: neutralText,
        cancelText: cancelText, from: presenter
      ) { button in
        promise.resolve(button.rawValue)
      }
    }
    .runOnQueue(.main)
  }
}
