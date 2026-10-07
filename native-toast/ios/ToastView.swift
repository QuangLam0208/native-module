import UIKit

/// Short tinted bar near the bottom of the window, non-blocking — same look as the JS ToastHost.
final class ToastView: UIView {
  private static let tagId = 0x70A57

  static func present(message: String, type: ToastType, duration: TimeInterval, in window: UIWindow) {
    // Only one toast at a time: a new one replaces the one still on screen.
    window.viewWithTag(tagId)?.removeFromSuperview()

    let toast = ToastView(message: message, type: type)
    toast.tag = tagId
    toast.alpha = 0
    window.addSubview(toast)

    NSLayoutConstraint.activate([
      toast.centerXAnchor.constraint(equalTo: window.centerXAnchor),
      toast.bottomAnchor.constraint(equalTo: window.safeAreaLayoutGuide.bottomAnchor, constant: -24),
      toast.widthAnchor.constraint(lessThanOrEqualTo: window.widthAnchor, multiplier: 0.9),
      toast.widthAnchor.constraint(greaterThanOrEqualToConstant: 120),
    ])

    UIView.animate(withDuration: 0.18) { toast.alpha = 1 }
    UIView.animate(
      withDuration: 0.18, delay: duration, options: [.curveEaseIn],
      animations: { toast.alpha = 0 },
      completion: { _ in toast.removeFromSuperview() }
    )
  }

  private init(message: String, type: ToastType) {
    super.init(frame: .zero)
    translatesAutoresizingMaskIntoConstraints = false
    backgroundColor = type.backgroundColor
    layer.cornerRadius = 8
    isUserInteractionEnabled = false

    let label = UILabel()
    label.translatesAutoresizingMaskIntoConstraints = false
    label.text = message
    label.textColor = .white
    label.font = .systemFont(ofSize: 14)
    label.numberOfLines = 4
    label.textAlignment = .center
    addSubview(label)

    NSLayoutConstraint.activate([
      label.topAnchor.constraint(equalTo: topAnchor, constant: 12),
      label.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -12),
      label.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 16),
      label.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -16),
    ])
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}
