package com.anren.signal_gps_monitor

import android.content.Context
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener


/** SignalGpsMonitorPlugin */
public class SignalGpsMonitorPlugin: FlutterPlugin, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var signalChannel : MethodChannel
  private lateinit var gpsChannel : MethodChannel

  private lateinit var signalHandler : SignalHandler
  private lateinit var gpsHandler : GpsHandler

  private lateinit var context: Context
  private lateinit var activity: Activity

  override fun onDetachedFromActivity() {
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
    gpsHandler.setActivity(activity)
    binding.addRequestPermissionsResultListener(gpsHandler as RequestPermissionsResultListener)
  }

  override fun onDetachedFromActivityForConfigChanges() {
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
  }

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.getApplicationContext()

    signalChannel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "signal_gps_monitor.signal_monitor")
    signalHandler = SignalHandler(context, signalChannel)
    signalChannel.setMethodCallHandler(signalHandler)

    gpsChannel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "signal_gps_monitor.gps_monitor")
    gpsHandler = GpsHandler(context, gpsChannel)
    gpsChannel.setMethodCallHandler(gpsHandler)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    signalChannel.setMethodCallHandler(null)
    gpsChannel.setMethodCallHandler(null)

    signalHandler.stop()
    gpsHandler.stop()
  }
}

class SignalHandler(context: Context, channel: MethodChannel) : MethodCallHandler, Signal.IListener {
  private var signal: Signal = Signal(context)
  private var channel: MethodChannel = channel

  fun stop() {
    signal.stop()
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "start") {
      signal.start(this)
      result.success(0)
    } else if (call.method == "stop") {
      signal.stop()
      result.success(0)
    } else if (call.method =="getStrength") {
      result.success(signal.getStrength());
    } else {
      result.notImplemented()
    }
  }

  override fun onUpdate(signalStrength: Map<String, Int>) {
    channel.invokeMethod("onUpdate", signalStrength)
  }
}

class GpsHandler(context: Context, channel: MethodChannel) : MethodCallHandler, Gps.IListener, RequestPermissionsResultListener {
  private var gps: Gps = Gps(context)
  private var channel: MethodChannel = channel
  private var activity: Activity? = null

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray): Boolean {
    gps.onAuthGranted(requestCode, permissions, grantResults)
    return false
  }

  fun setActivity(activity: Activity) {
    this.activity = activity
  }

  fun stop() {
    gps.stopStrengthMonitor()
    gps.stopLocationMonitor()
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "startStrengthMonitor") {
      gps.startStrengthMonitor(activity!!, this)
      result.success(0)
    } else if (call.method == "stopStrengthMonitor") {
      gps.stopStrengthMonitor()
      result.success(0)
    } else if (call.method == "startLocationMonitor") {
      gps.startLocationMonitor(activity!!, this)
      result.success(0)
    } else if (call.method == "stopLocationMonitor") {
      gps.stopLocationMonitor()
      result.success(0)
    } else if (call.method =="getStrength") {
      result.success(gps.getStrength())
    } else if (call.method =="getLocation") {
      result.success(gps.getLocation())
    } else {
      result.notImplemented()
    }
  }

  override fun onUpdateStrength(gpsStrength: Map<String, Any>) {
    channel.invokeMethod("onUpdateStrength", gpsStrength)
  }

  override fun onUpdateLocation(gpsLocation: Map<String, Double>) {
    channel.invokeMethod("onUpdateLocation", gpsLocation)
  }
}
