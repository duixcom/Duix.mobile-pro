//
//  ViewController.m
//  GJLocalDigitalDemo
//
//  Created by guiji on 2023/12/12.
//

#import "ViewController.h"
#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>

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
#import "ReadWavPCMViewController.h"
#import "LivePCMViewController.h"
//
//基础模型 git 地址下载较慢，请下载后自己管理加速
#define BASEMODELURL   @"https://github.com/GuijiAI/duix.ai/releases/download/v1.0.0/gj_dh_res.zip"
//////数字人模型 git 地址下载较慢，请下载后自己管理加速
#define DIGITALMODELURL @"https://github.com/GuijiAI/duix.ai/releases/download/v1.0.0/bendi3_20240518.zip"


#define APPID  @""
#define APPKEY @""

#define CONVERSATIONID  @""



@interface ViewController ()<GJDownWavToolDelegate,UITextFieldDelegate>
@property(nonatomic,strong)UIView *showView;
@property(nonatomic,strong)NSString * basePath;
@property(nonatomic,strong)NSString * digitalPath;
@property (nonatomic, assign) BOOL isRequest;
//会话id
@property (nonatomic, strong)UITextField * conversationTextField;

//appid
@property (nonatomic, strong)UITextField * appIDTextField;


//appkey
@property (nonatomic, strong)UITextField * appkeyTextField;



@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    self.view.backgroundColor=[UIColor whiteColor];
    

    

    

    
    self.conversationTextField=[[UITextField alloc] init];
    self.conversationTextField.frame=CGRectMake(48, 200, self.view.frame.size.width-96, 44);
    self.conversationTextField.backgroundColor = [UIColor clearColor];
 //        _phoneTextField.layer.borderColor=[UIColor colorWithHexString:@"#FFFFFF" alpha:0.29].CGColor;
 //        _phoneTextField.layer.borderWidth=1;
     self.conversationTextField.layer.masksToBounds = YES;
     self.conversationTextField.delegate = self;
     self.conversationTextField.layer.cornerRadius = 10;
     self.conversationTextField.layer.borderColor = [UIColor redColor].CGColor;
     self.conversationTextField.layer.borderWidth = 1;
     self.conversationTextField.returnKeyType=UIReturnKeyDone;
 //    self.textField.placeholder=@"会话ID";
     UIView *paddingView1 = [[UIView alloc] initWithFrame:CGRectMake(0, 0,130,44)];
     paddingView1.backgroundColor = [UIColor clearColor];
     UILabel *label1=[[UILabel alloc] initWithFrame:CGRectMake(10, 0,120,44)];
     label1.text=@"conversationId:";
     label1.textColor=[UIColor blackColor];
     label1.textAlignment=NSTextAlignmentLeft;
     [paddingView1 addSubview:label1];
     self.conversationTextField.leftView = paddingView1;
     self.conversationTextField.leftViewMode = UITextFieldViewModeAlways;
     [self.view addSubview:self.conversationTextField];
     
  
     
     
     self.appIDTextField=[[UITextField alloc] init];
      self.appIDTextField.frame=CGRectMake(48,260, self.view.frame.size.width-96, 44);
     self.appIDTextField.backgroundColor = [UIColor clearColor];
  //        _phoneTextField.layer.borderColor=[UIColor colorWithHexString:@"#FFFFFF" alpha:0.29].CGColor;
  //        _phoneTextField.layer.borderWidth=1;
      self.appIDTextField.layer.masksToBounds = YES;
      self.appIDTextField.delegate = self;
      self.appIDTextField.layer.cornerRadius = 10;
      self.appIDTextField.layer.borderColor = [UIColor redColor].CGColor;
      self.appIDTextField.layer.borderWidth = 1;
      self.appIDTextField.returnKeyType=UIReturnKeyDone;
     UIView *paddingView2 = [[UIView alloc] initWithFrame:CGRectMake(0, 0,90,44)];
     paddingView2.backgroundColor = [UIColor clearColor];
     UILabel *label2=[[UILabel alloc] initWithFrame:CGRectMake(10, 0,70,44)];
     label2.text=@"appId:";
     label2.textColor=[UIColor blackColor];
     label2.textAlignment=NSTextAlignmentLeft;
     [paddingView2 addSubview:label2];
     self.appIDTextField.leftView = paddingView2;
     self.appIDTextField.leftViewMode = UITextFieldViewModeAlways;
      [self.view addSubview:self.appIDTextField];

     
     
     self.appkeyTextField=[[UITextField alloc] init];
      self.appkeyTextField.frame=CGRectMake(48,320, self.view.frame.size.width-96, 44);
     self.appkeyTextField.backgroundColor = [UIColor clearColor];
  //        _phoneTextField.layer.borderColor=[UIColor colorWithHexString:@"#FFFFFF" alpha:0.29].CGColor;
  //        _phoneTextField.layer.borderWidth=1;
      self.appkeyTextField.layer.masksToBounds = YES;
      self.appkeyTextField.delegate = self;
      self.appkeyTextField.layer.cornerRadius = 10;
      self.appkeyTextField.layer.borderColor = [UIColor redColor].CGColor;
      self.appkeyTextField.layer.borderWidth = 1;
      self.appkeyTextField.returnKeyType=UIReturnKeyDone;
     UIView *paddingView3 = [[UIView alloc] initWithFrame:CGRectMake(0, 0,90,44)];
     paddingView3.backgroundColor = [UIColor clearColor];
     UILabel *label3=[[UILabel alloc] initWithFrame:CGRectMake(10, 0,70,44)];
     label3.text=@"appKey:";
     label3.textColor=[UIColor blackColor];
     label3.textAlignment=NSTextAlignmentLeft;
     [paddingView3 addSubview:label3];
     self.appkeyTextField.leftView = paddingView3;
     self.appkeyTextField.leftViewMode = UITextFieldViewModeAlways;
      [self.view addSubview:self.appkeyTextField];
   

    
    UIButton * startbtn=[UIButton buttonWithType:UIButtonTypeCustom];
    startbtn.frame=CGRectMake(40, self.view.frame.size.height-200, self.view.frame.size.width-80, 40);
    [startbtn setTitle:@"StartWAV" forState:UIControlStateNormal];
    [startbtn setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    [startbtn addTarget:self action:@selector(toStartWav) forControlEvents:UIControlEventTouchDown];
    [self.view addSubview:startbtn];
    


    UIButton * startbtn2=[UIButton buttonWithType:UIButtonTypeCustom];
    startbtn2.frame=CGRectMake(40, self.view.frame.size.height-160, self.view.frame.size.width-80, 40);
    [startbtn2 setTitle:@"StartPCM" forState:UIControlStateNormal];
    [startbtn2 setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    [startbtn2 addTarget:self action:@selector(toStartPCM) forControlEvents:UIControlEventTouchDown];
    [self.view addSubview:startbtn2];

    
    NSUserDefaults * defaults=[NSUserDefaults standardUserDefaults];


    self.conversationTextField.text=[defaults objectForKey:@"ConversationIdKEY"]?:CONVERSATIONID;
    self.appIDTextField.text=[defaults objectForKey:@"APPIDKEY"]?:APPID;
    self.appkeyTextField.text=[defaults objectForKey:@"APPKEY"]?:APPKEY;
    [[GJCheckNetwork manager] getWifiState];
    __weak typeof(self)weakSelf = self;
    [GJCheckNetwork manager].on_net = ^(NetType state) {
        if (state == Net_WWAN
            || state == Net_WiFi) {
            if (!weakSelf.isRequest) {
                weakSelf.isRequest = YES;
   
                [weakSelf isDownModel];
            }
        }
    };
   



 
    

}
-(void)toStartWav
{
    if(![self isFileExit])
    {
        return;
    }
    ReadWavPCMViewController * vc=[[ReadWavPCMViewController alloc] init];
    vc.basePath=self.basePath;
    vc.digitalPath=self.digitalPath;
    vc.appId=self.appIDTextField.text;//注意不要有空格
    vc.appKey=self.appkeyTextField.text;//注意不要有空格
    vc.conversationId=self.conversationTextField.text;////注意不要有空格
    vc.modalPresentationStyle=UIModalPresentationFullScreen;
    [self presentViewController:vc animated:YES completion:^{
        
    }];
}
-(void)toStartPCM
{
    if(![self isFileExit])
    {
        return;
    }
   
    LivePCMViewController * vc=[[LivePCMViewController alloc] init];
    vc.basePath=self.basePath;
    vc.digitalPath=self.digitalPath;
    vc.appId=self.appIDTextField.text;//注意不要有空格
    vc.appKey=self.appkeyTextField.text;//注意不要有空格
    vc.conversationId=self.conversationTextField.text;////注意不要有空格
     vc.modalPresentationStyle=UIModalPresentationFullScreen;
    [self presentViewController:vc animated:YES completion:^{
        
    }];
}
-(BOOL)isFileExit
{
    if(![[NSFileManager defaultManager] fileExistsAtPath:self.basePath])
    {
        NSLog(@"基础模型不存在");
        return NO;
    }
    
    if(![[NSFileManager defaultManager] fileExistsAtPath:self.digitalPath])
    {
        NSLog(@"模版不存在");
        return NO;
    }
    if(self.appIDTextField.text.length==0)
    {
        return NO;
    }
    if(self.appkeyTextField.text.length==0)
    {
        return NO;
    }
    
    return YES;
}


-(void)isDownModel
{
    NSString *unzipPath = [self getHistoryCachePath:@"unZipCache"];
    NSString * baseName=[[BASEMODELURL lastPathComponent] stringByDeletingPathExtension];
    self.basePath=[NSString stringWithFormat:@"%@/%@",unzipPath,baseName];
    
    NSString * digitalName=[[DIGITALMODELURL lastPathComponent] stringByDeletingPathExtension];
    self.digitalPath=[NSString stringWithFormat:@"%@/%@",unzipPath,digitalName];

    NSFileManager * fileManger=[NSFileManager defaultManager];
    if((![fileManger fileExistsAtPath:self.basePath])&&(![fileManger fileExistsAtPath:self.digitalPath]))
    {
        //下载基础模型和数字人模型
        [self toDownBaseModelAndDigital];

    }
   else if (![fileManger fileExistsAtPath:self.digitalPath])
    {
        //数字人模型
        [SVProgressHUD show];
        [SVProgressHUD setDefaultMaskType:SVProgressHUDMaskTypeBlack];
        [self toDownDigitalModel];
    }
    

}
//下载基础模型----不同的数字人模型使用同一个基础模型
-(void)toDownBaseModelAndDigital
{
    [SVProgressHUD show];
    [SVProgressHUD setDefaultMaskType:SVProgressHUDMaskTypeBlack];
    __weak typeof(self)weakSelf = self;
    NSString *zipPath = [self getHistoryCachePath:@"ZipCache"];
    //下载基础模型
    [[HttpClient manager] downloadWithURL:BASEMODELURL savePathURL:[NSURL fileURLWithPath:zipPath] pathExtension:nil progress:^(NSProgress * progress) {
        double down_progress=(double)progress.completedUnitCount/(double)progress.totalUnitCount*0.5;
        [SVProgressHUD showProgress:down_progress status:@"正在下载基础模型"];
    } success:^(NSURLResponse *response, NSURL *filePath) {
        NSLog(@"filePath:%@",filePath);
        
        [weakSelf toUnzip:filePath.absoluteString];
        //下载数字人模型
        [weakSelf  toDownDigitalModel];
  
    } fail:^(NSError *error) {
        [SVProgressHUD showErrorWithStatus:error.localizedDescription];
    }];
}
-(void)toUnzip:(NSString*)filePath
{
    filePath=[filePath stringByReplacingOccurrencesOfString:@"file://" withString:@""];
    NSString *unzipPath = [self getHistoryCachePath:@"unZipCache"];
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),^{
        [SSZipArchive unzipFileAtPath:filePath toDestination:unzipPath progressHandler:^(NSString * _Nonnull entry, unz_file_info zipInfo, long entryNumber, long total) {
            
        } completionHandler:^(NSString * _Nonnull path, BOOL succeeded, NSError * _Nullable error) {
            NSLog(@"path:%@,%d",path,succeeded);
        
        }];
    });
 
 
}
//下载数字人模型
-(void)toDownDigitalModel
{
    __weak typeof(self)weakSelf = self;
    NSString *zipPath = [self getHistoryCachePath:@"ZipCache"];
    [[HttpClient manager] downloadWithURL:DIGITALMODELURL savePathURL:[NSURL fileURLWithPath:zipPath] pathExtension:nil progress:^(NSProgress * progress) {
        double down_progress=0.5+(double)progress.completedUnitCount/(double)progress.totalUnitCount*0.5;
        [SVProgressHUD showProgress:down_progress status:@"正在下载数字人模型"];
    } success:^(NSURLResponse *response, NSURL *filePath) {
        NSLog(@"filePath:%@",filePath);
        [weakSelf toUnzip:filePath.absoluteString];
        [SVProgressHUD showSuccessWithStatus:@"下载成功"];
    } fail:^(NSError *error) {
        [SVProgressHUD showErrorWithStatus:error.localizedDescription];
    }];
}



-(NSString *)getHistoryCachePath:(NSString*)pathName
{
    NSString* folderPath =[[self getFInalPath] stringByAppendingPathComponent:pathName];
    //创建文件管理器
    NSFileManager *fileManager = [NSFileManager defaultManager];
    //判断temp文件夹是否存在
    BOOL fileExists = [fileManager fileExistsAtPath:folderPath];
    //如果不存在说创建,因为下载时,不会自动创建文件夹
    if (!fileExists)
    {
        [fileManager createDirectoryAtPath:folderPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    return folderPath;
}

- (NSString *)getFInalPath
{
    NSString* folderPath =[[NSHomeDirectory() stringByAppendingPathComponent:@"Library"] stringByAppendingPathComponent:@"GJCache"];
    //创建文件管理器
    NSFileManager *fileManager = [NSFileManager defaultManager];
    //判断temp文件夹是否存在
    BOOL fileExists = [fileManager fileExistsAtPath:folderPath];
    //如果不存在说创建,因为下载时,不会自动创建文件夹
    if (!fileExists) {
        [fileManager createDirectoryAtPath:folderPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    return folderPath;
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    
    NSUserDefaults * defaults=[NSUserDefaults standardUserDefaults];
     if(textField==self.conversationTextField)
     {
        
         [defaults setObject:string forKey:@"ConversationIdKEY"];
     }
    else if(textField==self.appIDTextField)
    {
        [defaults setObject:string forKey:@"APPIDKEY"];
    }
    else if(textField==self.appkeyTextField)
    {
        [defaults setObject:string forKey:@"APPKEY"];
    }
    return YES;
}
- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    
  
    return YES;
}



@end
