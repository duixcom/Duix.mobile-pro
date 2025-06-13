//
//  GJLDigitalConfig.h
//  GJLocalDigitalSDK
//
//  Created by guiji on 2025/5/15.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface GJLDigitalConfig : NSObject
/**
 * 用户ID  (for troubleshooting)
 */
@property (nonatomic, strong) NSString *userId;

/**
 * app名称  (for troubleshooting)
 */
@property (nonatomic, strong) NSString *appName;



+ (GJLDigitalConfig *)shareConfig;
@end

NS_ASSUME_NONNULL_END
