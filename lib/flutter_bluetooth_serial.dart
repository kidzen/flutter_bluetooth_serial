import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

///
///
///
class FlutterBluetoothSerial {
  static const int STATE_OFF = 10;
  static const int STATE_TURNING_ON = 11;
  static const int STATE_ON = 12;
  static const int STATE_TURNING_OFF = 13;
  static const int STATE_BLE_TURNING_ON = 14;
  static const int STATE_BLE_ON = 15;
  static const int STATE_BLE_TURNING_OFF = 16;
  static const int ERROR = -1;
  static const int CONNECTED = 1;
  static const int DISCONNECTED = 0;

  static const String namespace = 'flutter_bluetooth_serial';

  static const MethodChannel _channel =
  const MethodChannel('$namespace/methods');

  static const EventChannel _readChannel =
  const EventChannel('$namespace/read');

  static const EventChannel _stateChannel =
  const EventChannel('$namespace/state');

  final StreamController<MethodCall> _methodStreamController =
  new StreamController.broadcast();

  Stream<MethodCall> get _methodStream => _methodStreamController.stream;

  FlutterBluetoothSerial._() {
    _channel.setMethodCallHandler((MethodCall call) {
      _methodStreamController.add(call);
      });
  }

  static FlutterBluetoothSerial _instance = new FlutterBluetoothSerial._();

  static FlutterBluetoothSerial get instance => _instance;

  Stream<int> onStateChanged() =>
  _stateChannel.receiveBroadcastStream().map((buffer) => buffer);

  Stream<String> onRead() =>
  _readChannel.receiveBroadcastStream().map((buffer) => buffer.toString());

  Future<bool> get isAvailable async =>
  await _channel.invokeMethod('isAvailable');

  Future<String> get deviceSerial async =>
  await _channel.invokeMethod('deviceSerial');

  Future<bool> get isOn async => await _channel.invokeMethod('isOn');

  Future<bool> get isConnected async =>
  await _channel.invokeMethod('isConnected');

  Future<bool> get openSettings async =>
  await _channel.invokeMethod('openSettings');

  Future<List> getBondedDevices() async {
    final List list = await _channel.invokeMethod('getBondedDevices');
    return list.map((map) => BluetoothDevice.fromMap(map)).toList();
  }

  Future<dynamic> connect(BluetoothDevice device) =>
  _channel.invokeMethod('connect', device.toMap());

  Future<dynamic> disconnect() => _channel.invokeMethod('disconnect');

  Future<dynamic> write(String message) =>
  _channel.invokeMethod('write', {'message': message});

  Future<dynamic> printJob(NoticePrint notice, Uint8List imgByte) async =>
  await _channel.invokeMethod('printJob', {"notice":notice.toMap(), "signature":imgByte});

  Future<dynamic> testPrint(String message) =>
  _channel.invokeMethod('testPrint', {'message': message});

  Future<dynamic> writeBytes(Uint8List bytes) =>
  _channel.invokeMethod('writeBytes', {'message': bytes});
}


class BluetoothDevice {
  final String name;
  final String address;
  final int type;
  final String status;
  bool connected = false;

  BluetoothDevice.fromMap(Map map)
  : name = map['name'],
  address = map['address'],
  type = map['type'],
  status = map['status'];

  Map<String, dynamic> toMap() => {
    'name': this.name,
    'address': this.address,
    'type': this.type,
    'status': this.status,
    'connected': this.connected,
  };
}

class NoticePrint {
  String noticeNo;
  String roadtax;
  String vehicleNo;
  String vehicleMakeModel;
  String color;
  String locationDetail;
  String location;
  String area;
  String vehicleType;
  String date;
  String time;
  String actStd;
  String actReg;
  String offence;
  String officer;
  String rate1;
  String rate2;
  String rate3;
  String expiredDate;

  Map<String, dynamic> toMap() => {
    'notice_no': this.noticeNo,
    'vehicle_no': this.vehicleNo,
    'roadtax': this.roadtax,
    'vehicle_make_model': this.vehicleMakeModel,
    'color': this.color,
    'location_detail': this.locationDetail,
    'area': this.area,
    'location': this.location,
    'vehicle_type': this.vehicleType,
    'date': this.date,
    'time': this.time,
    'act_std': this.actStd,
    'act_reg': this.actReg,
    'offence': this.offence,
    'officer': this.officer,
    'rate1': this.rate1,
    'rate2': this.rate2,
    'rate3': this.rate3,
    'expired_date': this.expiredDate,
  };
}

