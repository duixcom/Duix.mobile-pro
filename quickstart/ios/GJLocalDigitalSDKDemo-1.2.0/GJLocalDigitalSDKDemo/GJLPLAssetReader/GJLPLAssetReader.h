
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

typedef NS_ENUM(NSInteger, MAVPlayerStatus) {
    MAVPlayerStatusUnknown = 0,
    MAVPlayerStatusReadyToPlay = 1,
    MAVPlayerStatusFailed = 2
};
@interface GJLPLAssetReader : NSObject

@property (strong, nonatomic) AVAssetReader *assetReader;
@property (nonatomic, readonly) BOOL hasAudio;
@property (nonatomic, readonly) BOOL hasVideo;

//播放状态回调
@property (nonatomic, copy) void (^playStatus)(NSInteger status);
- (instancetype)initWithURL:(NSURL *)url;
- (instancetype)initWithAVAsset:(AVAsset *)asset;
- (void)seekTo:(CMTime)time;

- (void)getVideoInfo:(int *)width height:(int *)height frameRate:(float *)fps duration:(CMTime *)duration;

-(void)toStartRead;

-(void)toCancelRead;

- (CMSampleBufferRef)readVideoSampleBuffer;

- (CMSampleBufferRef)readAudioSampleBuffer;;



@end
