import Foundation
import Network

/// Watches the network path with NWPathMonitor and reports it as a dictionary for JS.
/// All state is confined to `queue`.
final class NetworkMonitor {
  /// Called on a background queue whenever the status changes. Not called for the first report.
  var onChange: (([String: Any]) -> Void)?

  private let monitor = NWPathMonitor()
  private let queue = DispatchQueue(label: "native-network-status.monitor")
  private var lastStatus: [String: Any]?
  // Callers of `currentStatus` that asked before the system's first report.
  private var pendingRequests: [([String: Any]) -> Void] = []

  func start() {
    monitor.pathUpdateHandler = { [weak self] path in
      self?.handle(path)
    }
    monitor.start(queue: queue)
  }

  func stop() {
    monitor.cancel()
  }

  func currentStatus(_ completion: @escaping ([String: Any]) -> Void) {
    queue.async {
      if let status = self.lastStatus {
        completion(status)
      } else {
        self.pendingRequests.append(completion)
      }
    }
  }

  private func handle(_ path: NWPath) {
    let status = Self.describe(path)
    let previous = lastStatus
    lastStatus = status

    pendingRequests.forEach { $0(status) }
    pendingRequests = []

    // The system often reports the same path several times in a row.
    if let previous, !NSDictionary(dictionary: previous).isEqual(to: status) {
      onChange?(status)
    }
  }

  private static func describe(_ path: NWPath) -> [String: Any] {
    let isConnected = path.status == .satisfied
    return [
      "isConnected": isConnected,
      "type": isConnected ? connectionType(of: path) : "none",
      // True on cellular and personal hotspots.
      "isExpensive": path.isExpensive,
      // True when the user turned on Low Data Mode.
      "isConstrained": path.isConstrained,
    ]
  }

  private static func connectionType(of path: NWPath) -> String {
    if path.usesInterfaceType(.wifi) { return "wifi" }
    if path.usesInterfaceType(.cellular) { return "cellular" }
    if path.usesInterfaceType(.wiredEthernet) { return "ethernet" }
    return "other"
  }
}
