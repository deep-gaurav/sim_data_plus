import 'dart:async';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:sim_data_plus/sim_data.dart';

void main() => runApp(const MyApp());

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool _isLoading = true;
  SimData? _simData;
  String exception = '';

  @override
  void initState() {
    super.initState();
    init();
  }

  Future<void> init() async {
    try {
      //fetching status of access to phone permission
      var status = await Permission.phone.status;
      if (!status.isGranted) {
        bool isGranted = await Permission.phone.request().isGranted;
        if (!isGranted) return;
      }

      //calling plugin method to fetch sim data
      await SimDataPlugin.getSimData().then((simData){
        setState(() {
          _isLoading = false;
          _simData = simData;
          for (var item in _simData!.cards) {
            if(item.serialNumber==null){
              print("Serial Number is null need to ussd call");
            }
          }
        });
      });

    } catch (e) {
      debugPrint(e.toString());
      setState(() {
        _isLoading = false;
        _simData = null;
        exception = e.toString();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    var cards = _simData?.cards;
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Sim data demo')),
        body: Column(
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.all(20.0),
              child: Column(
                children: cards != null
                    ? cards.isEmpty
                        ? [const Text('No sim card present')]
                        : cards
                            .map(
                              (SimCard card) => ListTile(
                                leading: const Icon(Icons.sim_card),
                                title: const Text('Card'),
                                subtitle: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: <Widget>[
                                    Text('carrierName: ${card.carrierName}'),
                                    Text('countryCode: ${card.countryCode}'),
                                    Text('displayName: ${card.displayName}'),
                                    Text(
                                        'isDataRoaming: ${card.isDataRoaming}'),
                                    Text(
                                        'isNetworkRoaming: ${card.isNetworkRoaming}'),
                                    Text('phoneNumber: ${card.phoneNumber}'),
                                    Text('serialNumber: ${card.serialNumber}'),
                                    Text('subscriptionId: ${card.subscriptionId}'),
                                    Text('phoneNumber:${card.phoneNumber}'),
                                  ],
                                ),
                              ),
                            )
                            .toList()
                    : [
                        Center(
                          child: _isLoading
                              ? const CircularProgressIndicator()
                              :  Text(exception),
                        )
                      ],
              ),
            )
          ],
        ),
      ),
    );
  }
}
