import Flutter
import UIKit

public class SwiftSignalGpsMonitorPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "signal_gps_monitor", binaryMessenger: registrar.messenger())
    let instance = SwiftSignalGpsMonitorPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
