import UIKit
import os

/// Reads live hardware state. Call from the main thread: UIDevice is not thread-safe.
enum HardwareReader {
  private static let bytesPerMb = 1024.0 * 1024.0

  static func read() -> [String: Any] {
    var info = battery()
    info["totalRamMb"] = (Double(ProcessInfo.processInfo.physicalMemory) / bytesPerMb).rounded()
    // iOS has no system-wide "free RAM"; this is what the app may still allocate.
    // It reads 0 where the process has no memory limit (the simulator), reported as -1 for "unknown".
    let available = os_proc_available_memory()
    info["availableRamMb"] = available == 0 ? -1 : (Double(available) / bytesPerMb).rounded()
    return info
  }

  static func battery() -> [String: Any] {
    let device = UIDevice.current
    device.isBatteryMonitoringEnabled = true

    // batteryLevel is -1 when unknown, which is always the case on the simulator.
    let level = device.batteryLevel
    return [
      "batteryLevel": level < 0 ? -1 : Int((level * 100).rounded()),
      "isCharging": device.batteryState == .charging || device.batteryState == .full,
    ]
  }
}
