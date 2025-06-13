//
//  GJLPCMManager.h
//  GJLocalDigitalDemo
//
//  Created by guiji on 2025/5/12.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface GJLPCMManager : NSObject
+ (GJLPCMManager *)manager;
/*
 wavPath 音频的本地路径
 *1通道 16位深 16000采样率的wav本地文件和在线音频wav文件
 */
-(void)toSpeakWithPath:(NSString*)wavPath;

- (void)toStop;

@end

NS_ASSUME_NONNULL_END
