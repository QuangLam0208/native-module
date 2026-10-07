import ExpoModulesCore
import UIKit

public class DeviceHelperModule: Module {
  private static let batteryChangeEvent = "onBatteryChange"

  private var batteryObservers: [NSObjectProtocol] = []

  public func definition() -> ModuleDefinition {
    Name("DeviceHelper")

    Constant("osVersion") { DeviceInfo.osVersion }
    Constant("model") { DeviceInfo.model }

    Events(Self.batteryChangeEvent)

    // UIDevice is main-thread only, so the read runs on the main queue.
    AsyncFunction("getHardwareInfo") { () -> [String: Any] in
      HardwareReader.read()
    }
    .runOnQueue(.main)

    // Battery notifications are only posted while a JS listener is attached.
    OnStartObserving {
      DispatchQueue.main.async { self.startObservingBattery() }
    }

    OnStopObserving {
      DispatchQueue.main.async { self.stopObservingBattery() }
    }
  }

  private func startObservingBattery() {
    guard batteryObservers.isEmpty else { return }
    UIDevice.current.isBatteryMonitoringEnabled = true

    let names = [UIDevice.batteryLevelDidChangeNotification, UIDevice.batteryStateDidChangeNotification]
    batteryObservers = names.map { name in
      NotificationCenter.default.addObserver(forName: name, object: nil, queue: .main) { [weak self] _ in
        self?.sendEvent(Self.batteryChangeEvent, HardwareReader.battery())
      }
    }
  }

  private func stopObservingBattery() {
    batteryObservers.forEach(NotificationCenter.default.removeObserver)
    batteryObservers = []
  }
}
