import UIKit

/// What the user confirmed. For a single date `end` equals `start`.
struct DatePickerSelection {
  var start: Date
  var end: Date
}

/// A bottom sheet holding a wheel-style UIDatePicker with cancel and confirm buttons.
/// In range mode two buttons above the wheels switch between editing the start and the end.
final class DatePickerSheet: UIViewController, UIAdaptivePresentationControllerDelegate {
  private let options: DatePickerOptions
  private let isRange: Bool
  private let onFinish: (DatePickerSelection?) -> Void

  private var selection: DatePickerSelection
  private var isEditingEnd = false
  // Guards against reporting twice, e.g. a button tap followed by the dismissal callback.
  private var hasFinished = false

  private let picker = UIDatePicker()
  private let startButton = UIButton(type: .system)
  private let endButton = UIButton(type: .system)
  private lazy var summaryFormatter: DateFormatter = {
    let formatter = DateFormatter()
    formatter.setLocalizedDateFormatFromTemplate(options.mode.summaryTemplate)
    return formatter
  }()

  /// `onFinish` receives the confirmed selection, or nil for cancel. Call from the main thread.
  static func present(
    _ options: DatePickerOptions, isRange: Bool, from presenter: UIViewController,
    onFinish: @escaping (DatePickerSelection?) -> Void
  ) {
    let sheet = DatePickerSheet(options: options, isRange: isRange, onFinish: onFinish)
    let navigation = UINavigationController(rootViewController: sheet)
    navigation.overrideUserInterfaceStyle = options.theme.interfaceStyle
    navigation.sheetPresentationController?.detents = [.medium()]
    // The sheet's own backdrop belongs to the presentation controller, not to `navigation`,
    // so it needs the theme too or it keeps the device appearance behind themed content.
    if #available(iOS 17.0, *) {
      navigation.sheetPresentationController?.traitOverrides.userInterfaceStyle = options.theme.interfaceStyle
    }
    // Swiping the sheet down counts as cancel.
    navigation.presentationController?.delegate = sheet
    presenter.present(navigation, animated: true)
  }

  private init(options: DatePickerOptions, isRange: Bool, onFinish: @escaping (DatePickerSelection?) -> Void) {
    self.options = options
    self.isRange = isRange
    self.onFinish = onFinish

    let start = options.value.map(Date.init(milliseconds:)) ?? Date()
    let end = options.endValue.map(Date.init(milliseconds:)) ?? start
    // An end before the start is pulled up to the start rather than rejected.
    selection = DatePickerSelection(start: start, end: max(start, end))
    super.init(nibName: nil, bundle: nil)
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override func viewDidLoad() {
    super.viewDidLoad()
    view.backgroundColor = .systemBackground

    navigationItem.title = options.title
    navigationItem.leftBarButtonItem = UIBarButtonItem(
      title: options.cancelText, style: .plain, target: self, action: #selector(cancel))
    navigationItem.rightBarButtonItem = UIBarButtonItem(
      title: options.confirmText, style: .done, target: self, action: #selector(confirm))

    picker.datePickerMode = options.mode.uiMode
    picker.preferredDatePickerStyle = .wheels
    picker.minuteInterval = options.minuteInterval
    picker.addTarget(self, action: #selector(pickerChanged), for: .valueChanged)

    let content = UIStackView(arrangedSubviews: isRange ? [makeRangeButtons(), picker] : [picker])
    content.axis = .vertical
    content.spacing = 8
    content.translatesAutoresizingMaskIntoConstraints = false
    view.addSubview(content)
    NSLayoutConstraint.activate([
      content.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 8),
      content.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
      content.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
    ])

    if isRange {
      // Pass the end through the wheels once so it is clamped and rounded like the start.
      isEditingEnd = true
      showEditedDate()
      isEditingEnd = false
    }
    showEditedDate()
  }

  private func makeRangeButtons() -> UIView {
    startButton.addTarget(self, action: #selector(editStart), for: .touchUpInside)
    endButton.addTarget(self, action: #selector(editEnd), for: .touchUpInside)

    let row = UIStackView(arrangedSubviews: [startButton, endButton])
    row.axis = .horizontal
    row.spacing = 8
    row.distribution = .fillEqually
    return row
  }

  /// Points the wheels at the date being edited and refreshes the range buttons.
  private func showEditedDate() {
    let minimum = options.minimumDate.map(Date.init(milliseconds:))
    // The end of a range can never be picked before its start.
    picker.minimumDate = isEditingEnd ? max(minimum ?? selection.start, selection.start) : minimum
    picker.maximumDate = options.maximumDate.map(Date.init(milliseconds:))
    picker.date = isEditingEnd ? selection.end : selection.start
    // The wheels may have clamped or rounded the date; keep the selection in step with them.
    storePickerDate()
  }

  private func storePickerDate() {
    if isEditingEnd {
      selection.end = picker.date
    } else {
      selection.start = picker.date
      // Moving the start past the end drags the end along.
      selection.end = max(selection.end, selection.start)
    }
    guard isRange else { return }
    configure(startButton, label: options.startLabel, date: selection.start, isSelected: !isEditingEnd)
    configure(endButton, label: options.endLabel, date: selection.end, isSelected: isEditingEnd)
  }

  private func configure(_ button: UIButton, label: String, date: Date, isSelected: Bool) {
    var configuration: UIButton.Configuration = isSelected ? .tinted() : .gray()
    configuration.title = label
    configuration.subtitle = summaryFormatter.string(from: date)
    configuration.titleAlignment = .center
    configuration.cornerStyle = .large
    button.configuration = configuration
  }

  @objc private func pickerChanged() {
    storePickerDate()
  }

  @objc private func editStart() {
    isEditingEnd = false
    showEditedDate()
  }

  @objc private func editEnd() {
    isEditingEnd = true
    showEditedDate()
  }

  @objc private func confirm() {
    finish(with: selection)
  }

  @objc private func cancel() {
    finish(with: nil)
  }

  private func finish(with selection: DatePickerSelection?) {
    guard !hasFinished else { return }
    hasFinished = true
    dismiss(animated: true) { [onFinish] in onFinish(selection) }
  }

  func presentationControllerDidDismiss(_ presentationController: UIPresentationController) {
    guard !hasFinished else { return }
    hasFinished = true
    onFinish(nil)
  }

  /// The view controller currently on top, so the sheet also shows above modals.
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
