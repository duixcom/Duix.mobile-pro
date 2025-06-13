//
//  LivePCMViewController.m
//  GJLocalDigitalDemo
//
//  Created by guiji on 2025/5/20.
//

#import "LivePCMViewController.h"
#import "HttpClient.h"
#import "SVProgressHUD.h"
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import <Security/Security.h>
#import <GJLocalDigitalSDK/GJLocalDigitalSDK.h>

//#import <CoreTelephony/CTCellularData.h>
#import "GJCheckNetwork.h"
#import "SSZipArchive.h"
#import "GJDownWavTool.h"
#import "GYAccess.h"
#import "GJLPCMManager.h"
#import "GJLGCDNEWTimer.h"
#import "OpenUDID.h"
#import "UIColor+Expanded.h"
@interface LivePCMViewController ()
@property(nonatomic,strong)UIView *showView;
@property (nonatomic, strong) UIImageView * imageView;

@property (nonatomic, strong)UILabel * questionLabel;

@property (nonatomic, strong)UILabel * answerLabel;
@end

@implementation LivePCMViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    

    
    
    self.view.backgroundColor=[UIColor blackColor];
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    [audioSession setCategory:AVAudioSessionCategoryPlayAndRecord withOptions: AVAudioSessionCategoryOptionDefaultToSpeaker|AVAudioSessionCategoryOptionAllowBluetooth error:nil];
    [audioSession setPreferredIOBufferDuration:0.02 error:nil];

    [audioSession setActive:YES error:nil];
    

    [self.view addSubview:self.imageView];
    
    [self.view addSubview:self.showView];

    [self.view addSubview:self.questionLabel];
    
    [self.view addSubview:self.answerLabel];
    
    UIButton * stopbtn=[UIButton buttonWithType:UIButtonTypeCustom];
    stopbtn.frame=CGRectMake((self.view.frame.size.width-40)/2, self.view.frame.size.height-100, 40, 40);
    [stopbtn setTitle:@"结束" forState:UIControlStateNormal];
    [stopbtn setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    [stopbtn addTarget:self action:@selector(toStop) forControlEvents:UIControlEventTouchDown];
    [self.view addSubview:stopbtn];
    [self toDigitalBlock];
    [self  toStart];
    
    // Do any additional setup after loading the view.
}

-(UIImageView*)imageView
{
    if(nil==_imageView)
    {
        _imageView=[[UIImageView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
        NSString *bgpath =[NSString stringWithFormat:@"%@/%@",[[NSBundle mainBundle] bundlePath],@"bg2.jpg"];
        _imageView.contentMode=UIViewContentModeScaleAspectFill;
        _imageView.image=[UIImage imageWithContentsOfFile:bgpath];
        
    }
    return _imageView;
}
-(UIView*)showView
{
    if(nil==_showView)
    {
        _showView=[[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
        _showView.backgroundColor=[UIColor clearColor];
    }
    return _showView;
}
-(UILabel*)questionLabel
{
    if(nil==_questionLabel)
    {
        _questionLabel=[[UILabel alloc] initWithFrame:CGRectMake(40, self.view.frame.size.height-160, self.view.frame.size.width-50, 40)];
        _questionLabel.backgroundColor=[UIColor colorWithHexString:@"#000000" alpha:0.3];
        _questionLabel.numberOfLines=0;
        _questionLabel.textColor=[UIColor whiteColor];
        _questionLabel.font=[UIFont systemFontOfSize:12];
        _questionLabel.textAlignment=NSTextAlignmentLeft;
        _questionLabel.hidden=YES;
    }
    return _questionLabel;
}
-(UILabel*)answerLabel
{
    if(nil==_answerLabel)
    {
        _answerLabel=[[UILabel alloc] initWithFrame:CGRectMake(10, self.view.frame.size.height-220, self.view.frame.size.width-40, 40)];
        _answerLabel.backgroundColor=[UIColor colorWithHexString:@"#000000" alpha:0.3];
        _answerLabel.numberOfLines=0;
        _answerLabel.textColor=[UIColor whiteColor];
        _answerLabel.font=[UIFont systemFontOfSize:12];
        _answerLabel.textAlignment=NSTextAlignmentLeft;
        
        _answerLabel.hidden=YES;
    }
    return _answerLabel;
}
-(void)toStart
{
    __weak typeof(self)weakSelf = self;
    //授权


    [GJLDigitalConfig shareConfig].appName= [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleDisplayName"];
    [GJLDigitalConfig shareConfig].userId = [NSString stringWithFormat:@"sdk_%@",[OpenUDID value]];

    [[GJLDigitalManager manager] initWithAppId:self.appId appKey:self.appKey conversationId:self.conversationId block:^(BOOL isSuccess, NSString *errorMsg) {
        if(isSuccess)
        {
    
            NSInteger result=   [[GJLDigitalManager manager] initBaseModel:weakSelf.basePath digitalModel:weakSelf.digitalPath showView:weakSelf.showView];
             if(result==1)
             {
                //开始
 //                NSString *bgpath =[NSString stringWithFormat:@"%@/%@",[[NSBundle mainBundle] bundlePath],@"bg2.jpg"];
 //                [[GJLDigitalManager manager] toChangeBBGWithPath:bgpath];
                 [[GJLDigitalManager manager] toStart:^(BOOL isSuccess, NSString *errorMsg) {
                     if(isSuccess)
                     {
                         
                         dispatch_async(dispatch_get_main_queue(), ^{
                      
                             weakSelf.questionLabel.hidden=NO;
                             weakSelf.answerLabel.hidden=NO;
                         
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
         
   
  
}
-(void)initASR
{

    __weak typeof(self)weakSelf = self;
    [[GJLASRManager manager] initASR];
    //语音识别回调

    [GJLASRManager manager].asrBlock = ^(NSString *asrText, BOOL isFinish) {
        //注意必须主线程刷新UI
        
        dispatch_async(dispatch_get_main_queue(), ^{
            weakSelf.questionLabel.text=asrText;
        });
 

//

        
        
    };
    
    [GJLASRManager manager].speakTextBlock = ^(NSString *speakText) {
        dispatch_async(dispatch_get_main_queue(), ^{
            weakSelf.answerLabel.text=speakText;
        });
    };

    [GJLASRManager manager].startPushBlock = ^{
        
            [[GJLDigitalManager manager] finishSession];
            [[GJLDigitalManager manager] newSession];

     
    
    };
    
    [GJLASRManager manager].motionBlock = ^(NSString *motionText) {
        BOOL isMacth=   [[GJLDigitalManager manager] toMotionByName:motionText];
        if(isMacth)
        {
           [[GJLDigitalManager manager] toStartMotion];
        }
    
   
     
    
    };
    
    [GJLASRManager manager].stopPushBlock = ^{
        
        [[GJLDigitalManager manager] finishSession];


    };
    
    
    [GJLASRManager manager].pushDataBlock = ^(NSData *data) {

        [[GJLDigitalManager manager] toWavPcmData:data];


   
    };

}

#pragma mark ------------回调----------------
-(void)toDigitalBlock
{
    
    __weak typeof(self)weakSelf = self;
    [GJLDigitalManager manager].playFailed = ^(NSInteger code, NSString *errorMsg) {

            [SVProgressHUD showInfoWithStatus:errorMsg];

      
    };
    [GJLDigitalManager manager].audioPlayEnd = ^{
//        [weakSelf moviePlayDidEnd];
        NSLog(@"播放结束");
      
        [[GJLDigitalManager manager] clearAudioBuffer];
        [[GJLDigitalManager manager] toSopMotion:NO];
     
    };
    
    [GJLDigitalManager manager].audioPlayProgress = ^(float current, float total) {
        
    };
    [GJLDigitalManager manager].onRenderReportBlock = ^(int resultCode, BOOL isLip, float useTime) {
//        NSLog(@"resultCode:%d,isLip:%d,useTime:%f",resultCode,isLip,useTime);
    };
}

#pragma mark ------------结束所有----------------
-(void)toStop
{
    


    self.questionLabel.hidden=YES;
    self.answerLabel.hidden=YES;
    //停止绘制
    [[GJLDigitalManager manager] toStop];
    
    [[GJLASRManager manager] toCloseAsr];
    
    [self dismissViewControllerAnimated:YES completion:^{
        
    }];
}
/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
