import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:signal_gps_monitor/signal_gps_monitor.dart';

void main() {
  const MethodChannel channel = MethodChannel('signal_gps_monitor');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await SignalGpsMonitor.platformVersion, '42');
  });
}
