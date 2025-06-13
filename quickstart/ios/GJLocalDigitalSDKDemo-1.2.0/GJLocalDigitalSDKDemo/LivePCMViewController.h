//
//  LivePCMViewController.h
//  GJLocalDigitalDemo
//
//  Created by guiji on 2025/5/20.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface LivePCMViewController : UIViewController
@property(nonatomic,strong)NSString * basePath;
@property(nonatomic,strong)NSString * digitalPath;
@property(nonatomic,strong)NSString * appId;
@property(nonatomic,strong)NSString * appKey;
@property(nonatomic,strong)NSString * conversationId;
@end

NS_ASSUME_NONNULL_END
