//
//  GJLASRManager.h
//  GJLocalDigitalSDK
//
//  Created by guiji on 2023/12/25.
//

#import <Foundation/Foundation.h>



@interface GJLASRManager : NSObject

//asr


/*
 *asrText 识别文字
 *isFinish 一句话是否结束
 */
@property (nonatomic, copy) void (^asrBlock)(NSString * asrText,BOOL isFinish);

/*
 *data 录音返回 单声道 1   采样率 16000
 */
@property (nonatomic, copy) void (^recordDataBlock)(NSData * data);



/*
 *音量回调
 */
@property (nonatomic, copy) void (^rmsBlock)(float rms);


@property (nonatomic, copy) void (^errBlock)(NSError *err);

/*
 * 服务端开始推送音频流
 */
@property (nonatomic, copy) void (^startPushBlock)(void);
/*
 *data 服务端返回音频流 单声道 1   采样率 16000
 */
@property (nonatomic, copy) void (^pushDataBlock)(NSData * data);
/*
 *服务端停止推送音频流
 */
@property (nonatomic, copy) void (^stopPushBlock)(void);

/*
 *大模型返回文字
 */
@property (nonatomic, copy) void (^speakTextBlock)(NSString * speakText);

/*
 *返回动作标记
 */
@property (nonatomic, copy) void (^motionBlock)(NSString * motionText);

+ (GJLASRManager*)manager;
/*
*初始化ASR
*asr 语言和平台在创建会话在网页端配置
*/
-(void)initASR;

/*
 *开始识别
 */
-(void)toOpenAsr;

/*
 *停止识别
 */
-(void)toCloseAsr;


@end


