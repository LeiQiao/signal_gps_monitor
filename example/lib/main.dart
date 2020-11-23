import 'dart:math';

import 'package:flutter/material.dart';
import 'dart:async';
import 'package:intl/intl.dart';

import 'package:flutter/services.dart';
import 'package:signal_gps_monitor/signal_gps_monitor.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  SignalStrengthMonitor signal = SignalStrengthMonitor();
  GpsMonitor gps = GpsMonitor();
  String _locationLog = "-- location log --";
  String _gpsLog = "-- gps log --";
  String _signalLog = "-- signal log --";
  int logSwitch = 0;

  @override
  void initState() {
    super.initState();
    signal.addObserver(onUpdateSignal);
    gps.addLocationObserver(onUpdateLocation);
    gps.addStrengthObserver(onUpdateGps);
  }

  @override
  void dispose() {
    super.dispose();
    signal.removeObserver(onUpdateSignal);
    gps.removeLocationObserver(onUpdateLocation);
    gps.removeStrengthObserver(onUpdateGps);
  }

  void onUpdateSignal(signalStrength) {
    DateTime now = DateTime.now();
    String formattedDate = DateFormat('HH:mm:ss').format(now);
    var strength = '${signalStrength.strength}'.padRight(10);
    var dbm = '${signalStrength.dbm}'.padRight(10);
    String logText = '<$formattedDate> signal strength: $strength dbm: $dbm';
    setState(() {
      _signalLog = logText + '\n' + _signalLog;
    });
  }

  void onUpdateLocation(gpsLocation) {
    DateTime now = DateTime.now();
    String formattedDate = DateFormat('HH:mm:ss').format(now);
    var longitude = '${gpsLocation.longitude}'.padRight(10);
    var latitude = '${gpsLocation.latitude}'.padRight(10);
    var accuracy = '${gpsLocation.accuracy}'.padRight(10);
    var altitude = '${gpsLocation.altitude}'.padRight(10);
    var speed = '${gpsLocation.speed}'.padRight(10);
    String logText = '<$formattedDate> location $longitude $latitude $accuracy $altitude $speed';
    setState(() {
      _locationLog = logText + '\n' + _locationLog;
    });
  }

  void onUpdateGps(gpsStrength) {
    DateTime now = DateTime.now();
    String formattedDate = DateFormat('HH:mm:ss').format(now);
    var strength = '${gpsStrength.strength}'.padRight(10);
    var satellite = '${gpsStrength.satelliteCount}'.padRight(3);
    var snr = '${gpsStrength.snr}'.padRight(10);
    String logText = '<$formattedDate> gps strength: $strength satellite: $satellite snr: $snr';
    setState(() {
      _gpsLog = logText + '\n' + _gpsLog;
    });
  }

  @override
  Widget build(BuildContext context) {
    String logText = '';
    if (logSwitch == 0) logText = _locationLog;
    else if (logSwitch == 1) logText = _gpsLog;
    else if (logSwitch == 2) logText = _signalLog;

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          mainAxisAlignment: MainAxisAlignment.start,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget> [
            CRDStatusBar(),
            Container(height: 50, child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                GestureDetector(
                  child: Container(
                      child: Text('location', style: TextStyle(color: (logSwitch == 0) ? Colors.white : Colors.black)),
                      padding: EdgeInsets.all(10),
                      decoration: BoxDecoration(border: Border.all(color: Colors.black), color: (logSwitch == 0) ? Colors.black : Colors.white)
                  ),
                  onTap: () {
                    setState(() {
                      logSwitch = 0;
                    });
                  }),
                GestureDetector(
                    child: Container(
                        child: Text('gps', style: TextStyle(color: (logSwitch == 1) ? Colors.white : Colors.black)),
                        padding: EdgeInsets.all(10),
                        decoration: BoxDecoration(border: Border.all(color: Colors.black), color: (logSwitch == 1) ? Colors.black : Colors.white)
                    ),
                    onTap: () {
                      setState(() {
                        logSwitch = 1;
                      });
                    }),
                GestureDetector(
                    child: Container(
                        child: Text('signal', style: TextStyle(color: (logSwitch == 2) ? Colors.white : Colors.black)),
                        padding: EdgeInsets.all(10),
                        decoration: BoxDecoration(border: Border.all(color: Colors.black), color: (logSwitch == 2) ? Colors.black : Colors.white)
                    ),
                    onTap: () {
                      setState(() {
                        logSwitch = 2;
                      });
                    })
              ]
            )),
            Expanded(flex: 1, child: ListView(children: [
              Text(logText, textAlign: TextAlign.left)
            ]))
          ]
        )
      ),
    );
  }
}


class CRDStatusBar extends StatefulWidget {
  final SignalStrengthMonitor signal = SignalStrengthMonitor();
  final GpsMonitor gps = GpsMonitor();

  CRDStatusBar();

  @override
  CRDStatusBarState createState() {
    return CRDStatusBarState();
  }
}

class CRDStatusBarState extends State<CRDStatusBar> {
  int signalStrength = SignalStrength.None;
  int gpsStrength = GpsStrength.None;

  @override
  void initState() {
    widget.signal.addObserver(onSignalStrengthChanged);
    widget.gps.addStrengthObserver(onGpsStrengthChanged);
    super.initState();
  }

  @override
  void dispose() {
    widget.signal.removeObserver(onSignalStrengthChanged);
    widget.gps.removeStrengthObserver(onGpsStrengthChanged);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(height: 25,
        decoration: BoxDecoration(color: Colors.white),
        child: Row(
          children: [
            Container(width: 20),
            _buildSignalStrengthWidget(),
            Container(width: 20),
            _buildGpsStrengthWidget()
          ]
        )
    );
  }

  _buildSignalStrengthWidget() {
    String image = 'images/nosignal.png';
    if (signalStrength == SignalStrength.Great) image = 'images/great.png';
    else if (signalStrength == SignalStrength.Good) image = 'images/good.png';
    else if (signalStrength == SignalStrength.Moderate) image = 'images/moderate.png';
    else if (signalStrength == SignalStrength.Poor) image = 'images/poor.png';
    else image = 'images/nosignal.png';

    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        (signalStrength != SignalStrength.None) ?
          Text('网络', style: TextStyle(fontWeight: FontWeight.bold)) :
          Text('网络无信号', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.red)),
        Container(width: 5),
        Image.asset(image, width: 25, height: 25)
      ]
    );
  }

  _buildGpsStrengthWidget() {
    String image = 'images/nosignal.png';
    if (gpsStrength == GpsStrength.Great) image = 'images/great.png';
    else if (gpsStrength == GpsStrength.Good) image = 'images/good.png';
    else if (gpsStrength == GpsStrength.Poor) image = 'images/poor.png';
    else image = 'images/nosignal.png';

    return Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          (gpsStrength != GpsStrength.None) ?
            Text('GPS', style: TextStyle(fontWeight: FontWeight.bold)) :
            Text('GPS 无信号', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.red)),
          Container(width: 5),
          Image.asset(image, width: 25, height: 25)
        ]
    );
  }

  void onSignalStrengthChanged(SignalStrength strength) {
    setState(() {
      this.signalStrength = strength.strength;
    });
  }

  void onGpsStrengthChanged(GpsStrength strength) {
    setState(() {
      this.gpsStrength = strength.strength;
    });
  }
}
