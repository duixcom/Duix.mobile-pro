//
//  GJLDigitalManager.h
//  GJLocalDigitalSDK
//
//  Created by guiji on 2023/12/12.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
@interface GJLDigitalManager : NSObject

/*
 modeType 0 默认生产  1测试  2开发
 */
@property (nonatomic, assign) NSInteger modeType;

/*
 backType 0 背景透明   1 使用背景渲染
 */
@property (nonatomic, assign) NSInteger backType;


@property (nonatomic, assign)NSInteger dataLength;

/*
 pcmType 0 默认 wav to pcm  1 pcm
 */
@property (nonatomic, assign)NSInteger pcmType;


/*
*数字人渲染报错回调
* -1未初始化 50009资源超时或未配置
*/
@property (nonatomic, copy) void (^playFailed)(NSInteger code,NSString *errorMsg);

/*
*音频播放结束回调
*/
@property (nonatomic, copy) void (^audioPlayEnd)(void);

/*
*播放进度回调
*/
@property (nonatomic, copy) void (^audioPlayProgress)(float current,float total);

/*
*音频采集数据回调。 0 录音回调 1播放回调
*/
@property (nonatomic, copy) void (^sampleBufferOutputCallBack)(CMSampleBufferRef sample,NSInteger type);

/*
*音频流准备好开始播放音频流
*/
@property (nonatomic, copy)void (^pcmReadyBlock)(void);

/*
* 帧渲染计算统计
* @param resultCode 是否正常返回帧数据, <0 则说明帧数据异常
* @param isLip      是否计算唇形
* @param useTime    帧计算消耗的时间
*/
@property (nonatomic, copy)void (^onRenderReportBlock)(int resultCode, BOOL isLip, float useTime);

+ (GJLDigitalManager*)manager;


/*
*授权
*appId 对应应用的APPID
*appKey 对应应用的密钥
*isSuccess YES 返回成功 NO返回失败
*errorMsg 错误原因
*/
- (void)initWithAppId:(NSString *)appId appKey:(NSString *)appKey conversationId:(NSString*)conversationId block:(void (^) (BOOL isSuccess, NSString *errorMsg))block;

/*
 *basePath 底层通用模型路径-保持不变
 *digitalPath 数字人模型路径- 替换数字人只需要替换这个路径
 *return 1 返回成功  -1 初始化失败
 *showView 显示界面
 */
-(NSInteger)initBaseModel:(NSString*)basePath digitalModel:(NSString*)digitalPath showView:(UIView*)showView;


/*
 *bbgPath  替换背景 -jpg格式 --- ----背景size等于数字人模型的getDigitalSize-----------
 *默认backType=0时背景透明，自己在外面设置背景 。当backType=1 时调用此方法设置背景渲染
 */
-(void)toChangeBBGWithPath:(NSString*)bbgPath;





/*
*开始渲染数字人
*/
-(void)toStart:(void (^) (BOOL isSuccess, NSString *errorMsg))block;

/*
*结束
*/
-(void)toStop;

/*
*初始化模型过后才能获取
*getDigitalSize 数字人模型的宽度 数字人模型的高度
*/
-(CGSize)getDigitalSize;




/*
* 开始动作前调用
* 随机动作（一段文字包含多个音频，建议第一个音频开始时设置随机动作）
* return 0 数字人模型不支持随机动作 1 数字人模型支持随机动作
*/
-(NSInteger)toRandomMotion;

/*
* 一个动作区间来回震荡
* 1 数字人模型支持随机动作 0 数字人模型不支持随机动作
*/
-(NSInteger)toActRangeMinAndMax;

/*
* 开始动作 （一段文字包含多个音频，第一个音频开始时设置）
* return 0  数字人模型不支持开始动作 1  数字人模型支持开始动作
*/
-(NSInteger)toStartMotion;


/*
* 结束动作 （一段文字包含多个音频，最后一个音频播放结束时设置）
*isQuickly YES 立即结束动作   NO 等待动作播放完成再静默
*return 0 数字人模型不支持结束动作  1 数字人模型支持结束动作
*/
-(NSInteger)toSopMotion:(BOOL)isQuickly;

//
-(BOOL)toMotionByName:(NSString*)name;


/*
*暂停后才需执行播放数字人
*/
-(void)toPlay;

/*
*暂停数字人渲染
*/
-(void)toPause;


//-------------PCM流式--------------
/*
*开始录音和播放
*/
-(void)toStartRuning;


/*
*一句话或一段话的初始化session
*/
-(void)newSession;

/*
*一句话或一段话的推流结束调用finishSession 而非播放结束调用
*/
-(void)finishSession;


/*
*finishSession 结束后调用续上continueSession
*/
-(void)continueSession;

/*
*是否静音
*/
-(void)toMute:(BOOL)isMute;

/*
*audioData 单声道 16000采样率
* 参考toSpeakWithPath 转换成pcm的代码
*/
-(void)toWavPcmData:(NSData*)audioData;

/*
*pcm 单声道 16000采样率
*size
* 参考toSpeakWithPath 转换成pcm的代码
*/
-(void)wavPCM:(uint8_t*)pcm size:(int)size;

/*
*清空buffer
*/
-(void)clearAudioBuffer;

/*
*暂停播放音频流
*/
-(void)toPausePcm;

/*
*恢复播放音频流
*/
-(void)toResumePcm;


/*
* 是否启用录音
 */
-(void)toEnableRecord:(BOOL)isEnable;

/*
* 录音是否静音
 */
-(void)toMuteRecord:(BOOL)isMute;

/*
* 开始音频流播放
*/
- (void)startPlaying;
/*
* 结束音频流播放
*/
- (void)stopPlaying:(void (^)( BOOL isSuccess))success;
/*
*是否授权成功
*/
-(NSInteger)isGetAuth;

/*
*设置音量 参数范围 (0.0F~1.0F)
*/
-(void)toSetVolume:(float)volume;



@end


