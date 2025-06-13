//
//  GJLPCMManager.m
//  GJLocalDigitalDemo
//
//  Created by guiji on 2025/5/12.
//

#import "GJLPCMManager.h"
#import "GJLPLAssetReader.h"
#import "GJLGCDNEWTimer.h"
#import <GJLocalDigitalSDK/GJLocalDigitalSDK.h>

#define MAXDATALENGHT 64000
@interface GJLPCMManager()
@property (nonatomic, strong) GJLPLAssetReader *assetReader;
@property (nonatomic, strong) GJLGCDNEWTimer *audioTimer;
@property (nonatomic, strong) dispatch_queue_t audio_timer_queue;


@property (nonatomic, strong) dispatch_queue_t playAudioQueue;

@property (nonatomic, strong) dispatch_semaphore_t semaphore;

@property (nonatomic, assign) BOOL isPlaying;


//是否正在处理音频
@property (nonatomic, assign)BOOL isWaving;


@end
static GJLPCMManager * manager = nil;
@implementation GJLPCMManager

+ (GJLPCMManager *)manager
{
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        manager = [[GJLPCMManager alloc] init];
    });
    return manager;
}
-(id)init
{
    self=[super init];
    if(self)
    {

        
//        _pcmDataCache2 = [[NSMutableData alloc] init];
   
        self.audio_timer_queue=dispatch_queue_create("com.digitalsdk.audio_timer_queue", DISPATCH_QUEUE_SERIAL);
        self.playAudioQueue= dispatch_queue_create("com.digitalsdk.playAudioQueue", DISPATCH_QUEUE_SERIAL);
 
        
//        [self setupAudioSession];

        
    }
    return self;
}
- (void)speakWavPath:(NSString *)wavPath
{
   

    __weak typeof(self)weakSelf = self;

 
    AVAsset * asset;
   if([wavPath containsString:@"http"])
   {
       asset = [AVAsset assetWithURL:[NSURL URLWithString:wavPath]];
   }
    else
    {
        asset = [AVAsset assetWithURL:[NSURL fileURLWithPath:wavPath]];
    }

//    Float64 durationInSeconds = CMTimeGetSeconds(asset.duration);

  /*  NSLog(@"durationInSeconds:%f",durationInSeconds)*/;
    
    self.assetReader = [[GJLPLAssetReader alloc] initWithAVAsset:asset];
    self.assetReader.playStatus = ^(NSInteger status) {
        //开始
        if(status==1)
        {
  
            [weakSelf toStartAudioTimerAndPlaying];


        }
        
    };
    [self.assetReader seekTo:kCMTimeZero];

   
}

-(void)toStartAudioTimerAndPlaying
{
    __weak typeof(self)weakSelf = self;

    self.isPlaying=YES;

    

   
    
    
    [[GJLDigitalManager  manager] startPlaying];
    [self audioPushProc];

 
    self.audioTimer =[GJLGCDNEWTimer scheduledTimerWithTimeInterval:0.04 repeats:YES queue:self.audio_timer_queue block:^{
        
            [weakSelf audioPushProc];
        }];


}
-(void)toStopAudioTimer
{
    if(self.audioTimer!=nil)
    {
        [self.audioTimer invalidate];
        self.audioTimer=nil;
    }
   
}

// 音频数据
- (void)audioPushProc {

    @autoreleasepool {

        if(self.isPlaying)
        {
            if([self.assetReader hasAudio])
            {
                CMSampleBufferRef sample = [self.assetReader readAudioSampleBuffer];
          
                if (sample) {
                    
                    
                    CMBlockBufferRef blockBuffer = CMSampleBufferGetDataBuffer(sample);
                    size_t totolLength;
                    char *dataPointer = NULL;
                    CMBlockBufferGetDataPointer(blockBuffer, 0, NULL, &totolLength, &dataPointer);
                    //                   NSLog(@"totolLength:%ld",totolLength);
                    if (totolLength == 0 || !dataPointer) {
                        return;
                    }
                    
    //
                    NSData * data=[NSData dataWithBytes:dataPointer length:totolLength];
                    
                    [[GJLDigitalManager manager] toWavPcmData:data];
          


                    CFRelease(sample);
                }
                else
                {
                    //推流结束调用finishSession
                    NSLog(@"推流结束");
                    [self toStop];
                    [[GJLDigitalManager manager] finishSession];
//
//                    NSString * filepath=[[NSBundle mainBundle] pathForResource:@"3.wav" ofType:nil];
//                    [self speakWavPath:filepath];
                }
        }
        
            
  
        }
        
        

  }
}


-(void)toStop
{
    self.isPlaying=NO;
    [self toStopAudioTimer];
//    [self toStopAudioTimer2];
    if(self.assetReader!=nil)
    {
        [self.assetReader toCancelRead];
    }

}

#pragma mark-----------播放本地音频或url网络音频----------------------
-(void)toSpeakWithPath:(NSString*)wavPath
{
   

    __weak typeof(self)weakSelf = self;
//    [[GJLDigitalManager manager] toMute:NO];
    [self toStop];

    dispatch_async(self.playAudioQueue, ^{

//
         NSString *localPath = [wavPath stringByReplacingOccurrencesOfString:@"file://" withString:@""];
        dispatch_async(dispatch_get_main_queue(), ^{
            //一个动作区间来回震荡，1 支持随机动作 0 不支持随机动作
//            NSInteger rst= [[GJLDigitalManager manager] toActRangeMinAndMax];
            [weakSelf speakWavPath:localPath];
      
            
//            
        });


    });
  
    
}


@end
