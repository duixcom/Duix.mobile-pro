//
//  AppDelegate.m
//  GJLocalDigitalSDKDemo
//
//  Created by guiji on 2023/12/18.
//

#import "AppDelegate.h"

@interface AppDelegate ()

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Override point for customization after application launch.
    self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
    self.vc=[[ViewController alloc] init];
    UINavigationController * nav=[[UINavigationController alloc] initWithRootViewController:self.vc];
    self.window.rootViewController=nav;
    [self.window makeKeyAndVisible];
    return YES;
}





@end
