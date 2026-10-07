import ExpoModulesCore
import UIKit

/// Mirrors the app's JS toast types (app/utils/Toast.ts) and its Toasty-style palette.
enum ToastType: String, Enumerable {
  case normal
  case success
  case warning
  case error

  var backgroundColor: UIColor {
    switch self {
    case .normal: return UIColor(red: 0x35 / 255, green: 0x3A / 255, blue: 0x3E / 255, alpha: 1)
    case .success: return UIColor(red: 0x38 / 255, green: 0x8E / 255, blue: 0x3C / 255, alpha: 1)
    case .warning: return UIColor(red: 0xFF / 255, green: 0xA9 / 255, blue: 0x00 / 255, alpha: 1)
    case .error: return UIColor(red: 0xD5 / 255, green: 0x00 / 255, blue: 0x00 / 255, alpha: 1)
    }
  }
}
