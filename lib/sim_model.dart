class SimData {
  final List<SimCard> cards;
  SimData(this.cards);

  static SimData fromJson(data) {
    return SimData(data['cards'] != null && data['cards'] is List
        ? data['cards']
            .map<SimCard>((_card) => SimCard.fromJson(_card))
            .toList()
        : []);
  }
}

class SimCard {
  final String? carrierName;
  final String? countryCode;
  final String? displayName;
  final bool? isDataRoaming;
  final bool? isNetworkRoaming;
  final String? mcc;
  final String? mnc;
  final int? slotIndex;
  final String? serialNumber;
  final int? subscriptionId;
  final String? phoneNumber;
  final String? iccId;
  SimCard(
    this.carrierName,
    this.countryCode,
    this.displayName,
    this.isNetworkRoaming,
    this.isDataRoaming,
    this.slotIndex,
    this.serialNumber,
    this.subscriptionId,
    this.phoneNumber,
    this.iccId,
    this.mcc,
    this.mnc,
  );

  static SimCard fromJson(dynamic card) {
    return SimCard(
      card['carrierName'],
      card['countryCode'],
      card['displayName'],
      card['isDataRoaming'],
      card['isNetworkRoaming'],
      card['slotIndex'],
      card['serialNumber'],
      card['subscriptionId'] ?? 0,
      card['phoneNumber'] ?? '',
      card['iccId'],
      card['mcc'],
      card['mnc'],
    );
  }
}
