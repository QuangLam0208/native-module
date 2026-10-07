import UIKit

/// The button the user tapped. Raw values are the strings sent to JS.
enum AlertButton: String {
  case confirm
  case neutral
  case cancel
}

/// Builds and presents the alert. Call from the main thread.
enum AlertPresenter {
  static func present(
    title: String?, message: String, confirmText: String, neutralText: String?, cancelText: String,
    from presenter: UIViewController, onSelect: @escaping (AlertButton) -> Void
  ) {
    // An empty title would still reserve its row, so it is dropped like a missing one.
    let shownTitle = title?.isEmpty == false ? title : nil
    let alert = UIAlertController(title: shownTitle, message: message, preferredStyle: .alert)

    let confirm = UIAlertAction(title: confirmText, style: .default) { _ in onSelect(.confirm) }
    alert.addAction(confirm)
    // Shown in bold and triggered by the Return key on a hardware keyboard.
    alert.preferredAction = confirm

    // With a third button iOS stacks the buttons vertically, in the order added.
    if let neutralText, !neutralText.isEmpty {
      alert.addAction(UIAlertAction(title: neutralText, style: .default) { _ in onSelect(.neutral) })
    }

    // The cancel style always goes last: on the left of two buttons, at the bottom of three.
    alert.addAction(UIAlertAction(title: cancelText, style: .cancel) { _ in onSelect(.cancel) })

    presenter.present(alert, animated: true)
  }

  /// The view controller currently on top, so the alert also shows above modals.
  static func topViewController() -> UIViewController? {
    let keyWindow = UIApplication.shared.connectedScenes
      .compactMap { $0 as? UIWindowScene }
      .flatMap { $0.windows }
      .first { $0.isKeyWindow }

    var top = keyWindow?.rootViewController
    while let presented = top?.presentedViewController {
      top = presented
    }
    return top
  }
}
