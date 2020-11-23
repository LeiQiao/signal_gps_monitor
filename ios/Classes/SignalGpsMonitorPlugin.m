#import "SignalGpsMonitorPlugin.h"
#if __has_include(<signal_gps_monitor/signal_gps_monitor-Swift.h>)
#import <signal_gps_monitor/signal_gps_monitor-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "signal_gps_monitor-Swift.h"
#endif

@implementation SignalGpsMonitorPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftSignalGpsMonitorPlugin registerWithRegistrar:registrar];
}
@end
