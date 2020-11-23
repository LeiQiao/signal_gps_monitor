
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class SignalStrength {
  static const int Great = 4;
  static const int Good = 3;
  static const int Moderate = 2;
  static const int Poor = 1;
  static const int None = 0;

  final int strength;
  final int dbm;
  final int asu;
  SignalStrength(this.strength, this.dbm, this.asu);
}

typedef SignalStrengthCallback = void Function(SignalStrength);

class SignalStrengthMonitor {
  static const MethodChannel _channel = const MethodChannel('signal_gps_monitor.signal_monitor');
  static List<SignalStrengthCallback> _callbacks = [];

  SignalStrengthMonitor() {
    _channel.setMethodCallHandler(this.onChannelHandler);
    _channel.invokeMethod('start');
  }

  Future<dynamic> onChannelHandler(MethodCall methodCall) async {
    if (methodCall.method == "onUpdate") {
      var data = await methodCall.arguments;
      var signalStrength = SignalStrength(data['strength'], data['dbm'], data['asu']);
      _callbacks.forEach((callback) {
        callback(signalStrength);
      });
    }
  }

  getSignalStrength() async {
    var data = await _channel.invokeMethod('getStrength');
    var strength = SignalStrength(data['strength'], data['dbm'], data['asu']);
    return strength;
  }

  addObserver(SignalStrengthCallback callback) {
    if (_callbacks.indexOf(callback) >= 0) return;
    _callbacks.add(callback);
  }

  removeObserver(SignalStrengthCallback callback) {
    int index = _callbacks.indexOf(callback);
    if (index < 0) return;
    _callbacks.removeAt(index);
  }
}

class GpsStrength {
  static const int Great = 4;
  static const int Good = 3;
  static const int Poor = 1;
  static const int None = 0;

  final int strength;
  final int satelliteCount;
  final double snr;
  GpsStrength(this.strength, this.satelliteCount, this.snr);
}

class GpsLocation {
  final double longitude;
  final double latitude;
  final double accuracy;
  final double altitude;
  final double speed;
  GpsLocation(this.longitude, this.latitude, this.accuracy, this.altitude, this.speed);
}

typedef GpsStrengthCallback = void Function(GpsStrength);
typedef GpsLocationCallback = void Function(GpsLocation);

class GpsMonitor {
  static const MethodChannel _channel = const MethodChannel('signal_gps_monitor.gps_monitor');
  static List<GpsStrengthCallback> _strengthCallbacks = [];
  static List<GpsLocationCallback> _locationCallbacks = [];

  GpsMonitor() {
    _channel.setMethodCallHandler(this.onChannelHandler);
  }

  Future<dynamic> onChannelHandler(MethodCall methodCall) async {
    if (methodCall.method == "onUpdateStrength") {
      var data = await methodCall.arguments;
      var strength = GpsStrength(data['strength'], data['satelliteCount'], data['snr']);
      _strengthCallbacks.forEach((callback) {
        callback(strength);
      });
    } else if (methodCall.method == "onUpdateLocation") {
      var data = await methodCall.arguments;
      var location = GpsLocation(data['longitude'], data['latitude'], data['accuracy'], data['altitude'], data['speed']);
      _locationCallbacks.forEach((callback) {
        callback(location);
      });
    }
  }

  getGpsStrength() async {
    var data = await _channel.invokeMethod('getStrength');
    var strength = GpsStrength(data['strength'], data['satelliteCount'], data['snr']);
    return strength;
  }

  getPosition() async {
    var data = await _channel.invokeMethod('getPosition');
    var location = GpsLocation(data['longitude'], data['latitude'], data['accuracy'], data['altitude'], data['speed']);
    return location;
  }

  addStrengthObserver(GpsStrengthCallback callback) {
    if (_strengthCallbacks.indexOf(callback) >= 0) return;
    if (_strengthCallbacks.length == 0) {
      _channel.invokeMethod('startStrengthMonitor');
    }
    _strengthCallbacks.add(callback);
  }

  removeStrengthObserver(GpsStrengthCallback callback) {
    int index = _strengthCallbacks.indexOf(callback);
    if (index < 0) return;
    _strengthCallbacks.removeAt(index);
    if (_strengthCallbacks.length == 0) {
      _channel.invokeMethod('stopStrengthMonitor');
    }
  }

  addLocationObserver(GpsLocationCallback callback) {
    if (_locationCallbacks.indexOf(callback) >= 0) return;
    if (_locationCallbacks.length == 0) {
      _channel.invokeMethod('startLocationMonitor');
    }
    _locationCallbacks.add(callback);
  }

  removeLocationObserver(GpsLocationCallback callback) {
    int index = _locationCallbacks.indexOf(callback);
    if (index < 0) return;
    _locationCallbacks.removeAt(index);
    if (_locationCallbacks.length == 0) {
      _channel.invokeMethod('stopLocationMonitor');
    }
  }
}


