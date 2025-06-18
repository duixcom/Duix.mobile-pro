# Duix Mobile Pro

本文介绍了如何使用硅基数字人服务提供的Duix Mobile Pro SDK，包括安装、关键接口及代码示例。

## 一. 添加依赖
引入 aar 包: duix_client_sdk_pro_release_${version}.aar  
app 目录新建 libs 目录,放入 aar 包.
在 build.gradle 中增加配置如下:

```
dependencies {
    implementation fileTree(include: ['*.jar', '*.aar'], dir: 'libs')   // 加载libs文件夹下的aar包

    implementation 'com.auth0:java-jwt:3.18.1'                  // (必选)，需要的签名工具
    implementation 'org.java-websocket:Java-WebSocket:1.5.1'    // (可选)，在LiveKit交互模式中需要该依赖
}
```

## 二. Proguard混淆配置

在项目的proguard-rules.pro文件中添加如下配置:

```
-keep class ai.guiji.duix.DuixNcnn {*; }
```

## 三. 快速启动
在Activity中集成数字人模块代码, 如需使用麦克风，请确保启动前已经动态获取麦克风权限。
```kotlin
class PlayActivity : BaseActivity() {

    companion object {
        const val GL_CONTEXT_VERSION = 2
    }

    private lateinit var modelPath: String

    private lateinit var binding: ActivityPlayBinding
    private var player: Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ...

        val renderer =
            DUIXRenderer(binding.glTextureView)
        binding.glTextureView.setEGLContextClientVersion(GL_CONTEXT_VERSION)
        binding.glTextureView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        binding.glTextureView.isOpaque = false           // 透明
        binding.glTextureView.setRenderer(renderer)
        binding.glTextureView.renderMode =
            GLSurfaceView.RENDERMODE_WHEN_DIRTY      // 一定要在设置完Render之后再调用

        player = Player(mContext, renderer, playerCallback)
        player?.init(modelPath)

        binding.btnPlayAudio.setOnClickListener {
            val thread = Thread {
                player?.startPush()
                val inputStream = assets.open("pcm/3.pcm")
                val buffer = ByteArray(1280)
                while (inputStream.read(buffer) != -1){
                    val data = buffer.clone()
                    player?.pushPcm(data)
                }
                player?.stopPush()
            }
            thread.start()
        }
        binding.btnMotion.setOnClickListener {
            player?.requireMotion("打招呼")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }

    private val playerCallback = object : Player.Callback {
        override fun onInitSuccess(modelInfo: String) {
            runOnUiThread {
                Log.e(TAG, "onInitSuccess modelInfo: $modelInfo")
                Toast.makeText(mContext, "Init success", Toast.LENGTH_SHORT).show()
                binding.btnPlayAudio.isEnabled = true
                binding.btnMotion.isEnabled = true
            }
        }

        override fun onInitError(code: Int, subCode: Int, msg: String?) {
            runOnUiThread {
                Toast.makeText(mContext, "Init error $code $subCode, $msg", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        override fun onPlayStart() {
            Log.e(TAG, "onPlayStart")
        }

        override fun onPlayEnd() {
            Log.e(TAG, "onPlayEnd")
        }

        override fun onPlayError(code: Int, msg: String?) {
            Log.e(TAG, "onPlayError code: $code msg: $msg")
        }

        override fun onMotionStart(name: String?) {
            Log.e(TAG, "onMotionStart name: $name")
        }

        override fun onMotionComplete(name: String?) {
            Log.e(TAG, "onMotionComplete name: $name")
        }

    }
}
```

## 四. SDK渲染功能调用及API说明

### 1. 初始化服务

init接口定义:
ai.guiji.duix.sdk.client.pro.VirtualFactory

使用平台账户中提供的appId和appSecret初始化模块
```
/**
 * 初始化，使用默认的工作路径(/sdcard/Android/data/包名/files/duix/)
 */
int init(Context context, String appId, String appKey);

/**
 * 自定义工作路径初始化
 */
int init(String appId, String appKey, String workPath)
```

参数说明：

| 参数         | 类型       | 描述               |
|------------|----------|------------------|
| context    | Context  | 非空,App上下文        |
| appId      | String   | 非空，平台生成的appId    |
| appKey     | String   | 非空，平台生成的appKey   |
| workPath   | String   | 自定义工作路径          |


返回参数说明：

| 取值    | 说明        |  
|-------|-----------|
| 0     | 初始化成功     | 
| -1000 | 设置文件路径失败  | 
| -1001 | Context为空 | 
| -1002 | appId为空   | 
| -1003 | appKey为空  | 


参考demo App实例:
ai.guiji.duix.sdk.client.pro.demo.ui.activity.MainActivity  
demo为方便更换appId调试在MainActivity中设置，实际也可以在Application进行SDK初始化

```kotlin
VirtualFactory.init(mContext, mAppId, mAppKey)
```

### 2. 模型及配置文件下载
函数定义:  
ai.guiji.duix.sdk.client.pro.VirtualModelUtil

#### (2.1) 基础配置文件检查

函数定义:  
ai.guiji.duix.sdk.client.pro.VirtualModelUtil

```
/**
 * 返回基础配置是否下载完成
 */
boolean checkBaseConfig();
```

#### (2.2) 模型文件检查

函数定义:  
ai.guiji.duix.sdk.client.pro.VirtualModelUtil

```
/**
 * 返回模型文件是否下载完成
 */
boolean checkModel(String name)
```

参数说明：

| 参数       | 类型     | 描述             |
|----------|--------|----------------|
| name     | String | 模型文件的下载连接或模型名称 |


#### (2.3) 基础配置文件下载

函数定义:  
ai.guiji.duix.sdk.client.pro.VirtualModelUtil

```
/**
 * 启动基础配置文件下载
 */
void baseConfigDownload(Context context, String url, ModelDownloadCallback callback)
```

参数说明：

| 参数          | 类型                         | 描述          |
|-------------|----------------------------|-------------|
| context     | Context                    | App上下文      |
| url         | String                     | 配置文件的下载地址   |
| callback    | ModelDownloadCallback      | 配置下载的回调接口   |

其中ModelDownloadCallback的接口定义:  
ai.guiji.duix.sdk.client.pro.callback.ModelDownloadCallback

```
/**
 * 下载完成的事件
 * @param url 下载链接
 * @param dir 模型文件夹的保存路径
 */
void onDownloadComplete(String url, File dir)

/**
 * 下载失败的事件
 * @param url 下载链接
 * @param code 异常码
 * @param msg 异常消息
 */
void onDownloadFail(String url, int code, String msg)

/**
 * 下载进度
 * @param url 下载链接
 * @param current 当前下载的字节进度
 * @param total 需要下载zip文件大小
 */
void onDownloadProgress(String url, long current, long total)

/**
 * zip文件解压进度
 * @param url 下载链接
 * @param current 当前解压的字节进度
 * @param total 需要解压的zip文件大小
 */
void onUnzipProgress(String url, long current, long total)
```

#### (2.4) 模型文件下载

函数定义:  
ai.guiji.duix.sdk.client.pro.VirtualModelUtil

```
/**
 * 启动基础配置文件下载
 */
void modelDownload(Context context, String modelUrl, ModelDownloadCallback callback)
```

参数说明：

| 参数            | 类型                         | 描述        |
|---------------|----------------------------|-----------|
| context       | Context                    | App上下文    |
| modelUrl      | String                     | 模型文件的下载地址 |
| callback      | ModelDownloadCallback      | 模型下载的回调接口 |


### 3. 创建会话并启动数字人

函数定义:
ai.guiji.duix.sdk.client.pro.Player

```
/**
 * @param sink 渲染控件接口
 * @param callback 事件回调
 */
Player(Context context, RenderSink sink, Callback callback) 

void init(String modelUrl)
```

其中Callback定义：
```
public interface Callback {

        void onInitSuccess(String modelInfo);           // 初始化完成

        void onInitError(int code, int subCode, String msg);    // 初始化异常

        void onPlayStart();                             // 音频开始播放

        void onPlayEnd();                               // 音频播放完成

        void onPlayError(int code, String msg);         // 播放异常

        void onMotionStart(String name);                // 开始播放动作区间

        void onMotionComplete(String name);             // 动作播放完毕
       
        /**
        * 帧渲染计算统计
        * @param resultCode 是否正常返回帧数据, <0 则说明帧数据异常
        * @param isLip      是否计算唇形
        * @param useTime    帧计算消耗的时间
        */
        default void onRenderReport(int resultCode, boolean isLip, long useTime){}  
    }
```

**回调在渲染线程中发出,不要在回调中进行耗时操作**

onInitError中code的取值:

| 取值    | 说明       |
|-------|----------|
| -1000 | 授权异常     |
| -1001 | 模型加载异常   |


调用示例:
```
val renderer =
            DUIXRenderer(binding.glTextureView)
binding.glTextureView.setEGLContextClientVersion(GL_CONTEXT_VERSION)
binding.glTextureView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
binding.glTextureView.isOpaque = false           // 透明
binding.glTextureView.setRenderer(renderer)
binding.glTextureView.renderMode =
            GLSurfaceView.RENDERMODE_WHEN_DIRTY      // 一定要在设置完Render之后再调用
player = Player(mContext, renderer, playerCallback)
player?.init(modelPath)
```



### 4. 播放音频流

需要提供1600采样率，单通道，16bit深度的PCM数据。在调用startPush()后不断调用pushPcm(data)接口推送数据,推送长度不宜过长，在一段话讲完后调用stopPush()结束推送。

函数定义:
ai.guiji.duix.sdk.client.pro.Player

```
public void startPush()

public void pushPcm(byte[] buffer)

// 注意这个不是停止播放，而是停止推送，已经推送的音频会继续播放。
public void stopPush()
```

调用示例:
```
val thread = Thread {
                player?.startPush()
                val inputStream = assets.open("pcm/3.pcm")
                val buffer = ByteArray(1280)
                while (inputStream.read(buffer) != -1){
                    val data = buffer.clone()
                    player?.pushPcm(data)
                }
                player?.stopPush()
            }
thread.start()
```

### 5. 停止音频播放
函数定义:
ai.guiji.duix.sdk.client.pro.Player

```
// 调用该函数已经推送的音频会被丢弃，立刻恢复到静默状态
public void stopPlayAudio()
```

### 6. 数字人播放动作

播放动作需要模型支持动作标签，在初始化成功的回调中可以看到模型信息。

函数定义:
ai.guiji.duix.sdk.client.pro.Player

```
/**
     * 
     * @param name 动作标签名
     * @param now 是否立即播放。
     *            true: 请求后打断当前所有动作立刻播放该动作区间；
     *            false： 将动作区间添加到播放序列，在静默区间的下一个合适点开始播放动作，该模式可能等待时间较长但是过渡会更平滑。
     */
public void requireMotion(String name, boolean now)
```

调用示例：

```
player?.requireMotion("打招呼")
```

### 7. 音量控制

音量控制范围0.0F~1.0F, 0.0F: 静音、1.0F: 最大音量

函数定义：

ai.guiji.duix.sdk.client.pro.Player

```
public void setVolume(float volume)
```

调用示例：

```
player?.setVolume(1.0F)
```



## 五. LiveKit方案调用说明

LiveKit方案集成了前端渲染，后端识别、大模型答疑、音频推流等整套流程。

### 1. 初始化服务(同SDK常规初始化)

### 2. 模型及配置文件下载(同SDK常规初始化)
### 3. 创建会话并启动数字人

函数定义:
ai.guiji.duix.sdk.client.pro.Player

```
/**
 * @param sink 渲染控件接口
 * @param callback 事件回调
 */
Player(Context context, RenderSink sink, Callback callback) 

/**
 * @param conversationId 后台会话ID
 * @param modelUrl 模型下载地址或已下载的模型名称
 */
void init(String conversationId, String modelUrl)
```

其中Callback定义：
```
public interface Callback {

        void onInitSuccess(String modelInfo);           // 初始化完成

        void onInitError(int code, int subCode, String msg);    // 初始化异常

        void onPlayStart();                             // 音频开始播放

        void onPlayEnd();                               // 音频播放完成

        void onPlayError(int code, String msg);         // 播放异常

        void onMotionStart(String name);                // 开始播放动作区间

        void onMotionComplete(String name);             // 动作播放完毕

        // liveKIT 回调
        default void onSpeakStart(){}                   // liveKit开始推送音频数据
        default void onSpeakEnd(){}                         // liveKit音频推送完毕
        default void onSpeakBuffer(ByteBuffer buffer){}     // liveKit推送的实时PCM数据
        default void onSpeakText(String text){}             // liveKit推送的音频对应的文案
        default void onMotion(String motion){}              // liveKit请求播放动作
        default void onAsrResult(String content, boolean end){}     // ASR识别的上送音频结果
        default void onLiveKitConnectError(int code, String msg){}  // liveKit连接异常
    }
```

onInitError中code的取值:

| 取值    | 说明       |
|-------|----------|
| -1000 | 授权异常     |
| -1001 | 模型加载异常   |


调用示例:
```
val renderer =
            DUIXRenderer(binding.glTextureView)
binding.glTextureView.setEGLContextClientVersion(GL_CONTEXT_VERSION)
binding.glTextureView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
binding.glTextureView.isOpaque = false           // 透明
binding.glTextureView.setRenderer(renderer)
binding.glTextureView.renderMode =
            GLSurfaceView.RENDERMODE_WHEN_DIRTY      // 一定要在设置完Render之后再调用
player = Player(mContext, renderer, playerCallback)
player?.init(conversationId, modelPath)
```

### 4. 播放音频流

在onSpeakStart回调中调用startPush函数触发开启播放音频

```
override fun onSpeakStart() {
            runOnUiThread {
                player?.startPush()
            }
        }
```

在onSpeakBuffer回调中调用pushPcm函数将pcm数据推送到播放器

```
 override fun onSpeakBuffer(buffer: ByteBuffer) {
            runOnUiThread {
                val data = ByteArray(buffer.remaining())
                buffer.get(data)
                player?.pushPcm(data)
            }
        }
```

在onSpeakEnd回调中调用stopPush函数完成推流结束信号

```
override fun onSpeakEnd() {
            runOnUiThread {
                player?.stopPush()
            }
        }
```

### 5. 数字人动作播放

在onMotion回调中调用requireMotion函数触发数字人播放动作区间

```
override fun onMotion(motion: String) {
            runOnUiThread {
                player?.requireMotion(motion)
            }
        }

```

### 6. 后台音频文本回显

在onSpeakText回调中展示音频对应的文本内容

```
override fun onSpeakText(text: String) {
            runOnUiThread {
                // 在这里显示文本内容
            }
        }
```

### 7. 语音识别结果

在onAsrResult回调中展示语音识别结果

```
override fun onAsrResult(content: String, end: Boolean) {
            runOnUiThread {
                // 在这里显示语音识别结果
            }
        }
```

### 8. 后台连接异常

后台连接出现异常时触发onLiveKitConnectError回调

```
override fun onLiveKitConnectError(code: Int, msg: String?) {
            
        }
```

## 六. 注意事项

1. 推送的音频数据格式为16k采样率、单通道、16bit采样深度。
