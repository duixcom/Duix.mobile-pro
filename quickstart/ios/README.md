## Silicon Basic Edition DUIX SDK Usage Document (1.2.0)
    
###  Supported Systems and Hardware Versions
 GJLocalDigitalSDK.framework  (-Embed & Sign)
 

 
      

### Development Environment
Development Tool: Xcode ios12.0 and above iphone8 and above

## Quick Start
```

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
                [SVProgressHUD showInfoWithStatus:@"model fail"];
            }
     
        }
        else
        {
            [SVProgressHUD showInfoWithStatus:errorMsg];
        }
            
    }];
```
## Call process
```
1. Prepare the basic configuration and model files required for the synchronous digital person before starting the service.
2. Initialize the digital person rendering service.
3. Call the toStart function to start rendering the digital person
4. Call the toSpeakWithPath function to drive the digital person to broadcast.
5. Call cancelAudioPlay to actively stop broadcasting.
6. Call toStop to end and release the digital person rendering
```

### SDK Callback

```
/*
*Digital person rendering error callback
*-1 uninitialized 50009 resource timeout or not configured
*/
@property (nonatomic, copy) void (^playFailed)(NSInteger code,NSString *errorMsg);

/*
*Audio playback end callback 
*/
@property (nonatomic, copy) void (^audioPlayEnd)(void);

/*
*Audio playback progress callback 
/
@property (nonatomic, copy) void (^audioPlayProgress)(float current,float total);
```

## Methods

### Auth

```
/*
 * appId The APPID of the corresponding application
 * appKey The secret key of the corresponding application
 * isSuccess YES means success, NO means failure
 * errorMsg Error message
 */
- (void)initWithAppId:(NSString *)appId appKey:(NSString *)appKey block:(void (^) (BOOL isSuccess, NSString *errorMsg))block;
```

### Initialization

```
/*
*basePath Base common model path - remains unchanged
*digitalPath Digital person model path - only need to replace this path to replace the digital person
*return 1 Success  -1 Initialization failed
*showView Display interface
*/
-(NSInteger)initBaseModel:(NSString*)basePath digitalModel:(NSString*)digitalPath showView:(UIView*)showView;
```

### Replace background

```
/*
* bbgPath Replace background
* Note: -jpg format ----Background size equals the digital person model's getDigitalSize-----------
*/
-(void)toChangeBBGWithPath:(NSString*)bbgPath;
```


### Start rendering digital person

```
/*
*Start
*/
-(void)toStart:(void (^) (BOOL isSuccess, NSString *errorMsg))block;
```

### End rendering digital person and release
```
/*
*End
*/
-(void)toStop;
```

### Width and height of digital person model

```
/*
*After initializing the model, you can get it
*getDigitalSize Width of digital person model Height of digital person model
*/
-(CGSize)getDigitalSize;
```

### Cancel playing audio

```
/*
*Cancel playing audio
*/
-(void)cancelAudioPlay;
```



### Authorization Success

```
/*
* Check if authorization is successful
*/
-(NSInteger)isGetAuth;
```

### PCM Streaming

```
/*
* Start recording and playback
*/
-(void)toStartRuning;
```

```
/*
* Initialize session for a sentence or paragraph
*/
-(void)newSession;
```

```
/*
* Call finishSession when streaming ends (not when playback ends)
*/
-(void)finishSession;
```


```
/*
* Continue session after finishSession
*/
-(void)continueSession;
```

```
/*
* Mute/unmute
*/
-(void)toMute:(BOOL)isMute;
```


```
/*
* pcm
* size
* Refer to toSpeakWithPath for PCM conversion code
*/
-(void)toWavPcmData:(NSData*)audioData;
```

```
/*
* Clear audio buffer
*/
-(void)clearAudioBuffer;
```


```
/*
* Pause PCM audio streaming
*/
-(void)toPausePcm;
```


```
/*
* Resume PCM audio streaming
*/
-(void)toResumePcm;
```


```
/*
* Enable/disable recording
*/
-(void)toEnableRecord:(BOOL)isEnable;
```


```
/*
* Start audio streaming playback
*/
- (void)startPlaying;
```

```
/*
* Stop audio streaming playback
*/
- (void)stopPlaying:(void (^)( BOOL isSuccess))success;
```


## Actions

### Random Action
 
```
/*
* Call before starting action
* Random action (for text containing multiple audio segments, recommended to set at first audio start)
* return 0 - digital human model doesn't support random action 1 - supported
*/
-(NSInteger)toRandomMotion;
```

### Start Action

```
/*
* Start action (for text containing multiple audio segments, set at first audio start)
* return 0 - digital human model doesn't support start action 1 - supported
*/
-(NSInteger)toStartMotion;
```

### Stop Action
```
/*
* Stop action (for text containing multiple audio segments, set when last audio playback ends)
* isQuickly YES - end action immediately NO - wait for action to complete before silence
* return 0 - digital human model doesn't support stop action 1 - supported
*/
-(NSInteger)toSopMotion:(BOOL)isQuickly;
```

### Resume Digital Human Playback
```
/*
* Only needed after pausing digital human
*/
-(void)toPlay;
```

### Pause Digital Human Playback
```
/*
* Pause digital human playback
*/
-(void)toPause;
```

## Speech Recognition 

### Initialize Recording and ASR

```
/*
* Initialize recording and ASR
*/
-(void)initASR;
```

### Start Recognition

```
/*
* Start recognition
*/
-(void)toOpenAsr;
```

### Stop Recognition

```
/*
* Stop recognition
*/
-(void)toCloseAsr;
```

### Speech Recognition Callbacks

```
@property (nonatomic, copy) void (^asrBlock)(NSString * asrText,BOOL isFinish);

/*
 * data - recorded data (mono, 16000Hz sample rate)
 */
@property (nonatomic, copy) void (^recordDataBlock)(NSData * data);



/*
 * Volume callback
 */
@property (nonatomic, copy) void (^rmsBlock)(float rms);


@property (nonatomic, copy) void (^errBlock)(NSError *err);

/*
 * Server starts pushing audio stream
 */
@property (nonatomic, copy) void (^startPushBlock)(void);
/*
 * data - server returns audio stream (mono, 16000Hz sample rate)
 */
@property (nonatomic, copy) void (^pushDataBlock)(NSData * data);
/*
 * Server stops pushing audio stream
 */
@property (nonatomic, copy) void (^stopPushBlock)(void);

/*
 * Large model returns text
 */
@property (nonatomic, copy) void (^speakTextBlock)(NSString * speakText);

/*
 * Returns action marker
 */
@property (nonatomic, copy) void (^motionBlock)(NSString * motionText);
```

## Version History

**1.2.0**
```
1. Added PCM streaming support
```

**1.0.3**
```
1. Digital human background transparency
2. Fixed memory issues during decompression
```

**1.0.2**
```
1. Q&A functionality
2. Speech recognition
3. Text synthesis
4. Speaking animations
```


**1.0.1**
```
1. Local authorization and initialization for digital human
2. Local rendering for digital human
3. Audio playback and lip-sync driving
```
