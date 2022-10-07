#import "SimDataPlusPlugin.h"
#if __has_include(<sim_data_plus/sim_data_plus-Swift.h>)
#import <sim_data_plus/sim_data_plus-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "sim_data_plus-Swift.h"
#endif

@implementation SimDataPlusPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftSimDataPlusPlugin registerWithRegistrar:registrar];
}
@end
