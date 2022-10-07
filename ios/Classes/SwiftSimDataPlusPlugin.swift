import Flutter
import UIKit
import CoreTelephony

public class SwiftSimDataPlusPlugin: NSObject, FlutterPlugin {
    lazy var carriers:[String:CTCarrier?]? = {
        let networkInfo = CTTelephonyNetworkInfo()
        if #available(iOS 12.0 , *){
            return networkInfo.serviceSubscriberCellularProviders;
        }else{
            let carrier = networkInfo.subscriberCellularProvider;
            return ["carrier":carrier];
        }
    }();
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "com.example.sim_data_plus/channel_name", binaryMessenger: registrar.messenger())
    let instance = SwiftSimDataPlusPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
      guard let carriers = carriers else {
          result(FlutterError(code: "EC-SIM-NR", message: "Sim not ready", details: nil))
          return;
      }
      if(call.method == "getSimData"){
          var cards:[Card] = [];
          var index = 0;
          for carrier in carriers{
              if(carrier.value != nil){
                  let card = Card(mcc: carrier.value?.mobileCountryCode ?? "", mnc: carrier.value?.mobileNetworkCode ?? "", slotIndex: index, carrierName: carrier.value?.carrierName ?? "")
                  cards.append(card);
                  index += 1;
              }
          }
          let data = Data(cards: cards)
          let encoder = JSONEncoder()
          encoder.outputFormatting = .prettyPrinted
          
          let value =  try! encoder.encode(data)
          let jsonString = String(data: value,encoding: .utf8)
          result(jsonString)
      }
  }
}

struct Data: Codable {
    var cards: [Card]
}

struct Card: Codable{
    var mcc:String;
    var mnc: String;
    var slotIndex:Int;
    var carrierName:String;
}
