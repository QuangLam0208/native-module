import ExpoModulesCore
import UIKit

public class NativeToastModule: Module {
  public func definition() -> ModuleDefinition {
    Name("NativeToast")

    // UIKit work, so it runs on the main queue. Rejects when no window is on screen.
    AsyncFunction("show") { (message: String, type: ToastType, durationMs: Double) in
      guard durationMs > 0 else {
        throw Exception(name: "INVALID_DURATION", description: "durationMs must be greater than 0")
      }
      guard let window = Self.keyWindow() else {
        throw Exception(name: "NO_WINDOW", description: "No key window to present the toast on")
      }
      ToastView.present(message: message, type: type, duration: durationMs / 1000, in: window)
    }
    .runOnQueue(.main)
  }

  private static func keyWindow() -> UIWindow? {
    UIApplication.shared.connectedScenes
      .compactMap { $0 as? UIWindowScene }
      .flatMap { $0.windows }
      .first { $0.isKeyWindow }
  }
}
