import ExpoModulesCore

public class NetworkStatusModule: Module {
  private static let statusChangeEvent = "onStatusChange"

  private let monitor = NetworkMonitor()

  public func definition() -> ModuleDefinition {
    Name("NetworkStatus")

    Events(Self.statusChangeEvent)

    // The monitor runs for the whole life of the module: it is passive and costs nothing
    // while the network is stable, and it keeps `getStatus` instant.
    OnCreate {
      monitor.onChange = { [weak self] status in
        self?.sendEvent(Self.statusChangeEvent, status)
      }
      monitor.start()
    }

    OnDestroy {
      monitor.stop()
    }

    // Resolves with the current status. Waits for the system's first report if it has not arrived yet.
    AsyncFunction("getStatus") { (promise: Promise) in
      monitor.currentStatus { status in
        promise.resolve(status)
      }
    }
  }
}
