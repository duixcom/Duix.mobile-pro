## 硅基本地版DUIX-PRO SDK使⽤⽂档 (1.2.0)
    
### 物料准备
 GJLocalDigitalSDK.framework  (-Embed & Sign)
 

 
      

### 开发环境
开发⼯具: Xcode  ios12.0以上 iphone8及以上

## 快速开始
```
    //授权
       [GJLDigitalConfig shareConfig].appName= [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleDisplayName"];
    [GJLDigitalConfig shareConfig].userId = [NSString stringWithFormat:@"sdk_%@",[OpenUDID value]];

    [[GJLDigitalManager manager] initWithAppId:self.appId appKey:self.appKey conversationId:self.conversationId block:^(BOOL isSuccess, NSString *errorMsg) {
        if(isSuccess)
        {
    
            NSInteger result=   [[GJLDigitalManager manager] initBaseModel:weakSelf.basePath digitalModel:weakSelf.digitalPath showView:weakSelf.showView];
             if(result==1)
             {
       
 //                NSString *bgpath =[NSString stringWithFormat:@"%@/%@",[[NSBundle mainBundle] bundlePath],@"bg2.jpg"];
 //                [[GJLDigitalManager manager] toChangeBBGWithPath:bgpath];
                 [[GJLDigitalManager manager] toStart:^(BOOL isSuccess, NSString *errorMsg) {
                     if(isSuccess)
                     {
                         
                         dispatch_async(dispatch_get_main_queue(), ^{
                      
                    
                         
                                [[GJLDigitalManager manager] toStartRuning];
                                [weakSelf initASR];
                                [[GJLASRManager manager] toOpenAsr];
                          
                     
                         });
//

                     }
                     else
                     {
                         [SVProgressHUD showInfoWithStatus:errorMsg];
                     }
                 }];
             }
            else
            {
                [SVProgressHUD showInfoWithStatus:@"模型初始化失败"];
            }
     
        }
        else
        {
            [SVProgressHUD showInfoWithStatus:errorMsg];
        }
            
    }];
```
## 调用流程
```
1. 启动服务前需要准备好授权的appId,appKey以及同步数字人需要的基础配置和模型文件。
2. 使用授权接口授权。
3. 初始化数字人渲染服务。
4. 调用toStart函数开始渲染数字人
5. 调用toSpeakWithPath函数驱动数字人播报。
6. 调用cancelAudioPlay函数可以主动停止播报。
7. 调用toStop结束并释放数字人渲染
```

### SDK回调

```
/*
*数字人渲染报错回调
*0 未授权 -1未初始化 50009资源超时或未配置
*/
@property (nonatomic, copy) void (^playFailed)(NSInteger code,NSString *errorMsg);

/*
*音频播放结束回调
*/
@property (nonatomic, copy) void (^audioPlayEnd)(void);

/*
*音频播放进度回调
/
@property (nonatomic, copy) void (^audioPlayProgress)(float current,float total);
```

## 方法

### 授权

```
/*
*appId 对应应用的APPID
*appKey 对应应用的密钥
*isSuccess YES 返回成功 NO返回失败
*errorMsg 错误原因
*/
- (void)initWithAppId:(NSString *)appId appKey:(NSString *)appKey block:(void (^) (BOOL isSuccess, NSString *errorMsg))block;
```

### 初始化

```
/*
*basePath 底层通用模型路径-保持不变
*digitalPath 数字人模型路径- 替换数字人只需要替换这个路径
*return 1 返回成功 0未授权 -1 初始化失败
*showView 显示界面
*/
-(NSInteger)initBaseModel:(NSString*)basePath digitalModel:(NSString*)digitalPath showView:(UIView*)showView;
```

### 替换背景

```
/*
* bbgPath 替换背景 
* 注意: -jpg格式 ----背景size等于数字人模型的getDigitalSize-----------
*/
-(void)toChangeBBGWithPath:(NSString*)bbgPath;
```



### 开始渲染数字人

```
/*
*开始
*/
-(void)toStart:(void (^) (BOOL isSuccess, NSString *errorMsg))block;
```

### 结束渲染数字人并释放
```
/*
*结束
*/
-(void)toStop;
```

### 数字人模型的宽度高度

```
/*
*初始化模型过后才能获取
*getDigitalSize 数字人模型的宽度 数字人模型的高度
*/
-(CGSize)getDigitalSize;
```

### 取消播放音频

```
/*
*取消播放音频
*/
-(void)cancelAudioPlay;
```



### 授权成功

```
/*
*是否授权成功
*/
-(NSInteger)isGetAuth;
```

### PCM流式

```
/*
*开始录音和播放
*/
-(void)toStartRuning;
```

```
/*
*一句话或一段话的初始化session
*/
-(void)newSession;
```

```
/*
*一句话或一段话的推流结束调用finishSession 而非播放结束调用
*/
-(void)finishSession;
```


```
/*
*finishSession 结束后调用续上continueSession
*/
-(void)continueSession;
```

```
/*
*是否静音
*/
-(void)toMute:(BOOL)isMute;
```


```
/*
*pcm
*size
* 参考toSpeakWithPath 转换成pcm的代码
*/
-(void)toWavPcmData:(NSData*)audioData;
```

```
/*
*清空buffer
*/
-(void)clearAudioBuffer;
```


```
/*
*暂停播放音频流
*/
-(void)toPausePcm;
```


```
/*
*恢复播放音频流
*/
-(void)toResumePcm;
```


```
/*
* 是否启用录音
 */
-(void)toEnableRecord:(BOOL)isEnable;
```


```
/*
* 开始音频流播放
*/
- (void)startPlaying;
```

```
/*
* 结束音频流播放
*/
- (void)stopPlaying:(void (^)( BOOL isSuccess))success;
```


## 动作

### 随机动作
 
```
/*
* 开始动作前调用
* 随机动作（一段文字包含多个音频，建议第一个音频开始时设置随机动作）
* return 0 数字人模型不支持随机动作 1 数字人模型支持随机动作
*/
-(NSInteger)toRandomMotion;
```

### 开始动作

```
/*
* 开始动作 （一段文字包含多个音频，第一个音频开始时设置）
* return 0  数字人模型不支持开始动作 1  数字人模型支持开始动作
*/
-(NSInteger)toStartMotion;
```

### 结束动作
```
/*
* 结束动作 （一段文字包含多个音频，最后一个音频播放结束时设置）
*isQuickly YES 立即结束动作   NO 等待动作播放完成再静默
*return 0 数字人模型不支持结束动作  1 数字人模型支持结束动作
*/
-(NSInteger)toSopMotion:(BOOL)isQuickly;
```

### 暂停后开始播放数字人
```
/*
*暂停后才需执行播放数字人
*/
-(void)toPlay;
```

### 暂停数字人播放
```
/*
*暂停数字人播放
*/
-(void)toPause;
```

## 语音识别 

### 初始化录音和ASR

```
/*
*初始化录音和ASR
*/
-(void)initASR;
```

### 开始识别

```
/*
*开始识别
*/
-(void)toOpenAsr;
```

### 停止识别

```
/*
*停止识别
*/
-(void)toCloseAsr;
```

### 语音识别回调

```
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
```

## 版本记录

**1.2.0**
```
1. 支持pcm流式
```

**1.0.3**
```
1. 数字人背景透明
2. 解压内存问题
```

**1.0.2**
```
1. 问答
2. 语音识别
3. 文字合成
4. 说话动作
```


**1.0.1**
```
1. 数字人本地授权和初始化
2. 数字人本地渲染
3. 音频播放和驱动嘴形
```
