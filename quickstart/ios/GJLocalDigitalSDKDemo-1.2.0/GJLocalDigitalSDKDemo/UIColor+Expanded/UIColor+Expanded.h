//
//  UIColor+Expanded.h
//  DrawYourSister
//
//  Created by piepie on 14-7-18.
//  Copyright (c) 2014年 toolwiz. All rights reserved.
//

#import <UIKit/UIKit.h>
#define RGBA_COLOR(R, G, B, A) [UIColor colorWithRed:((R) / 255.0f) green:((G) / 255.0f) blue:((B) / 255.0f) alpha:A]
#define RGB_COLOR(R, G, B) [UIColor colorWithRed:((R) / 255.0f) green:((G) / 255.0f) blue:((B) / 255.0f) alpha:1.0f]
@interface UIColor (Expanded)
+ (UIColor *)colorWithHexString:(NSString *)stringToConvert alpha:(float)alpha;
+ (UIColor *)colorWithRGBHex: (uint32_t)hex alpha:(float)alpha;
+ (UIColor *)colorWithHexString:(NSString *)color;

////从十六进制字符串获取颜色，
////color:支持@“#123456”、 @“0X123456”、 @“123456”三种格式
//+ (UIColor *)colorWithHexString:(NSString *)color alpha:(CGFloat)alpha;

- (UIImage *)colorToImage:(CGRect)rect;
@end
