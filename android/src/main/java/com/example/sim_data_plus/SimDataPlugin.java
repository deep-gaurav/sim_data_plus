package com.example.sim_data_plus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telecom.PhoneAccountHandle;
import android.telephony.UiccCardInfo;
import android.telecom.TelecomManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Iterator;
/**
 * SimDataPlugin
 */
public class SimDataPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

  //channel name, method channel, context and activity defined to call and access sim data 
  public static final String CHANNEL_NAME = "com.example.sim_data_plus/channel_name";
  private Context applicationContext;
  private Activity activity;
  private MethodChannel channel;

  //attach plugin to phone operating system through applictaion
  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    this.initialize(flutterPluginBinding.getBinaryMessenger(),
        flutterPluginBinding.getApplicationContext());
  }

  //initialise plugin
  public static void registerWith(Registrar registrar) {
    SimDataPlugin instance = new SimDataPlugin();
    instance.initialize(registrar.messenger(), registrar.context());
  }

  //initialise method channel and set handler on plugin
  private void initialize(BinaryMessenger messenger, Context context) {
    this.applicationContext = context;
    channel = new MethodChannel(messenger, CHANNEL_NAME);
    channel.setMethodCallHandler(this);
  }

  //remove previously attached plugin from phone operating system through applictaion
  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    channel = null;
    this.applicationContext = null;
  }

  //sim data fetching methods called according to phone os version
  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    //check if permissions are granted 
    if (!checkPermission()) {
      requestPermission();
    }
    try {
      //check if android version is android 11
      String simCards;
      if(android.os.Build.VERSION.SDK_INT >= 30){
        System.out.println("higher android");
        //call getSimData() to fetch sim details in case pf android 11 version
        simCards = getSimData().toString();
      }else{
        System.out.println("lower android");
        //call getSimData1() to fetch sim details in case of android 10 or lower versions
        simCards = getSimData1().toString();
      }
      result.success(simCards);

    } catch (Exception e) {
      System.out.println("Sim Data Plugin Exception");
      System.out.println(e);
      String simCards = "{}";
      // result.error("SimData_error", e.getMessage(), e.getStackTrace());
      result.success(simCards);
    }
  }

  //sim data fetching method for android 10 or lower versions
  private JSONObject getSimData1() throws Exception {
    //get phone service to access telephone network subscription or sim cards
    SubscriptionManager subscriptionManager = (SubscriptionManager) this.applicationContext
        .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        //get list of active sim cards on device
        @SuppressLint("MissingPermission") List<SubscriptionInfo> subscriptionInfos  = subscriptionManager.getActiveSubscriptionInfoList();



    JSONArray cards = new JSONArray();
    int i=0;
    for (SubscriptionInfo subscriptionInfo : subscriptionInfos) {

      //storing sim data returned from sim card system service object into variables
      int slotIndex = subscriptionInfo.getSimSlotIndex();
      CharSequence carrierName = subscriptionInfo.getCarrierName();
      String countryIso = subscriptionInfo.getCountryIso();
      int dataRoaming = subscriptionInfo.getDataRoaming();  // 1 is enabled ; 0 is disabled
      CharSequence displayName = subscriptionInfo.getDisplayName();
      String serialNumber = subscriptionInfo.getIccId();
      boolean networkRoaming = subscriptionManager.isNetworkRoaming(slotIndex);
      // String phoneNumber = subscriptionInfo.getNumber();
      int subscriptionId = subscriptionInfo.getSubscriptionId();
                         
      //storing variable data into new json object for each sim card
      JSONObject card = new JSONObject();

      card.put("carrierName", carrierName.toString());
      card.put("countryCode", countryIso);
      card.put("displayName", displayName.toString());
      card.put("isDataRoaming", (dataRoaming == 1));
      card.put("isNetworkRoaming", networkRoaming);
      // card.put("phoneNumber", phoneNumber);
      card.put("serialNumber", serialNumber);
      card.put("subscriptionId",subscriptionId);

      try{
        String phoneNumber = subscriptionInfo.getNumber();
        card.put("phoneNumber",phoneNumber);
      }catch(Exception ex){
        System.out.println("Excp - "+ex);
      }
      //add json object of sim card data into json array
      cards.put(card);
    }

    //storing json array into another json object
    JSONObject simCards = new JSONObject();
    simCards.put("cards", cards);

    //return data in json format object
    return simCards;
  }

  //sim data fetching method for android 11 version
  @SuppressLint("MissingPermission")
  private JSONObject getSimData() throws Exception {
    TelecomManager tm2;
    Iterator<PhoneAccountHandle> phoneAccounts;
    PhoneAccountHandle phoneAccountHandle;
    //get phone service to access telephone network subscription or sim cards
    SubscriptionManager subscriptionManager = (SubscriptionManager) this.applicationContext
        .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        //get list of active sim cards on device
        List<SubscriptionInfo> subscriptionInfos  =subscriptionManager.getActiveSubscriptionInfoList();
        //get telephony manager service of device system
        TelephonyManager telephonyManager = (TelephonyManager) this.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
    // try{
        //get telecom manager service of device system
        tm2= (TelecomManager)this.applicationContext.getSystemService(Context.TELECOM_SERVICE);
        //get all call capable phone accounts
        phoneAccounts = tm2.getCallCapablePhoneAccounts().listIterator();

    // }catch(Exception ex){
    //   System.out.println("Exception on TelecomManager");
    //   System.out.println(ex);
    // }
    JSONArray cards = new JSONArray();
    int count = 0;
    for (SubscriptionInfo subscriptionInfo : subscriptionInfos) {
      //storing sim data returned from sim card system service object into variables
      int slotIndex = subscriptionInfo.getSimSlotIndex();
      CharSequence carrierName = subscriptionInfo.getCarrierName();
      String countryIso = subscriptionInfo.getCountryIso();
      int dataRoaming = subscriptionInfo.getDataRoaming();  // 1 is enabled ; 0 is disabled
      CharSequence displayName = subscriptionInfo.getDisplayName();
      String serialNumber =subscriptionInfo.getIccId();
      int mcc = subscriptionInfo.getMcc();
      int mnc = subscriptionInfo.getMnc();
      boolean networkRoaming = subscriptionManager.isNetworkRoaming(slotIndex);
      // String phoneNumber = subscriptionInfo.getNumber();
      int subscriptionId = subscriptionInfo.getSubscriptionId();

      //storing variable data into new json object for each sim card
      JSONObject card = new JSONObject();

      card.put("carrierName", carrierName.toString());
      card.put("countryCode", countryIso);
      card.put("displayName", displayName.toString());
      card.put("isDataRoaming", (dataRoaming == 1));
      card.put("isNetworkRoaming", networkRoaming);
      card.put("subscriptionId",subscriptionId);
      try{
        String phoneNumber = subscriptionInfo.getNumber();
        card.put("phoneNumber",phoneNumber);
      }catch(Exception ex){
        System.out.println("Excp - "+ex);
      }

      // try{
      
      //get serial number of sim card/s
      phoneAccountHandle = phoneAccounts.next();
      if(count==0){
        card.put("serialNumber",phoneAccountHandle.getId().substring(0,19));
      }else{
        card.put("serialNumber",phoneAccountHandle.getId().substring(0,19));
      }
      count++;
      System.out.println("serial number - "+phoneAccountHandle.getId().substring(0,19));

      // }catch(Exception ex){
      //   System.out.println("Exception on TelecomManager");
      //   System.out.println(ex);
      // }
      //add json object of sim card data into json array
      cards.put(card);

    }

    //storing json array into another json object
    JSONObject simCards = new JSONObject();
    simCards.put("cards", cards);

    //return data in json format object
    return simCards;
  }

  //request required phone permissions to read sim data
  private void requestPermission() {
    //request permission as per android version
    if(android.os.Build.VERSION.SDK_INT ==30){
      String[] permissions = {"android.permission.READ_PHONE_STATE"};
      int requestCode = 5;
      ActivityCompat.requestPermissions(this.activity,permissions, requestCode);
    }else{
      String[] perm = {Manifest.permission.READ_PHONE_STATE};
      ActivityCompat.requestPermissions(this.activity, perm, 0);
    }
  
  }
  //request extra phone permissions required to read sim data in android 11
  private void requestNewPermission(){
    String[] perm = {Manifest.permission.READ_PHONE_NUMBERS};
    ActivityCompat.requestPermissions(this.activity, perm, 8);
  }
  //check status of phone permissions required
  private boolean checkPermission() {
    return PackageManager.PERMISSION_GRANTED == ContextCompat
    .checkSelfPermission(this.applicationContext, Manifest.permission.READ_PHONE_STATE);
  }
  //check status of extra phone permissions required
  private boolean checkNewPermission() {
    return PackageManager.PERMISSION_GRANTED == ContextCompat
    .checkSelfPermission(this.applicationContext, Manifest.permission.READ_PHONE_NUMBERS);
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    this.activity = null;
  }
}
